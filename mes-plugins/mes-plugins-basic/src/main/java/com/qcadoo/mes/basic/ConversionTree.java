package com.qcadoo.mes.basic;

import java.math.BigDecimal;

public class ConversionTree {

    public String unitTo;

    public String unitFrom;

    public BigDecimal quantityFrom;

    public BigDecimal quantityTo;

    public String getUnitTo() {
        return unitTo;
    }

    public void setUnitTo(String unitTo) {
        this.unitTo = unitTo;
    }

    public String getUnitFrom() {
        return unitFrom;
    }

    public void setUnitFrom(String unitFrom) {
        this.unitFrom = unitFrom;
    }

    public BigDecimal getQuantityFrom() {
        return quantityFrom;
    }

    public void setQuantityFrom(BigDecimal quantityFrom) {
        this.quantityFrom = quantityFrom;
    }

    public BigDecimal getQuantityTo() {
        return quantityTo;
    }

    public void setQuantityTo(BigDecimal quantityTo) {
        this.quantityTo = quantityTo;
    }

    public ConversionTree getParent() {
        return parent;
    }

    public void setParent(ConversionTree parent) {
        this.parent = parent;
    }

    public ConversionTree parent;

    // public ConversionTree child;

}
