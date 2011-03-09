package com.qcadoo.plugin;

public class PluginInformation {

    private final String description;

    private final String vendor;

    private final String vendorUrl;

    private final String version;

    private final String name;

    public PluginInformation(final String name, final String description, final String vendor, final String vendorUrl,
            final String version) {
        super();
        this.name = name;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((vendor == null) ? 0 : vendor.hashCode());
        result = prime * result + ((vendorUrl == null) ? 0 : vendorUrl.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PluginInformation other = (PluginInformation) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (vendor == null) {
            if (other.vendor != null)
                return false;
        } else if (!vendor.equals(other.vendor))
            return false;
        if (vendorUrl == null) {
            if (other.vendorUrl != null)
                return false;
        } else if (!vendorUrl.equals(other.vendorUrl))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
