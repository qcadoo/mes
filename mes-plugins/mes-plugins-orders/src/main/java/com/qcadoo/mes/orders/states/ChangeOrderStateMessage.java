package com.qcadoo.mes.orders.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public class ChangeOrderStateMessage {

    private String referenceToField;

    private String message;

    private MessageType type;

    // statyczne konstruktory

    private ChangeOrderStateMessage(String message, String referenceToField, MessageType type) {
        this.referenceToField = referenceToField;
        this.message = message;
        this.type = type;
    }

    public static ChangeOrderStateMessage error(String message, String referenceToField) {
        return new ChangeOrderStateMessage(message, referenceToField, MessageType.FAILURE);
    }

    public static ChangeOrderStateMessage error(String message) {
        return new ChangeOrderStateMessage(message, null, MessageType.FAILURE);
    }

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

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

}
