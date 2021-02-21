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
package com.qcadoo.mes.technologiesGenerator.domain;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.model.api.Entity;

public enum TechnologyStructureNodeType {

    INTERMEDIATE("intermediate") {
    },
    COMPONENT("component") {
    },
    MATERIAL("material") {
    },
    CUSTOMIZED_COMPONENT("customizedComponent") {
    },
    PRODUCT_BY_SIZE_GROUP("productBySizeGroup") {
    };

    private final String stringValue;

    public static TechnologyStructureNodeType of(final Entity entity) {
        return parseString(entity.getStringField(GeneratorTreeNodeFields.ENTITY_TYPE));
    }

    private TechnologyStructureNodeType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static TechnologyStructureNodeType parseString(final String stringValue) {
        for (TechnologyStructureNodeType technologyStructureNodeType : values()) {
            if (StringUtils.equalsIgnoreCase(stringValue, technologyStructureNodeType.getStringValue())) {
                return technologyStructureNodeType;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot parse TechnologyStructureNodeType from '%s'", stringValue));
    }

    public static TechnologyStructureNodeType resolveFor(final ProductInfo productInfo) {
        if (productInfo.isIntermediate()) {
            if (productInfo.hasParent()) {
                return INTERMEDIATE;
            } else {
                return resolveComponentType(productInfo);
            }
        }
        if (productInfo.getProductTechnology().isPresent()) {
            return resolveComponentType(productInfo);
        }
        return MATERIAL;
    }

    private static TechnologyStructureNodeType resolveComponentType(final ProductInfo productInfo) {
        if (productInfo.getProductTechnology().isPresent()
                && Objects.equals(productInfo.getProductTechnology(), productInfo.getOriginalTechnology())) {
            return COMPONENT;
        }
        return CUSTOMIZED_COMPONENT;
    }

}
