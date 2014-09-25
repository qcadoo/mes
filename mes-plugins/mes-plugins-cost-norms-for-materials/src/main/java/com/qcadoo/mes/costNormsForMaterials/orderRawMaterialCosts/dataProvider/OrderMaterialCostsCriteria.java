package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchProjection;

public final class OrderMaterialCostsCriteria {

    public static final String PRODUCT_ALIAS = "product_alias";

    private final Long orderId;

    private Optional<SearchProjection> searchProjection = Optional.absent();

    private Optional<SearchCriterion> productCriteria = Optional.absent();

    private Optional<SearchOrder> searchOrder = Optional.absent();

    public static OrderMaterialCostsCriteria forOrder(final Long orderId) {
        return new OrderMaterialCostsCriteria(orderId);
    }

    private OrderMaterialCostsCriteria(final Long orderId) {
        Preconditions.checkArgument(orderId != null, "Order id cannot be null.");
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Optional<SearchProjection> getSearchProjection() {
        return searchProjection;
    }

    public OrderMaterialCostsCriteria setSearchProjection(final SearchProjection searchProjection) {
        this.searchProjection = Optional.fromNullable(searchProjection);
        return this;
    }

    public OrderMaterialCostsCriteria setProductCriteria(final SearchCriterion productCriteria) {
        this.productCriteria = Optional.fromNullable(productCriteria);
        return this;
    }

    public Optional<SearchCriterion> getProductCriteria() {
        return productCriteria;
    }

    public Optional<SearchOrder> getSearchOrder() {
        return searchOrder;
    }

    public OrderMaterialCostsCriteria setSearchOrder(final SearchOrder searchOrder) {
        this.searchOrder = Optional.fromNullable(searchOrder);
        return this;
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
        OrderMaterialCostsCriteria rhs = (OrderMaterialCostsCriteria) obj;
        return new EqualsBuilder().append(this.orderId, rhs.orderId).append(this.searchProjection, rhs.searchProjection)
                .append(this.productCriteria, rhs.productCriteria).append(this.searchOrder, rhs.searchOrder).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderId).append(searchProjection).append(productCriteria).append(searchOrder)
                .toHashCode();
    }
}
