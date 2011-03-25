package com.qcadoo.mes.plugins.controller;

public class PluginAdditionalData {

    private final String name;

    private final String description;

    private final String vendor;

    private final String vendorUrl;

    private final boolean isSystem;

    public PluginAdditionalData(final String name, final String description, final String vendor, final String vendorUrl,
            final boolean isSystem) {
        this.name = name;
        this.description = description;
        this.vendor = vendor;
        this.vendorUrl = vendorUrl;
        this.isSystem = isSystem;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVendor() {
        return vendor;
    }

    public String getVendorUrl() {
        return vendorUrl;
    }

    public boolean isSystem() {
        return isSystem;
    }

}
