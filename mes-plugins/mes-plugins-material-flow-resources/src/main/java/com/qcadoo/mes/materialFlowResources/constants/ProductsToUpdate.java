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
package com.qcadoo.mes.materialFlowResources.constants;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum ProductsToUpdate {

    ALL("01all"), SELECTED("02selected");

    private final String value;

    private ProductsToUpdate(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static ProductsToUpdate of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Passed entity have to be non null");
        return parseString(entity.getStringField(CostNormsGeneratorFields.PRODUCTS_TO_UPDATE));
    }

    public static ProductsToUpdate parseString(final String type) {
        for (ProductsToUpdate productsToUpdate : ProductsToUpdate.values()) {
            if (StringUtils.equalsIgnoreCase(type, productsToUpdate.getStringValue())) {
                return productsToUpdate;
            }
        }
        throw new IllegalArgumentException("Couldn't parse ProductsToUpdate from string '" + type + "'");
    }
}
