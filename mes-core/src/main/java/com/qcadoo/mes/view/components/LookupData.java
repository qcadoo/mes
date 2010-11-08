package com.qcadoo.mes.view.components;

public final class LookupData extends SimpleValue {

    private String selectedEntityValue;

    private String selectedEntityCode;

    private Long contextEntityId;

    public LookupData() {
    }

    public Long getSelectedEntityId() {
        return (Long) getValue();
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        setValue(selectedEntityId);
    }

    public String getSelectedEntityValue() {
        return selectedEntityValue;
    }

    public void setSelectedEntityValue(final String selectedEntityValue) {
        this.selectedEntityValue = selectedEntityValue;
    }

    public void setContextEntityId(final Long contextEntityId) {
        this.contextEntityId = contextEntityId;
    }

    public Long getContextEntityId() {
        return contextEntityId;
    }

    public void setSelectedEntityCode(final String selectedEntityCode) {
        this.selectedEntityCode = selectedEntityCode;
    }

    public String getSelectedEntityCode() {
        return selectedEntityCode;
    }

}
