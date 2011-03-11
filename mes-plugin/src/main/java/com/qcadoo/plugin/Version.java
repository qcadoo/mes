package com.qcadoo.plugin;

public class Version implements Comparable<Version> {

    private final int major;

    private final int minor;

    private final int branch;

    public Version(final String version) {
        String[] split = version.split("\\.");

        if (split.length > 3) {
            throw new IllegalStateException("Version " + version + " is invalid");
        }

        if (split.length > 0) {
            major = Integer.parseInt(split[0]);
        } else {
            major = 0;
        }

        if (split.length > 1) {
            minor = Integer.parseInt(split[1]);
        } else {
            minor = 0;
        }

        if (split.length > 2) {
            branch = Integer.parseInt(split[2]);
        } else {
            branch = 0;
        }
    }

    @Override
    public int compareTo(final Version otherVersion) {
        if (major < otherVersion.major) {
            return -1;
        } else if (major > otherVersion.major) {
            return 1;
        }

        if (minor < otherVersion.minor) {
            return -1;
        } else if (minor > otherVersion.minor) {
            return 1;
        }

        if (branch < otherVersion.branch) {
            return -1;
        } else if (branch > otherVersion.branch) {
            return 1;
        }

        return 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + branch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + branch;
        result = prime * result + major;
        result = prime * result + minor;
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
        if (!(obj instanceof Version)) {
            return false;
        }
        Version other = (Version) obj;
        if (branch != other.branch) {
            return false;
        }
        if (major != other.major) {
            return false;
        }
        if (minor != other.minor) {
            return false;
        }
        return true;
    }

}
