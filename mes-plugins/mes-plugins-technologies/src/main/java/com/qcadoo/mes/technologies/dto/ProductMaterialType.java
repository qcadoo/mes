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
package com.qcadoo.mes.technologies.dto;

import org.apache.commons.lang3.StringUtils;

public enum ProductMaterialType {

    COMPONENT("01component"), INTERMEDIATE("02intermediate"), FINAL_PRODUCT("03finalProduct"), WASTE("04waste"),
    ADDITIONAL_FINAL_PRODUCT("05additionalFinalProduct"), NONE("");

    private final String productMaterialType;

    private ProductMaterialType(final String type) {
        this.productMaterialType = type;
    }

    public String getStringValue() {
        return productMaterialType;
    }

    public static ProductMaterialType parseString(final String productMaterialType) {
        if (StringUtils.isEmpty(productMaterialType)) {
            return NONE;
        } else if ("01component".equals(productMaterialType)) {
            return COMPONENT;
        } else if ("02intermediate".equals(productMaterialType)) {
            return INTERMEDIATE;
        } else if ("03finalProduct".equals(productMaterialType)) {
            return FINAL_PRODUCT;
        } else if ("04waste".equals(productMaterialType)) {
            return WASTE;
        }else if ("05additionalFinalProduct".equals(productMaterialType)) {
            return ADDITIONAL_FINAL_PRODUCT;
        }

        throw new IllegalStateException("Unsupported ProductMaterialType: " + productMaterialType);
    }
}
