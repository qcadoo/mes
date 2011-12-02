package com.qcadoo.mes.productionCounting.internal.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public class ChangeRecordStateMessage {

    private String referenceToField;

    private String message;

    private MessageType type;

    private final String[] vars;

    public ChangeRecordStateMessage(final String message, final String referenceToField, final MessageType type,
            final String... vars) {
        this.message = message;
        this.referenceToField = referenceToField;
        this.type = type;
        this.vars = vars;
    }

    public static ChangeRecordStateMessage errorForComponent(final String message, final String referenceToField,
            final String... vars) {
        return new ChangeRecordStateMessage(message, referenceToField, MessageType.FAILURE, vars);
    }

    public static ChangeRecordStateMessage error(final String message, final String... vars) {
        return new ChangeRecordStateMessage(message, null, MessageType.FAILURE, vars);
    }

    public static ChangeRecordStateMessage info(final String message, final String... vars) {
        return new ChangeRecordStateMessage(message, null, MessageType.INFO, vars);
    }

    public static ChangeRecordStateMessage success(final String message, final String... vars) {
        return new ChangeRecordStateMessage(message, null, MessageType.INFO, vars);
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

    public String[] getVars() {
        return vars;
    }

}
