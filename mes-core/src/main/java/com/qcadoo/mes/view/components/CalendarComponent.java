/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.view.ContainerComponent;

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
