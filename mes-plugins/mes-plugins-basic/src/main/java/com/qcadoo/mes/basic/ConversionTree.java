package com.qcadoo.mes.basic;

import java.math.BigDecimal;

public class ConversionTree {

    private String unitTo;

    private String unitFrom;

    private BigDecimal quantityFrom;

    private BigDecimal quantityTo;

    private ConversionTree parent;

    public String getUnitTo() {
        return unitTo;
    }

    public void setUnitTo(final String unitTo) {
        this.unitTo = unitTo;
    }

    public String getUnitFrom() {
        return unitFrom;
    }

    public void setUnitFrom(final String unitFrom) {
        this.unitFrom = unitFrom;
    }

    public BigDecimal getQuantityFrom() {
        return quantityFrom;
    }

    public void setQuantityFrom(final BigDecimal quantityFrom) {
        this.quantityFrom = quantityFrom;
    }

    public BigDecimal getQuantityTo() {
        return quantityTo;
    }

    public void setQuantityTo(final BigDecimal quantityTo) {
        this.quantityTo = quantityTo;
    }

    public ConversionTree getParent() {
        return parent;
    }

    public void setParent(final ConversionTree parent) {
        this.parent = parent;
    }

}
