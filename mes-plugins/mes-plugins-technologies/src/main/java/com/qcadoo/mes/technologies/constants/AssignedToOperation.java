package com.qcadoo.mes.technologies.constants;

public enum AssignedToOperation {

    WORKSTATIONS("01workstations"), WORKSTATIONS_TYPE("02workstationTypes");

    private final String value;

    private AssignedToOperation(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    public static AssignedToOperation parseString(final String string) {
        if ("01workstations".equals(string)) {
            return WORKSTATIONS;
        } else if ("02workstationTypes".equals(string)) {
            return WORKSTATIONS_TYPE;
        }

        throw new IllegalStateException("Unsupported assignedToOperation attribute: " + string);
    }

}
