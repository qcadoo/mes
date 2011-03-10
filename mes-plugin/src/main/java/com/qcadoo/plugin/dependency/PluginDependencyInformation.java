package com.qcadoo.plugin.dependency;

import java.util.Arrays;

import com.qcadoo.plugin.VersionUtils;

public class PluginDependencyInformation {

    private final String pluginIdentifier;

    private final int[] minimumVersion;

    private final boolean includeMinimumVersion;

    private final int[] maximumVersion;

    private final boolean includeMaximumVersion;

    public PluginDependencyInformation(final String key) {
        this(key, null, false, null, false);
    }

    public PluginDependencyInformation(final String key, final int[] minimumVersion, final boolean includeMinimumVersion,
            final int[] maximumVersion, final boolean includeMaximumVersion) {
        this.pluginIdentifier = key;
        this.minimumVersion = minimumVersion;
        this.includeMinimumVersion = includeMinimumVersion;
        this.maximumVersion = maximumVersion;
        this.includeMaximumVersion = includeMaximumVersion;

        if (this.minimumVersion != null && this.maximumVersion != null) {
            int compareResult = VersionUtils.compare(this.minimumVersion, this.maximumVersion);
            if (compareResult < 0) {
                throw new IllegalStateException("Minimum version is larger than maximum version");
            } else if (compareResult == 0) {
                if (!(includeMinimumVersion && includeMaximumVersion)) {
                    throw new IllegalStateException("Version range is empty");
                }
            }
        }

    }

    public String getDependencyPluginIdentifier() {
        return pluginIdentifier;
    }

    public boolean isVersionSattisfied(final int[] version) {
        if (minimumVersion != null) {
            int minComparationResult = VersionUtils.compare(minimumVersion, version);
            if (minComparationResult < 0) {
                return false;
            } else if (minComparationResult == 0 && !includeMinimumVersion) {
                return false;
            }
        }

        if (maximumVersion != null) {
            int maxComparationResult = VersionUtils.compare(maximumVersion, version);
            if (maxComparationResult > 0) {
                return false;
            } else if (maxComparationResult == 0 && !includeMaximumVersion) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (includeMaximumVersion ? 1231 : 1237);
        result = prime * result + (includeMinimumVersion ? 1231 : 1237);
        result = prime * result + ((pluginIdentifier == null) ? 0 : pluginIdentifier.hashCode());
        result = prime * result + Arrays.hashCode(maximumVersion);
        result = prime * result + Arrays.hashCode(minimumVersion);
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
        PluginDependencyInformation other = (PluginDependencyInformation) obj;
        if (includeMaximumVersion != other.includeMaximumVersion)
            return false;
        if (includeMinimumVersion != other.includeMinimumVersion)
            return false;
        if (pluginIdentifier == null) {
            if (other.pluginIdentifier != null)
                return false;
        } else if (!pluginIdentifier.equals(other.pluginIdentifier))
            return false;
        if (!Arrays.equals(maximumVersion, other.maximumVersion))
            return false;
        if (!Arrays.equals(minimumVersion, other.minimumVersion))
            return false;
        return true;
    }

}
