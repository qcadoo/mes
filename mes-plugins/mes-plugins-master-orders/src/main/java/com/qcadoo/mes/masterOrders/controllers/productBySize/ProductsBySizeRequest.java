package com.qcadoo.mes.masterOrders.controllers.productBySize;

import com.google.common.collect.Lists;

import java.util.List;

public class ProductsBySizeRequest {

    private Long entityId;

    private List<ProductBySizePosition> positions = Lists.newArrayList();

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public List<ProductBySizePosition> getPositions() {
        return positions;
    }

    public void setPositions(List<ProductBySizePosition> positions) {
        this.positions = positions;
    }
}
