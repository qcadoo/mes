package com.qcadoo.mes.deliveries.helpers;

import com.google.common.base.Objects;

import java.util.Date;

public class DeliveredMultiProduct {

    private final Long productId;
    private final Long additionalCodeId;
    private final Date expirationDate;

    public DeliveredMultiProduct(Long productId, Long additionalCodeId, Date expirationDate) {
        this.productId = productId;
        this.additionalCodeId = additionalCodeId;
        this.expirationDate = expirationDate;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getAdditionalCodeId() {
        return additionalCodeId;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DeliveredMultiProduct that = (DeliveredMultiProduct) o;
        return Objects.equal(productId, that.productId) &&
                Objects.equal(additionalCodeId, that.additionalCodeId) &&
                Objects.equal(expirationDate, that.expirationDate);
    }

    @Override public int hashCode() {
        return Objects.hashCode(productId, additionalCodeId, expirationDate);
    }
}
