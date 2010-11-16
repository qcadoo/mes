/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.plugins.internal.enums;

public enum PluginDescriptorProperties {
    IDENTIFIER("identifier"), NAME("name"), PACKAGE_NAME("packageName"), VERSION("version"), VENDOR("vendor"), DESCRIPTION(
            "description");

    private String value;

    private PluginDescriptorProperties(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
