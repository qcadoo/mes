package com.qcadoo.mes.orders.states;

public class ChangeOrderStateError {

    private String referenceToField;

    private String message;

    public String getMessage() {
        return message;
    }

    public String getReferenceToField() {
        return referenceToField;
    }

    public void setReferenceToField(String referenceToField) {
        this.referenceToField = referenceToField;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
