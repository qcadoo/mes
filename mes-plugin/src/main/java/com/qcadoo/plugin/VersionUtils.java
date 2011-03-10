package com.qcadoo.plugin;

public class VersionUtils {

    public static int[] parse(final String version) {
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

    public static int compare(final int[] version, final int[] otherVersion) {
        for (int i = 0; i < 3; i++) {
            if (version[i] > otherVersion[i]) {
                return -1;
            } else if (version[i] < otherVersion[i]) {
                return 1;
            }
        }
        return 0;
    }

    public static int[][] parseDependency(final String version) {
        return new int[][] { { 1, 0, 0 }, { 0 }, { 1, 0, 0 }, { 1 } };
    }

}
