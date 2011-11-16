package com.qcadoo.mes.technologies.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public class MessageHolder {

    private String targetReferenceName;

    private String messageKey;

    private MessageType messageType;
    
    private String[] vars;

    private MessageHolder(final String messageKey, final String targetReferenceName, final MessageType messageType, String... vars) {
        this.messageKey = messageKey;
        this.targetReferenceName = targetReferenceName;
        this.messageType = messageType;
        this.vars = vars;
    }

    public static MessageHolder error(final String messageKey, final String targetReferenceName, String... vars) {
        return new MessageHolder(messageKey, targetReferenceName, MessageType.FAILURE, vars);
    }

    public static MessageHolder error(final String messageKey, String... vars) {
        return new MessageHolder(messageKey, null, MessageType.FAILURE, vars);
    }

    public static MessageHolder info(final String messageKey, String... vars) {
        return new MessageHolder(messageKey, null, MessageType.INFO, vars);
    }

    public static MessageHolder success(final String messageKey, String... vars) {
        return new MessageHolder(messageKey, null, MessageType.SUCCESS, vars);
    }

    public String getTargetReferenceName() {
        return targetReferenceName;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public MessageType getMessageType() {
        return messageType;
    }
    
    public String[] getVars() {
        return vars;
    }

}
