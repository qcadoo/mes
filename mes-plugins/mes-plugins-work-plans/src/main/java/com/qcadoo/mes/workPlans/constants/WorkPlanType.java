/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.workPlans.constants;

public enum WorkPlanType {
    NO_DISTINCTION("01noDistinction"), BY_END_PRODUCT("02byEndProduct"), BY_WORKSTATION_TYPE("03byWorkstationType"), BY_DIVISION(
            "04byDivision");

    private String stringValue;

    private WorkPlanType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static WorkPlanType parseString(final String string) {
        if ("01noDistinction".equals(string)) {
            return NO_DISTINCTION;
        } else if ("02byEndProduct".equals(string)) {
            return BY_END_PRODUCT;
        } else if ("03byWorkstationType".equals(string)) {
            return BY_WORKSTATION_TYPE;
        } else if ("04byDivision".equals(string)) {
            return BY_DIVISION;
        }

        throw new IllegalStateException("Unsupported workPlan type: " + string);
    }
}
