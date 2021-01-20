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

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableList;
import com.qcadoo.mes.technologies.domain.TechnologyId;

public class TechnologyStructureNode {

    private final ProductInfo productInfo;

    private final TechnologyStructureNodeType type;

    private final List<TechnologyStructureNode> children;

    public TechnologyStructureNode(final ProductInfo productInfo, final TechnologyStructureNodeType type,
            final List<TechnologyStructureNode> children) {
        this.productInfo = productInfo;
        this.type = type;
        this.children = ImmutableList.copyOf(children);
    }

    public TechnologyStructureNode withOriginalTechnology(final Optional<TechnologyId> originalTechnology) {
        return new TechnologyStructureNode(productInfo.withOriginalProductTechnology(originalTechnology), type, children);
    }

    public TechnologyStructureNode withProductTechnology(final Optional<TechnologyId> newProductTechnology) {
        return new TechnologyStructureNode(productInfo.withProductTechnology(newProductTechnology), type, children);
    }

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public TechnologyStructureNodeType getType() {
        return type;
    }

    public List<TechnologyStructureNode> getChildren() {
        return children;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        TechnologyStructureNode rhs = (TechnologyStructureNode) obj;

        return new EqualsBuilder().append(this.productInfo, rhs.productInfo).append(this.type, rhs.type).append(this.children,
                rhs.children).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(productInfo).append(type).toHashCode();
    }

}
