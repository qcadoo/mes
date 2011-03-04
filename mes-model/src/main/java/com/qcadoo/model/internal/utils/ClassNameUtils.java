package com.qcadoo.model.internal.utils;

import org.springframework.util.StringUtils;

public final class ClassNameUtils {

    public static String getFullyQualifiedClassName(final String pluginIdentifier, final String modelName) {
        return "com.qcadoo.model.beans." + pluginIdentifier + "." + StringUtils.capitalize(pluginIdentifier)
                + StringUtils.capitalize(modelName);
    }

}
