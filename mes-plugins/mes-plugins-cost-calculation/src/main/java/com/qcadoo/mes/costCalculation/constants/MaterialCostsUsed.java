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
package com.qcadoo.mes.costCalculation.constants;

import org.apache.commons.lang3.StringUtils;

public enum MaterialCostsUsed {

    NOMINAL("01nominal"), AVERAGE("02average"), LAST_PURCHASE("03lastPurchase"), AVERAGE_OFFER_COST(
            "04averageOfferCost"), LAST_OFFER_COST("05lastOfferCost"), COST_FOR_ORDER("06costForOrder"),
    OFFER_COST_OR_LAST_PURCHASE("07offerCostOrLastPurchase");

    private final String materialCostsUsed;

    MaterialCostsUsed(final String materialCostsUsed) {
        this.materialCostsUsed = materialCostsUsed;
    }

    public String getStringValue() {
        return materialCostsUsed;
    }

    public static MaterialCostsUsed parseString(final String rawStringValue) {
        for (MaterialCostsUsed materialCostsUsed : values()) {
            if (StringUtils.equalsIgnoreCase(rawStringValue, materialCostsUsed.getStringValue())) {
                return materialCostsUsed;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot parse MaterialCostsUsed from '%s'", rawStringValue));
    }

}
