package com.qcadoo.mes.lineChangeoverNorms.constants;

public enum ChangeoverType {

    FOR_TECHNOLOGY("01forTechnology"), FOR_TECHNOLOGY_GROUP("02forTechnologyGroup");

    private final String state;

    private ChangeoverType(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }

}
