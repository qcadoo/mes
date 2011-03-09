package com.qcadoo.plugin.dependency;

import java.util.Arrays;

public class PluginDependencyInformation {

    private String key;

    private int[] minimumVersion;

    private boolean includeMinimumVersion = true;

    private int[] maximumVersion;

    private boolean includeMaximumVersion = true;

    public PluginDependencyInformation(String key) {
        this(key, null, false, null, false);
    }

    public PluginDependencyInformation(String key, String minimumVersion, boolean includeMinimumVersion, String maximumVersion,
            boolean includeMaximumVersion) {
        super();
        this.key = key;
        this.minimumVersion = convertVersion(minimumVersion);
        this.includeMinimumVersion = includeMinimumVersion;
        this.maximumVersion = convertVersion(maximumVersion);
        this.includeMaximumVersion = includeMaximumVersion;

        if (this.minimumVersion != null && this.maximumVersion != null) {
            int compareResult = compareVersions(this.minimumVersion, this.maximumVersion);
            if (compareResult < 0) {
                throw new IllegalStateException("Minimum version is larger than maximum version");
            } else if (compareResult == 0) {
                if (!(includeMinimumVersion && includeMaximumVersion)) {
                    throw new IllegalStateException("Version range is empty");
                }
            }
        }

    }

    private int compareVersions(int[] version1, int[] version2) {
        for (int i = 0; i < 3; i++) {
            if (version1[i] > version2[i]) {
                return -1;
            } else if (version1[i] < version2[i]) {
                return 1;
            }
        }
        return 0;
    }

    private int[] convertVersion(String version) {
        if (version == null) {
            return null;
        }

        String[] splitVersion = version.split("\\.");

        if (splitVersion.length > 3) {
            throw new IllegalStateException("Version has too many elements");
        }

        int[] convertedVersion = new int[3];

        for (int i = 0; i < 3; i++) {
            if (i < splitVersion.length) {
                convertedVersion[i] = Integer.parseInt(splitVersion[i]);
            } else {
                convertedVersion[i] = 0;
            }
        }

        return convertedVersion;
    }

    public String getKey() {
        return key;
    }

    public boolean isVersionSattisfied(String version) {
        int[] convertedVersion = convertVersion(version);
        if (minimumVersion != null) {
            int minComparationResult = compareVersions(minimumVersion, convertedVersion);
            if (minComparationResult < 0) {
                return false;
            } else if (minComparationResult == 0 && !includeMinimumVersion) {
                return false;
            }
        }

        if (maximumVersion != null) {
            int maxComparationResult = compareVersions(maximumVersion, convertedVersion);
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
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + Arrays.hashCode(maximumVersion);
        result = prime * result + Arrays.hashCode(minimumVersion);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (!Arrays.equals(maximumVersion, other.maximumVersion))
            return false;
        if (!Arrays.equals(minimumVersion, other.minimumVersion))
            return false;
        return true;
    }

}
