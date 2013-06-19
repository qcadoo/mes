package com.qcadoo.mes.technologies.dto;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.collect.Maps;

public class ProductQuantitiesAndOperationRuns {

    private final Map<Long, BigDecimal> operationRuns;

    private final Map<Long, BigDecimal> productQuantities;

    public ProductQuantitiesAndOperationRuns() {
        this.operationRuns = Maps.newHashMap();
        this.productQuantities = Maps.newHashMap();
    }

    @Deprecated
    public ProductQuantitiesAndOperationRuns(final Map<Long, BigDecimal> operationRuns,
            final Map<Long, BigDecimal> productQuantities) {
        this.operationRuns = Maps.newHashMap(operationRuns);
        this.productQuantities = Maps.newHashMap(productQuantities);
    }

    public Map<Long, BigDecimal> getOperationRuns() {
        return operationRuns;
    }

    public Map<Long, BigDecimal> getProductQuantities() {
        return productQuantities;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(operationRuns).append(productQuantities).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ProductQuantitiesAndOperationRuns)) {
            return false;
        }
        ProductQuantitiesAndOperationRuns other = (ProductQuantitiesAndOperationRuns) obj;
        return new EqualsBuilder().append(operationRuns, other.operationRuns).append(productQuantities, other.productQuantities)
                .isEquals();
    }

}
