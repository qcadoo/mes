package com.qcadoo.model.beans.plugins;

public class PluginsPlugin {

    private String packageName;

    private String name;

    private String identifier;

    private String fileName;

    private boolean base;

    private String status;

    private String version;

    private String vendor;

    private String description;

    private Long id;

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setBase(final boolean base) {
        this.base = base;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public boolean isBase() {
        return base;
    }

    public String getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDescription() {
        return description;
    }

}
