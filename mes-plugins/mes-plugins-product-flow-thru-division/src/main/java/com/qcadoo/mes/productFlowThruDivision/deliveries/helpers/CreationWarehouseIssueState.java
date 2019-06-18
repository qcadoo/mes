package com.qcadoo.mes.productFlowThruDivision.deliveries.helpers;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.validators.ErrorMessage;

import java.util.List;

public class CreationWarehouseIssueState {

    private final List<ErrorMessage> errors;

    private final boolean valid;

    public CreationWarehouseIssueState() {
        this.valid = true;
        this.errors = Lists.newArrayList();
    }

    public CreationWarehouseIssueState(List<ErrorMessage> errors) {
        this.valid = false;
        this.errors = errors;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }

    public boolean isValid() {
        return valid;
    }

}