/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.plugins.internal.enums;

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
