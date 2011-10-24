package com.qcadoo.mes.orders.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public class ChangeOrderStateMessage {

    private String referenceToField;

    private String message;

    private MessageType type;

    public ChangeOrderStateMessage(String message, String referenceToField, MessageType type) {
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

    public static ChangeOrderStateMessage info(String message) {
        return new ChangeOrderStateMessage(message, null, MessageType.INFO);
    }

    public static ChangeOrderStateMessage success(String message) {
        return new ChangeOrderStateMessage(message, null, MessageType.INFO);
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
