package com.qcadoo.mes.materialFlowResources.constants;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum DocumentType {

    RECEIPT("01receipt"), INTERNAL_INBOUND("02internalInbound"), INTERNAL_OUTBOUND("03internalOutbound"), RELEASE("04release"), TRANSFER(
            "05transfer");

    private final String value;

    private DocumentType(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static DocumentType of(final Entity document) {
        Preconditions.checkArgument(document != null, "Passed entity have not to be null.");
        return parseString(document.getStringField(DocumentFields.TYPE));
    }

    public static DocumentType parseString(final String type) {
        for (DocumentType documentType : values()) {
            if (StringUtils.equalsIgnoreCase(type, documentType.getStringValue())) {
                return documentType;
            }
        }
        throw new IllegalArgumentException("Couldn't parse DocumentType from string '" + type + "'");
    }
}
