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
package com.qcadoo.mes.technologies.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ProductQuantitiesHolder {

    private final Map<OperationProductComponentHolder, BigDecimal> productQuantities;

    private final Map<Long, BigDecimal> operationRuns;

    private final Set<OperationProductComponentHolder> nonComponents;

    public ProductQuantitiesHolder() {
        this.productQuantities = Maps.newHashMap();
        this.operationRuns = Maps.newHashMap();
        this.nonComponents = Sets.newHashSet();
    }

    public ProductQuantitiesHolder(final OperationProductComponentWithQuantityContainer productQuantities,
            final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents) {
        this.productQuantities = Maps.newHashMap(productQuantities.asMap());
        this.operationRuns = Maps.newHashMap(operationRuns);
        this.nonComponents = Sets.newHashSet(nonComponents);
    }

    public ProductQuantitiesHolder(final OperationProductComponentWithQuantityContainer productQuantities,
            final Map<Long, BigDecimal> operationRuns) {
        this.productQuantities = Maps.newHashMap(productQuantities.asMap());
        this.operationRuns = Maps.newHashMap(operationRuns);
        this.nonComponents = Sets.newHashSet();
    }

    public Map<Long, BigDecimal> getOperationRuns() {
        return operationRuns;
    }

    public Map<OperationProductComponentHolder, BigDecimal> getProductQuantities() {
        return productQuantities;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(operationRuns).append(nonComponents).append(productQuantities).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ProductQuantitiesHolder)) {
            return false;
        }

        ProductQuantitiesHolder other = (ProductQuantitiesHolder) obj;

        return new EqualsBuilder().append(operationRuns, other.operationRuns).append(nonComponents, other.nonComponents)
                .append(productQuantities, other.productQuantities).isEquals();
    }

}
