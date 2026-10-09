package com.qcadoo.mes.materialFlowResources.controllers;

import com.google.common.collect.Lists;

import java.util.List;

public class ProductsWithPriceRequest {

    private Long entityId;

    private List<ProductWithPricePosition> positions = Lists.newArrayList();

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public List<ProductWithPricePosition> getPositions() {
        return positions;
    }

    public void setPositions(List<ProductWithPricePosition> positions) {
        this.positions = positions;
    }
}
