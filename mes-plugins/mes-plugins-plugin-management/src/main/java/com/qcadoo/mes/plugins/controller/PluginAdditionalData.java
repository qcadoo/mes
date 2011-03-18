package com.qcadoo.mes.plugins.controller;

public class PluginAdditionalData {

    public final String name;

    public final String description;

    public final String vendor;

    public final String vendorUrl;

    public final boolean isSystem;

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
