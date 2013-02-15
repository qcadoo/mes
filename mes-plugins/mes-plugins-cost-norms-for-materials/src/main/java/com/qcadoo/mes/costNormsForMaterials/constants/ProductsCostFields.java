/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.costNormsForMaterials.constants;

public enum ProductsCostFields {
    AVERAGE("averageCost"), LAST_PURCHASE("lastPurchaseCost"), NOMINAL("nominalCost"), COST_FOR_ORDER("costForOrder"), AVERAGE_OFFER_COST(
            "averageOfferCost"), LAST_OFFER_COST("lastOfferCost");

    private final String strValue;

    private ProductsCostFields(final String strValue) {
        this.strValue = strValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public static ProductsCostFields parseString(final String strValue) {
        if ("01nominal".equals(strValue)) {
            return NOMINAL;
        } else if ("02average".equals(strValue)) {
            return AVERAGE;
        } else if ("03lastPurchase".equals(strValue)) {
            return LAST_PURCHASE;
        } else if ("04averageOfferCost".equals(strValue)) {
            return LAST_OFFER_COST;
        } else if ("05lastOfferCost".equals(strValue)) {
            return AVERAGE_OFFER_COST;
        } else if ("06costForOrder".equals(strValue)) {
            return COST_FOR_ORDER;
        }
        throw new IllegalStateException("Unsupported calculateMaterialCostsMode: " + strValue);
    }

}
