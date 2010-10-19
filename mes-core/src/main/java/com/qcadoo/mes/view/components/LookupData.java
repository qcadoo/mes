package com.qcadoo.mes.view.components;

public class LookupData {

    private Long selectedEntityId;

    private String selectedEntityValue;

    private Long contextEntityId;

    public LookupData() {
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
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

}
