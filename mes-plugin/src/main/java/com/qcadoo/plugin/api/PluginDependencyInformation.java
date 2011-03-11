package com.qcadoo.plugin.api;


public class PluginDependencyInformation {

    private final String identifier;

    private final VersionOfDependency version;

    public PluginDependencyInformation(final String key) {
        this(key, new VersionOfDependency(""));
    }

    public PluginDependencyInformation(final String identifier, final VersionOfDependency version) {
        this.identifier = identifier;
        this.version = version;
    }

    public String getDependencyPluginIdentifier() {
        return identifier;
    }

    public boolean isVersionSattisfied(final Version version) {
        return this.version.isVersionSatisfied(version);
    }

    @Override
    public String toString() {
        return identifier + " " + version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PluginDependencyInformation)) {
            return false;
        }
        PluginDependencyInformation other = (PluginDependencyInformation) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
