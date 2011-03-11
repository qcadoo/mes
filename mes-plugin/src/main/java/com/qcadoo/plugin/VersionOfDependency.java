package com.qcadoo.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class VersionOfDependency {

    private final static Pattern PATTERN = Pattern
            .compile("((\\(|\\[)?(\\d+(.\\d+(.\\d+)?)?))??,?((\\d+(.\\d+(.\\d+)?)?)(\\)|\\])?)??");

    private final Version minVersion;

    private final boolean includeMinVersion;

    private final Version maxVersion;

    private final boolean includeMaxVersion;

    public VersionOfDependency(final String version) {
        if (StringUtils.hasText(version)) {
            Matcher matcher = PATTERN.matcher(version);

            if (matcher.matches()) {
                if (matcher.group(3) != null && matcher.group(2) == null && matcher.group(7) == null && matcher.group(10) == null) {
                    minVersion = new Version(matcher.group(3));
                    includeMinVersion = true;
                    maxVersion = minVersion;
                    includeMaxVersion = true;
                } else {
                    minVersion = matcher.group(3) != null ? new Version(matcher.group(3)) : null;
                    includeMinVersion = !"(".equals(matcher.group(2));
                    maxVersion = matcher.group(7) != null ? new Version(matcher.group(7)) : null;
                    includeMaxVersion = !"(".equals(matcher.group(10));
                }

                if (this.minVersion != null && this.maxVersion != null) {
                    int compareResult = this.minVersion.compareTo(this.maxVersion);
                    if (compareResult > 0) {
                        throw new IllegalStateException("Version " + version
                                + " is invalid: min version is larger than max version");
                    } else if (compareResult == 0) {
                        if (!(includeMinVersion && includeMaxVersion)) {
                            throw new IllegalStateException("Version " + version + " is invalid: range is empty");
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Version " + version + " is invalid");
            }
        } else {
            minVersion = null;
            includeMinVersion = false;
            maxVersion = null;
            includeMaxVersion = false;
        }
    }

    public boolean isVersionSatisfied(final Version version) {
        if (minVersion != null) {
            int minComparationResult = minVersion.compareTo(version);
            if (minComparationResult > 0) {
                return false;
            } else if (minComparationResult == 0 && !includeMinVersion) {
                return false;
            }
        }

        if (maxVersion != null) {
            int maxComparationResult = maxVersion.compareTo(version);
            if (maxComparationResult < 0) {
                return false;
            } else if (maxComparationResult == 0 && !includeMaxVersion) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (includeMaxVersion ? 1231 : 1237);
        result = prime * result + (includeMinVersion ? 1231 : 1237);
        result = prime * result + ((maxVersion == null) ? 0 : maxVersion.hashCode());
        result = prime * result + ((minVersion == null) ? 0 : minVersion.hashCode());
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
        if (!(obj instanceof VersionOfDependency)) {
            return false;
        }
        VersionOfDependency other = (VersionOfDependency) obj;
        if (includeMaxVersion != other.includeMaxVersion) {
            return false;
        }
        if (includeMinVersion != other.includeMinVersion) {
            return false;
        }
        if (maxVersion == null) {
            if (other.maxVersion != null) {
                return false;
            }
        } else if (!maxVersion.equals(other.maxVersion)) {
            return false;
        }
        if (minVersion == null) {
            if (other.minVersion != null) {
                return false;
            }
        } else if (!minVersion.equals(other.minVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (minVersion == null && maxVersion == null) {
            return "0.0.0";
        } else if (minVersion != null && maxVersion == null) {
            return (includeMinVersion ? "[" : "(") + minVersion.toString();
        } else if (minVersion == null && maxVersion != null) {
            return maxVersion.toString() + (includeMaxVersion ? "]" : ")");
        } else if (minVersion.equals(maxVersion)) {
            return minVersion.toString();
        } else {
            return (includeMinVersion ? "[" : "(") + minVersion.toString() + "," + maxVersion.toString()
                    + (includeMaxVersion ? "]" : ")");
        }
    }

}
