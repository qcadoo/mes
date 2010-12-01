/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.viewold.ContainerComponent;

/**
 * Represents datepicker input element.
 */
public final class CalendarComponent extends SimpleFieldComponent {

    public CalendarComponent(final String name, final ContainerComponent<?> parent, final String fieldName,
            final String dataSource, final TranslationService translationService) {
        super(name, parent, fieldName, dataSource, translationService);
    }

    @Override
    public String getType() {
        return "calendar";
    }

    @Override
    public String convertToViewValue(final Object value) {
        if (value instanceof Date) {
            return new SimpleDateFormat(DateType.DATE_FORMAT).format((Date) value);
        } else {
            return String.valueOf(value).trim();
        }
    }

    @Override
    public Object convertToDatabaseValue(final String value) {
        return value;
    }
}
