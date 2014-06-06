package com.qcadoo.mes.materialFlowDocuments.constants;

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

    public static DocumentType parseString(final String type) {
        if (RECEIPT.getStringValue().equalsIgnoreCase(type)) {
            return RECEIPT;
        } else if (INTERNAL_INBOUND.getStringValue().equalsIgnoreCase(type)) {
            return INTERNAL_INBOUND;
        } else if (INTERNAL_OUTBOUND.getStringValue().equalsIgnoreCase(type)) {
            return INTERNAL_OUTBOUND;
        } else if (RELEASE.getStringValue().equalsIgnoreCase(type)) {
            return RELEASE;
        } else if (TRANSFER.getStringValue().equalsIgnoreCase(type)) {
            return TRANSFER;
        } else {
            throw new IllegalArgumentException("Couldn't parse DocumentType from string '" + type + "'");
        }
    }
}
