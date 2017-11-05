package com.qcadoo.mes.materialFlowResources.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourceStockDto {

    private BigDecimal quantity;

    private BigDecimal availableQuantity;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceStockDto that = (ResourceStockDto) o;
        return Objects.equals(quantity, that.quantity) &&
                Objects.equals(availableQuantity, that.availableQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, availableQuantity);
    }
}
