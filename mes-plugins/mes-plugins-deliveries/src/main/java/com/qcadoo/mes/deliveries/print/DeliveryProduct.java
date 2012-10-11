package com.qcadoo.mes.deliveries.print;


public class DeliveryProduct {

    private Long deliveredProductId;

    private Long orderedProductId;

    public Long getDeliveredProductId() {
        return deliveredProductId;
    }

    public void setDeliveredProductId(Long deliveredProductId) {
        this.deliveredProductId = deliveredProductId;
    }

    public Long getOrderedProductId() {
        return orderedProductId;
    }

    public void setOrderedProductId(Long orderedProductId) {
        this.orderedProductId = orderedProductId;
    }

}
