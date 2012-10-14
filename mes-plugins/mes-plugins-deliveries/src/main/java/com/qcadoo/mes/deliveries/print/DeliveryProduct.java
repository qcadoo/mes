package com.qcadoo.mes.deliveries.print;

public class DeliveryProduct {

    private Long deliveredProductId;

    private Long orderedProductId;

    public Long getDeliveredProductId() {
        return deliveredProductId;
    }

    public void setDeliveredProductId(final Long deliveredProductId) {
        this.deliveredProductId = deliveredProductId;
    }

    public Long getOrderedProductId() {
        return orderedProductId;
    }

    public void setOrderedProductId(final Long orderedProductId) {
        this.orderedProductId = orderedProductId;
    }

}
