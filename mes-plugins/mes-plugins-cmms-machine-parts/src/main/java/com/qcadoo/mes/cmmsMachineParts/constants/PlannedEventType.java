/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.cmmsMachineParts.constants;

import com.qcadoo.model.api.Entity;

public enum PlannedEventType {
    REVIEW("01review"), REPAIRS("02repairs"), EXTERNAL_SERVICE("03externalService"), UDT_REVIEW("04udtReview"), METER_READING(
            "05meterReading"), MANUAL("06manual"), ADDITIONAL_WORK("07additionalWork"), AFTER_REVIEW("08afterReview");

    private final String type;

    public static PlannedEventType from(final Entity entity) {
        return parseString(entity.getStringField(PlannedEventFields.TYPE));
    }

    private PlannedEventType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static PlannedEventType parseString(final String type) {
        if ("01review".equals(type)) {
            return REVIEW;
        } else if ("02repairs".equals(type)) {
            return REPAIRS;
        } else if ("03externalService".equals(type)) {
            return EXTERNAL_SERVICE;
        } else if ("04udtReview".equals(type)) {
            return UDT_REVIEW;
        } else if ("05meterReading".equals(type)) {
            return METER_READING;
        } else if ("06manual".equals(type)) {
            return MANUAL;
        } else if ("07additionalWork".equals(type)) {
            return ADDITIONAL_WORK;
        } else if ("08afterReview".equals(type)) {
            return AFTER_REVIEW;
        }

        throw new IllegalStateException("Unsupported type: " + type);
    }

}
