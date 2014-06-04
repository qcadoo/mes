package com.qcadoo.mes.technologies.constants;

import com.google.common.base.Preconditions;

public enum TechnologyOperationComponentType {

    OPERATION("operation"), REFERENCE_TECHNOLOGY("referenceTechnology");

    private final String technologyOperationComponentType;

    private TechnologyOperationComponentType(final String technologyOperationComponentType) {
        this.technologyOperationComponentType = technologyOperationComponentType;
    }

    public String getStringValue() {
        return this.technologyOperationComponentType;
    }

    public static TechnologyOperationComponentType parseString(final String string) {
        TechnologyOperationComponentType parsedValue = null;
        for (TechnologyOperationComponentType value : TechnologyOperationComponentType.values()) {
            if (value.getStringValue().equals(string)) {
                parsedValue = value;
                break;
            }
        }
        Preconditions.checkArgument(parsedValue != null, "Couldn't parse TechnologyOperationComponentType from string '" + string
                + "'");
        return parsedValue;
    }

}
