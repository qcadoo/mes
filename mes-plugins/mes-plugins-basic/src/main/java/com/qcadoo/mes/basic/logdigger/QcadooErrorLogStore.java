/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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

    private String[][] ignoredErrorMessages;

    private String logdiggerAddress;

    private String logdiggerJira;

    private String logdiggerJiraUser;

    private String logdiggerJiraPass;

    private String logdiggerJiraProject;

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

        ignoredErrors = new String[][] { {} };
        ignoredErrorMessages = new String[][] { { "org.springframework.web.util.NestedServletException: Request processing failed; nested exception is com.qcadoo.mes.urcCore.IntegrationException:" } };

        logdiggerAddress = String.valueOf(attributeProvider.getAttribute("logdiggerAddress"));
        logdiggerJira = String.valueOf(attributeProvider.getAttribute("logdiggerJira"));
        logdiggerJiraUser = String.valueOf(attributeProvider.getAttribute("logdiggerJiraUser"));
        logdiggerJiraPass = String.valueOf(attributeProvider.getAttribute("logdiggerJiraPass"));
        logdiggerJiraProject = String.valueOf(attributeProvider.getAttribute("logdiggerJiraProject"));
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

        for (String[] ignoredErrorMessage : ignoredErrorMessages) {
            for (int i = 0; i < ignoredErrorMessage.length; i++) {
                if (ignoredErrorMessage[i] == null) {
                    return null;
                }
                if (!errorLog.getMessage().startsWith(ignoredErrorMessage[i])) {
                    break;
                }
                if (i >= ignoredErrorMessage.length - 1) {
                    return null;
                }
            }
        }

        String id = jdbcErrorLogStore.save(errorLog);

        if (jdbcErrorLogStore.findSimilar(errorLog, 0, 1).getTotalCount() == 1) {
            String description = (logdiggerAddress + "/console/view?err=" + id + "\n\n" + errorLog.getStackTrace()).replaceAll(
                    "[\n\t\r]+", "\\\n");
            String summary = "LogDigger " + errorLog.getLogSnippet().split("\n")[0];

            JerseyJiraRestClientFactory jerseyJiraRestClientFactory = new JerseyJiraRestClientFactory();

            try {
                URI jiraServerURI = new URI(logdiggerJira);
                NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();

                JiraRestClient jiraRestClient = jerseyJiraRestClientFactory.createWithBasicHttpAuthentication(jiraServerURI,
                        logdiggerJiraUser, logdiggerJiraPass);

                IssueInputBuilder issueInputBuilder = new IssueInputBuilder(logdiggerJiraProject, (long) 1);
                issueInputBuilder.setReporterName(logdiggerJiraUser);
                issueInputBuilder.setSummary(summary);
                issueInputBuilder.setDescription(description);
                issueInputBuilder.setPriorityId((long) 3);

                IssueInput issueInput = issueInputBuilder.build();

                jiraRestClient.getIssueClient().createIssue(issueInput, nullProgressMonitor);

                LOG.info(" Logdigger: " + issueInput.toString());
            } catch (URISyntaxException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        return id;
    }

}
