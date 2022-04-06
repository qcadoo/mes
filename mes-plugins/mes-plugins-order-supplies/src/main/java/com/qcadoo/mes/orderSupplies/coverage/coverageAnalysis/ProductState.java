package com.qcadoo.mes.orderSupplies.coverage.coverageAnalysis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class ProductState {

    private Long orderId;

    private Long productId;

    private Long orderedProductId;

    private String state;

    private Date startDate;

    private BigDecimal reserveMissingQuantity;

    private BigDecimal plannedQuantity;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getReserveMissingQuantity() {
        return reserveMissingQuantity;
    }

    public void setReserveMissingQuantity(BigDecimal reserveMissingQuantity) {
        this.reserveMissingQuantity = reserveMissingQuantity;
    }

    public Long getOrderedProductId() {
        return orderedProductId;
    }

    public void setOrderedProductId(Long orderedProductId) {
        this.orderedProductId = orderedProductId;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductState that = (ProductState) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(productId, that.productId) && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, productId, state);
    }
}
