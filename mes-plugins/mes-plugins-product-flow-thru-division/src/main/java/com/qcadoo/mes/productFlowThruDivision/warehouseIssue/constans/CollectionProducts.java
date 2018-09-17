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
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans;

public enum CollectionProducts {

    ON_ORDER("01onOrder"), ON_OPERATION("02onOperation"), ON_DIVISION("03onDivision");

    private final String collection;

    private CollectionProducts(final String collection) {
        this.collection = collection;
    }

    public String getStringValue() {
        return collection;
    }

    public static CollectionProducts fromStringValue(String code) {
        for (CollectionProducts collectionProducts : values()) {
            if (collectionProducts.getStringValue().equals(code)) {
                return collectionProducts;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown code: '%s' from enum: 'CollectionProducts'", code));
    }

}
