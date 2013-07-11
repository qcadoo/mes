package com.qcadoo.mes.technologies.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ProductQuantitiesHolder {

    private final Map<Long, BigDecimal> operationRuns;

    private final Set<OperationProductComponentHolder> nonComponents;

    private final Map<OperationProductComponentHolder, BigDecimal> productQuantities;

    public ProductQuantitiesHolder() {
        this.operationRuns = Maps.newHashMap();
        this.nonComponents = Sets.newHashSet();
        this.productQuantities = Maps.newHashMap();
    }

    @Deprecated
    public ProductQuantitiesHolder(final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents,
            final Map<OperationProductComponentHolder, BigDecimal> productQuantities) {
        this.operationRuns = Maps.newHashMap(operationRuns);
        this.nonComponents = Sets.newHashSet(nonComponents);
        this.productQuantities = Maps.newHashMap(productQuantities);
    }

    public Map<Long, BigDecimal> getOperationRuns() {
        return operationRuns;
    }

    public Map<OperationProductComponentHolder, BigDecimal> getProductQuantities() {
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

        if (obj == null || !(obj instanceof ProductQuantitiesHolder)) {
            return false;
        }

        ProductQuantitiesHolder other = (ProductQuantitiesHolder) obj;

        return new EqualsBuilder().append(operationRuns, other.operationRuns).append(productQuantities, other.productQuantities)
                .isEquals();
    }

}
