package com.qcadoo.mes.enums;

public enum PluginStatus {

    DOWNLOADED("downloaded"), INSTALLED("installed"), ACTIVE("active");

    private String value;

    private PluginStatus(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
