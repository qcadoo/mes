package com.qcadoo.plugin;

public class PluginInformation {

    private String description;

    private String vendor;

    private String vendorUrl;

    private String version;

    private final String name;

    private final String fileName;

    public PluginInformation(final String name, final String description, final String vendor, final String vendorUrl,
            final String version, final String fileName) {
        super();
        this.name = name;
        this.fileName = fileName;
        this.description = description;
        this.vendor = vendor;
        this.vendorUrl = vendorUrl;
        this.version = version;
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

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

}
