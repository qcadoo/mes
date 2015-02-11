package com.qcadoo.mes.productionCountingWithCosts.constants;


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

import org.apache.commons.lang3.StringUtils;

public enum PriceBasedOn {

    NOMINAL_PRODUCT_COST("01nominalProductCost"), REAL_PRODUCTION_COST("02realProductionCost");

    private final String value;

    private PriceBasedOn(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static PriceBasedOn parseString(final String type) {
        for (PriceBasedOn priceType : values()) {
            if (StringUtils.equalsIgnoreCase(type, priceType.getStringValue())) {
                return priceType;
            }
        }
        throw new IllegalArgumentException("Couldn't parse PriceBasedOn from string '" + type + "'");
    }
}
