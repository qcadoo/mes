package com.qcadoo.mes.technologies.constants;

import com.google.common.base.Preconditions;

public enum TechnologyOperationComponentReferenceMode {

    REFERENCE("01reference"), COPY("02copy");

    private final String technologyOperationComponentReferenceMode;

    private TechnologyOperationComponentReferenceMode(final String technologyOperationComponentReferenceMode) {
        this.technologyOperationComponentReferenceMode = technologyOperationComponentReferenceMode;
    }

    public String getStringValue() {
        return this.technologyOperationComponentReferenceMode;
    }

    public static TechnologyOperationComponentReferenceMode parseString(final String string) {
        TechnologyOperationComponentReferenceMode referenceMode = null;
        for (TechnologyOperationComponentReferenceMode value : TechnologyOperationComponentReferenceMode.values()) {
            if (value.getStringValue().equals(string)) {
                referenceMode = value;
                break;
            }
        }
        Preconditions.checkArgument(referenceMode != null,
                "Couldn't parse TechnologyOperationComponentReferenceMode from string '" + string + "'");
        return referenceMode;
    }

}
