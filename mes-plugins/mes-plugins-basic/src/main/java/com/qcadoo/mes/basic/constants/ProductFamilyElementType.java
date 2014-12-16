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
package com.qcadoo.mes.basic.constants;

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public enum ProductFamilyElementType {
    PARTICULAR_PRODUCT("01particularProduct"), PRODUCTS_FAMILY("02productsFamily");

    private final String productFamilyElementType;

    public static ProductFamilyElementType from(final Entity entity) {
        if (isBasicProduct(entity)) {
            return parseString(entity.getStringField(ProductFields.ENTITY_TYPE));
        }
        throw new IllegalArgumentException(String.format("Expected basic_product, but got %s", entity));
    }

    private static boolean isBasicProduct(final Entity entity) {
        DataDefinition dataDefinition = entity.getDataDefinition();
        String pluginIdentifier = dataDefinition.getPluginIdentifier();
        String modelName = dataDefinition.getName();
        return StringUtils.equalsIgnoreCase(pluginIdentifier, BasicConstants.PLUGIN_IDENTIFIER)
                && StringUtils.equalsIgnoreCase(modelName, BasicConstants.MODEL_PRODUCT);
    }

    private ProductFamilyElementType(final String type) {
        this.productFamilyElementType = type;
    }

    public String getStringValue() {
        return productFamilyElementType;
    }

    public static ProductFamilyElementType parseString(final String productFamilyElementType) {
        if ("01particularProduct".equals(productFamilyElementType)) {
            return PARTICULAR_PRODUCT;
        } else if ("02productsFamily".equals(productFamilyElementType)) {
            return PRODUCTS_FAMILY;
        }

        throw new IllegalStateException("Unsupported ProductFamilyElementType: " + productFamilyElementType);
    }

}
