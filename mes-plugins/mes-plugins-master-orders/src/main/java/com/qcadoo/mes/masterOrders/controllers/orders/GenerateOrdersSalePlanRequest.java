package com.qcadoo.mes.masterOrders.controllers.orders;

import com.google.common.collect.Lists;

import java.util.List;

public class GenerateOrdersSalePlanRequest {

    private Long entityId;

    private List<OrderSalePlanPosition> positions = Lists.newArrayList();

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public List<OrderSalePlanPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<OrderSalePlanPosition> positions) {
        this.positions = positions;
    }
}
