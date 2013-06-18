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
package com.qcadoo.mes.basicProductionCounting.constants;

public enum ProductionCountingQuantityTypeOfMaterial {
    COMPONENT("01component"), INTERMEDIATE("02intermediate"), FINAL_PRODUCT("03finalProduct"), WASTE("04waste");

    private final String productionCountingQuantityTypeOfMaterial;

    private ProductionCountingQuantityTypeOfMaterial(final String productionCountingQuantityTypeOfMaterial) {
        this.productionCountingQuantityTypeOfMaterial = productionCountingQuantityTypeOfMaterial;
    }

    public String getStringValue() {
        return productionCountingQuantityTypeOfMaterial;
    }

    public static ProductionCountingQuantityTypeOfMaterial parseString(final String string) {
        if ("01component".equals(string)) {
            return COMPONENT;
        } else if ("02intermediate".equals(string)) {
            return INTERMEDIATE;
        } else if ("03finalProduct".equals(string)) {
            return FINAL_PRODUCT;
        } else if ("04waste".equals(string)) {
            return WASTE;
        }

        throw new IllegalStateException("Unsupported ProductionCountingQuantityTypeOfMaterial: "
                + string);
    }

}
