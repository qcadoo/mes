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
