/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.materialRequirementCoverageForOrder.constans;

public enum CoverageType {
    ALL("01all"), WITHOUT_PRODUCTS_FROM_WAREHOUSE("02withoutProductsFromWarehouse"), ONLY_SHORCOMINGS_AND_DELAYS(
            "03onlyShorcomingsAndDelays");

    private final String coverageType;

    private CoverageType(final String coverageType) {
        this.coverageType = coverageType;
    }

    public String getStringValue() {
        return coverageType;
    }

    public static CoverageType parseString(final String string) {
        if ("01all".equals(string)) {
            return ALL;
        } else if ("03onlyShorcomingsAndDelays".equals(string)) {
            return WITHOUT_PRODUCTS_FROM_WAREHOUSE;
        } else if ("03onlyShorcomingsAndDelays".equals(string)) {
            return ONLY_SHORCOMINGS_AND_DELAYS;
        }

        throw new IllegalStateException("Unsupported coverageType: " + string);
    }

}
