package com.qcadoo.mes.productFlowThruDivision.deliveries.helpers;

import com.qcadoo.model.api.Entity;

public class DeliveredProductReservationObject {

    private Entity deliveredProduct;

    private Entity reservationForDeliveredProduct;

    public DeliveredProductReservationObject(Entity deliveredProduct, Entity reservationForDeliveredProduct) {
        this.deliveredProduct = deliveredProduct;
        this.reservationForDeliveredProduct = reservationForDeliveredProduct;
    }

    public Entity getDeliveredProduct() {
        return deliveredProduct;
    }

    public void setDeliveredProduct(Entity deliveredProduct) {
        this.deliveredProduct = deliveredProduct;
    }

    public Entity getReservationForDeliveredProduct() {
        return reservationForDeliveredProduct;
    }

    public void setReservationForDeliveredProduct(Entity reservationForDeliveredProduct) {
        this.reservationForDeliveredProduct = reservationForDeliveredProduct;
    }
}
