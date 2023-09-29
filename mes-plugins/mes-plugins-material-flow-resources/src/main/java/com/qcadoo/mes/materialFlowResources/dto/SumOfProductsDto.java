package com.qcadoo.mes.materialFlowResources.dto;

import java.math.BigDecimal;

public class SumOfProductsDto {
    private BigDecimal quantitySum;
    private BigDecimal additionalQuantitySum;

    public BigDecimal getQuantitySum() {
        return quantitySum;
    }

    public void setQuantitySum(BigDecimal quantitySum) {
        this.quantitySum = quantitySum;
    }

    public BigDecimal getAdditionalQuantitySum() {
        return additionalQuantitySum;
    }

    public void setAdditionalQuantitySum(BigDecimal additionalQuantitySum) {
        this.additionalQuantitySum = additionalQuantitySum;
    }
}
