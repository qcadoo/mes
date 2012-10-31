/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

public enum ProductFamilyElementType {
    PARTICULAR_PRODUCT("01particularProduct"), PRODUCTS_FAMILY("02productsFamily");

    private final String type;

    private ProductFamilyElementType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static ProductFamilyElementType parseString(final String string) {
        if ("01particularProduct".equals(string)) {
            return PARTICULAR_PRODUCT;
        } else if ("02productsFamily".equals(string)) {
            return PRODUCTS_FAMILY;
        }

        throw new IllegalStateException("Unsupported ElementHierarchyInFamilyEnumStringValue: " + string);
    }
}
