package com.qcadoo.mes.basic.constants;

public enum TypeTerminalLicenses {

    UP_TO_TEN_EMPLOYEES("01upToTenEmployees"), FROM_11_TO_50_EMPLOYEES("02from11to50Employees"), OVER_51_EMPLOYEES("03over51Employees");

    private final String stringValue;

    private TypeTerminalLicenses(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static TypeTerminalLicenses parseString(final String stringValue) {
        for (TypeTerminalLicenses type : values()) {
            if (type.getStringValue().equals(stringValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Couldn't parse TypeTerminalLicenses from string '" + stringValue + "'");
    }

}
