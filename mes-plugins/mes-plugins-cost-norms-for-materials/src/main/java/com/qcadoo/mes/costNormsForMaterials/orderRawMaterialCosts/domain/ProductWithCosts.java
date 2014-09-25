package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
