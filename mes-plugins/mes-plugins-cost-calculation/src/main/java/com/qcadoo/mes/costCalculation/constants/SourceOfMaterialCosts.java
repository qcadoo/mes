/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

public enum SourceOfMaterialCosts {
    CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT("01currentGlobalDefinitionsInProduct"), FROM_ORDERS_MATERIAL_COSTS(
            "02fromOrdersMaterialCosts");

    private String sourceOfMaterialCosts;

    private SourceOfMaterialCosts(final String sourceOfMaterialCosts) {
        this.sourceOfMaterialCosts = sourceOfMaterialCosts;
    }

    public String getStringValue() {
        return sourceOfMaterialCosts;
    }

    public static SourceOfMaterialCosts parseString(final String string) {
        if ("01currentGlobalDefinitionsInProduct".equals(string)) {
            return CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT;
        } else if ("02fromOrdersMaterialCosts".equals(string)) {
            return FROM_ORDERS_MATERIAL_COSTS;
        }

        throw new IllegalStateException("Unsupported sourceOfMaterialCosts: " + string);
    }
}
