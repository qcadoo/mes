package com.qcadoo.mes.basic.constants;

public enum TimetableExceptionType {

    FREE_TIME("01freeTime"), WORK_TIME("02workTime");

    private final String stringValue;

    private TimetableExceptionType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static TimetableExceptionType parseString(final String stringValue) {
        for (TimetableExceptionType type : values()) {
            if (type.getStringValue().equals(stringValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Couldn't parse OrderState from string '" + stringValue + "'");
    }

}
