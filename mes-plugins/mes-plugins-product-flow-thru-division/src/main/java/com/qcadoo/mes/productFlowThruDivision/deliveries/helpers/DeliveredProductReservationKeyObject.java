package com.qcadoo.mes.productFlowThruDivision.deliveries.helpers;

import com.google.common.base.Objects;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;

public class DeliveredProductReservationKeyObject {

    private final Long productId;

    private final Entity product;

    private final BigDecimal conversion;

    public DeliveredProductReservationKeyObject(Entity product, BigDecimal conversion) {
        this.productId = toId(product);
        this.product = product;
        this.conversion = conversion;
    }

    private Long toId(Entity entity) {
        if (entity == null) {
            return null;
        }
        return entity.getId();
    }

    public Long getProductId() {
        return productId;
    }

    public Entity getProduct() {
        return product;
    }

    public BigDecimal getConversion() {
        return conversion;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DeliveredProductReservationKeyObject that = (DeliveredProductReservationKeyObject) o;
        return Objects.equal(productId, that.productId) &&
                Objects.equal(conversion, that.conversion);
    }

    @Override public int hashCode() {
        return Objects.hashCode(productId, conversion);
    }
}
