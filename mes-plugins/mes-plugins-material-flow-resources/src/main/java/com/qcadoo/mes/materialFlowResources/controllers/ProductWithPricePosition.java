package com.qcadoo.mes.materialFlowResources.controllers;

import java.math.BigDecimal;

public class ProductWithPricePosition {

    private Long id;

    private BigDecimal value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
