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
package com.qcadoo.mes.technologies.constants;

public enum MrpAlgorithm {

    ONLY_COMPONENTS("01onlyComponents"), ALL_PRODUCTS_IN("02allProductsIn"), COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS(
            "03componentsAndSubcontractorsProducts");

    private final String typeOfProductionRecording;

    private MrpAlgorithm(final String algoritmOfMaterialRequirements) {
        this.typeOfProductionRecording = algoritmOfMaterialRequirements;
    }

    public String getStringValue() {
        return typeOfProductionRecording;
    }

    public static MrpAlgorithm parseString(final String string) {
        if ("01onlyComponents".equals(string)) {
            return ONLY_COMPONENTS;
        } else if ("02allProductsIn".equals(string)) {
            return ALL_PRODUCTS_IN;
        } else if ("03componentsAndSubcontractorsProducts".equals(string)) {
            return COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS;
        }

        throw new IllegalStateException("Unsupported mrpAlgorithm attribute: " + string);
    }

}
