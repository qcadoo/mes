/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.costCalculation.constants;

public enum CalculateMaterialCostsMode {
    NOMINAL("01nominal"), AVERAGE("02average"), LAST_PURCHASE("03lastPurchase"), COST_FOR_ORDER("04costForOrder");

    private final String calculateMaterialCostsMode;

    private CalculateMaterialCostsMode(final String calculateMaterialCostsMode) {
        this.calculateMaterialCostsMode = calculateMaterialCostsMode;
    }

    public String getStringValue() {
        return calculateMaterialCostsMode;
    }

    public static CalculateMaterialCostsMode parseString(final String string) {
        if ("01nominal".equals(string)) {
            return NOMINAL;
        } else if ("02average".equals(string)) {
            return AVERAGE;
        } else if ("03lastPurchase".equals(string)) {
            return LAST_PURCHASE;
        } else if ("04costForOrder".equals(string)) {
            return COST_FOR_ORDER;
        }

        throw new IllegalStateException("Unsupported calculateMaterialCostsMode: " + string);
    }

}
