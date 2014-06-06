package com.qcadoo.mes.materialFlowDocuments.constants;

public enum DocumentState {

    DRAFT("01draft"), ACCEPTED("02accepted");

    private final String value;

    private DocumentState(final String value){
        this.value = value;
    }
    public String getStringValue() {
        return this.value;
    }

    public static DocumentState parseString(final String type) {
        if (DRAFT.getStringValue().equalsIgnoreCase(type)) {
            return DRAFT;
        } else if (ACCEPTED.getStringValue().equalsIgnoreCase(type)) {
            return ACCEPTED;
        } else {
            throw new IllegalArgumentException("Couldn't parse DocumentState from string '" + type + "'");
        }
    }
}

