package com.qcadoo.mes.states.messages.constants;

public enum MessageType {

    SUCCESS("01success"), INFO("02info"), FAILURE("03failure");

    private final String stringValue;

    private MessageType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public static MessageType parseString(final String string) {
        if ("01success".equalsIgnoreCase(string)) {
            return SUCCESS;
        } else if ("02info".equalsIgnoreCase(string)) {
            return INFO;
        } else if ("03failure".equalsIgnoreCase(string)) {
            return FAILURE;
        } else {
            throw new IllegalArgumentException("Couldn't parse MessageType from string '" + string + "'");
        }
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
