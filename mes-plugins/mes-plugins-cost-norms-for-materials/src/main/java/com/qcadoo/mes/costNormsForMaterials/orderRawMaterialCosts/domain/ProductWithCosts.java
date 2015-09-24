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
package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Function;

public class ProductWithCosts {

    public static final Function<ProductWithCosts, Long> EXTRACT_ID = new Function<ProductWithCosts, Long>() {

        @Override
        public Long apply(final ProductWithCosts productWithCosts) {
            return productWithCosts.getProductId();
        }
    };

    private final Long productId;

    private final BigDecimal costForNumber;

    private final BigDecimal nominalCost;

    private final BigDecimal lastPurchaseCost;

    private final BigDecimal averageCost;

    public ProductWithCosts(final Long productId, final BigDecimal costForNumber, final BigDecimal nominalCost,
            final BigDecimal lastPurchaseCost, final BigDecimal averageCost) {
        this.productId = productId;
        this.costForNumber = costForNumber;
        this.nominalCost = nominalCost;
        this.lastPurchaseCost = lastPurchaseCost;
        this.averageCost = averageCost;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getCostForNumber() {
        return costForNumber;
    }

    public BigDecimal getNominalCost() {
        return nominalCost;
    }

    public BigDecimal getLastPurchaseCost() {
        return lastPurchaseCost;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
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
        ProductWithCosts rhs = (ProductWithCosts) obj;
        return new EqualsBuilder().append(this.productId, rhs.productId).append(this.costForNumber, rhs.costForNumber)
                .append(this.nominalCost, rhs.nominalCost).append(this.lastPurchaseCost, rhs.lastPurchaseCost)
                .append(this.averageCost, rhs.averageCost).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(productId).append(costForNumber).append(nominalCost).append(lastPurchaseCost)
                .append(averageCost).toHashCode();
    }
}
