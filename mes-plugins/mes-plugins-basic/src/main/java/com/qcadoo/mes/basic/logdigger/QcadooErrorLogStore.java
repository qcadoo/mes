package com.qcadoo.mes.basic.logdigger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.logdigger.connector.AttributeProvider;
import com.logdigger.connector.ErrorLog;
import com.logdigger.connector.ErrorLogStore;
import com.logdigger.connector.ResultsPage;
import com.logdigger.jdbc.JdbcErrorLogStore;

public class QcadooErrorLogStore implements ErrorLogStore {

    private static final Logger LOG = LoggerFactory.getLogger(QcadooErrorLogStore.class);

    private JdbcErrorLogStore jdbcErrorLogStore;

    private String[][] ignoredErrors;

    private String logdiggerAddress;

    private String logdiggerJira;

    private String logdiggerJirUser;

    private String logdiggerJiraPass;

    @Override
    public void destroy() {
        jdbcErrorLogStore.destroy();
    }

    @Override
    public ResultsPage<ErrorLog> findInDateRange(final Date fromDate, final Date toDate, final int offset, final int limit) {
        return jdbcErrorLogStore.findInDateRange(fromDate, toDate, offset, limit);
    }

    @Override
    public ResultsPage<ErrorLog> findSimilar(final ErrorLog sample, final int offset, final int limit) {
        return jdbcErrorLogStore.findSimilar(sample, offset, limit);
    }

    @Override
    public ErrorLog getErrorLogById(final String id) {
        return jdbcErrorLogStore.getErrorLogById(id);
    }

    @Override
    public ResultsPage<ErrorLog> getErrorLogs(final int offset, final int limit) {
        return jdbcErrorLogStore.getErrorLogs(offset, limit);
    }

    @Override
    public LastStoreError getLastStoreError() {
        return jdbcErrorLogStore.getLastStoreError();
    }

    @Override
    public int getMaxCategories() {
        return jdbcErrorLogStore.getMaxCategories();
    }

    @Override
    public String getStoreDescription() {
        return jdbcErrorLogStore.getStoreDescription() + " + Qcadoo extention";
    }

    @Override
    public void init(final AttributeProvider attributeProvider) {
        jdbcErrorLogStore = new JdbcErrorLogStore();
        jdbcErrorLogStore.init(attributeProvider);

        ignoredErrors = new String[][] {};

        logdiggerAddress = String.valueOf(attributeProvider.getAttribute("logdiggerAddress"));
        logdiggerJira = String.valueOf(attributeProvider.getAttribute("logdiggerJira"));
        logdiggerJirUser = String.valueOf(attributeProvider.getAttribute("logdiggerJirUser"));
        logdiggerJiraPass = String.valueOf(attributeProvider.getAttribute("logdiggerJiraPass"));
    }

    @Override
    public boolean isAvailable(final Feature feature) {
        return jdbcErrorLogStore.isAvailable(feature);
    }

    @Override
    public String save(final ErrorLog errorLog) {
        for (String[] ignoredError : ignoredErrors) {
            for (int i = 0; i < ignoredError.length; i++) {
                if (ignoredError[i] == null) {
                    return null;
                }
                if (!ignoredError[i].equals(errorLog.getCategorySignature(i))) {
                    break;
                }
                if (i >= ignoredError.length - 1) {
                    return null;
                }
            }
        }

        String id = jdbcErrorLogStore.save(errorLog);

        if (jdbcErrorLogStore.findSimilar(errorLog, 0, 1).getTotalCount() == 1) {
            String description = (logdiggerAddress + "/console/view?err=" + id + "\n\n" + errorLog.getStackTrace()).replaceAll(
                    "[\n\t\r]+", "\\\n");
            String summary = "LogDigger " + errorLog.getLogSnippet().split("\n")[0];

            JerseyJiraRestClientFactory f = new JerseyJiraRestClientFactory();
            try {
                URI jiraServerUri = new URI(logdiggerJira);
                NullProgressMonitor progressMonitor = new NullProgressMonitor();

                JiraRestClient jc = f.createWithBasicHttpAuthentication(jiraServerUri, logdiggerJirUser, logdiggerJiraPass);

                IssueInputBuilder ib = new IssueInputBuilder("QCADOOCLS", (long) 1);
                ib.setReporterName("logdigger");
                ib.setSummary(summary);
                ib.setDescription(description);
                ib.setPriorityId((long) 3);
                IssueInput issue = ib.build();

                jc.getIssueClient().createIssue(issue, progressMonitor);

                LOG.info(" Logdigger: " + issue.toString());
            } catch (URISyntaxException e) {
                LOG.warn(e.getMessage(), e);
            }

        }

        return id;
    }
}
