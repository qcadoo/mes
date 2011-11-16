package com.qcadoo.mes.technologies.states;

import com.qcadoo.view.api.ComponentState.MessageType;

public class MessageHolder {

    private String targetReferenceName;

    private String message;

    private MessageType messageType;

    private MessageHolder(final String message, final String targetReferenceName, final MessageType messageType) {
        this.message = message;
        this.targetReferenceName = targetReferenceName;
        this.messageType = messageType;
    }

    public static MessageHolder error(final String message, final String targetReferenceName) {
        return new MessageHolder(message, targetReferenceName, MessageType.FAILURE);
    }

    public static MessageHolder error(final String message) {
        return new MessageHolder(message, null, MessageType.FAILURE);
    }

    public static MessageHolder info(final String message) {
        return new MessageHolder(message, null, MessageType.INFO);
    }

    public static MessageHolder success(final String message) {
        return new MessageHolder(message, null, MessageType.SUCCESS);
    }

    public String getTargetReferenceName() {
        return targetReferenceName;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

}
