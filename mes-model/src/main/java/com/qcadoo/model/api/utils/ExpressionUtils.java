package com.qcadoo.model.api.utils;

import java.util.List;
import java.util.Locale;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.ExpressionServiceImpl;

public final class ExpressionUtils {

    private ExpressionUtils() {
    }

    public static String getValue(final Entity entity, final List<FieldDefinition> fieldDefinitions, final Locale locale) {
        return ExpressionServiceImpl.getInstance().getValue(entity, fieldDefinitions, locale);
    }

    public static String getValue(final Entity entity, final String expression, final Locale locale) {
        return ExpressionServiceImpl.getInstance().getValue(entity, expression, locale);
    }

}
