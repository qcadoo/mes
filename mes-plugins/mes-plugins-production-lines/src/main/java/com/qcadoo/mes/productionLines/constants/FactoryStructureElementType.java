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
package com.qcadoo.mes.productionLines.constants;

import com.qcadoo.model.api.Entity;

public enum FactoryStructureElementType {
    COMPANY("company"), FACTORY("factory"), DIVISION("division"), PRODUCTION_LINE("productionLine"), WORKSTATION("workstation"), SUBASSEMBLY(
            "subassembly");

    private final String elementType;

    private FactoryStructureElementType(final String elementType) {
        this.elementType = elementType;
    }

    public String getStringValue() {
        return elementType;
    }

    public static FactoryStructureElementType of(final Entity factoryStructureElement) {
        return parseString(factoryStructureElement.getStringField(FactoryStructureElementFields.ENTITY_TYPE));
    }

    public static FactoryStructureElementType parseString(final String elementType) {
        if ("company".equals(elementType)) {
            return COMPANY;
        } else if ("factory".equals(elementType)) {
            return FACTORY;
        } else if ("division".equals(elementType)) {
            return DIVISION;
        } else if ("productionLine".equals(elementType)) {
            return PRODUCTION_LINE;
        } else if ("workstation".equals(elementType)) {
            return WORKSTATION;
        } else if ("subassembly".equals(elementType)) {
            return SUBASSEMBLY;
        }
        throw new IllegalStateException("Unsupported FactoryStructureElementType attribute: " + elementType);
    }
}
