/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionPerShift.constants;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.orders.dates.OrderDates;
import com.qcadoo.model.api.Entity;

public enum ProgressType {

    PLANNED("01planned") {

        @Override
        public DateTime extractStartDateTimeFrom(final OrderDates orderDates) {
            return orderDates.getStart().planned();
        }
    },
    CORRECTED("02corrected") {

        @Override
        public DateTime extractStartDateTimeFrom(final OrderDates orderDates) {
            return orderDates.getStart().correctedWithFallback();
        }
    };

    private final String stringValue;

    private ProgressType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static ProgressType of(final Entity progressForDay) {
        Preconditions.checkArgument(progressForDay != null, "Missing entity.");
        if (progressForDay.getBooleanField(ProgressForDayFields.CORRECTED)) {
            return CORRECTED;
        }
        return PLANNED;
    }

    public static ProgressType parseString(final String stringValue) {
        for (ProgressType progressType : values()) {
            if (StringUtils.equalsIgnoreCase(stringValue, progressType.getStringValue())) {
                return progressType;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot parse ProgressType enum value from '%s'", stringValue));
    }

    public abstract DateTime extractStartDateTimeFrom(final OrderDates orderDates);

}
