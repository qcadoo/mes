/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.constants;

public enum ProductType {
    COMPONENT("01component"), INTERMEDIATE("02intermediate"),;

    private final String productType;

    private ProductType(final String productType) {
        this.productType = productType;
    }

    public String getStringValue() {
        return productType;
    }

    public static ProductType parseString(final String string) {
        if ("01component".equals(string)) {
            return COMPONENT;
        } else if ("02intermediate".equals(string)) {
            return INTERMEDIATE;
        }

        throw new IllegalStateException("Unsupported productType: " + string);
    }

}
