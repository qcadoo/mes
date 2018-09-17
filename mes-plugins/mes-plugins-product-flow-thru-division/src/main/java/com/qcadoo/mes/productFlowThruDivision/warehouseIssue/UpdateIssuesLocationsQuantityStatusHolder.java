package com.qcadoo.mes.productFlowThruDivision.warehouseIssue;

public class UpdateIssuesLocationsQuantityStatusHolder {

    private final boolean updated;
    private final String message;

    public UpdateIssuesLocationsQuantityStatusHolder(boolean updated, String message) {
        this.updated = updated;
        this.message = message;
    }

    public boolean isUpdated() {
        return updated;
    }

    public String getMessage() {
        return message;
    }
}
