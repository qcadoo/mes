package com.qcadoo.mes.basic.logdigger;

import java.util.Date;

public class QcadooErrorLogStore implements ErrorLogStore {

    private JdbcErrorLogStore jdbcErrorLogStore;

    private String[][] ignoredErrors;

    private String logdiggerAddress;

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

        // if (jdbcErrorLogStore.findSimilar(errorLog, 0, 1).getTotalCount() == 1) {
        // TODO KRNA add jira integration
        // }

        return id;
    }

}
