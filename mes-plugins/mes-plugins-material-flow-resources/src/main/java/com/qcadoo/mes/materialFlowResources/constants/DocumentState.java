package com.qcadoo.mes.materialFlowResources.constants;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum DocumentState {

    DRAFT("01draft"), ACCEPTED("02accepted");

    private final String value;

    private DocumentState(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static DocumentState of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Passed entity have to be non null");
        return parseString(entity.getStringField(DocumentFields.STATE));
    }

    public static DocumentState parseString(final String type) {
        for (DocumentState documentState : DocumentState.values()) {
            if (StringUtils.equalsIgnoreCase(type, documentState.getStringValue())) {
                return documentState;
            }
        }
        throw new IllegalArgumentException("Couldn't parse DocumentState from string '" + type + "'");
    }
}
