package com.qcadoo.mes.productionPerShift.domain;

import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.Date;

public class PpsParameters {

    private Entity productionPerShift;
    private Entity order;
    private Date orderStartDate;
    private BigDecimal plannedQuantity;

    public Entity getProductionPerShift() {
        return productionPerShift;
    }

    public void setProductionPerShift(Entity productionPerShift) {
        this.productionPerShift = productionPerShift;
    }

    public Entity getOrder() {
        return order;
    }

    public void setOrder(Entity order) {
        this.order = order;
    }

    public Date getOrderStartDate() {
        return orderStartDate;
    }

    public void setOrderStartDate(Date orderStartDate) {
        this.orderStartDate = orderStartDate;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }
}
