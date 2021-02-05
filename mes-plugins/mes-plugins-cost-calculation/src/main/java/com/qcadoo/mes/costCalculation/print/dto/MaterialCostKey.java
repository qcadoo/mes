package com.qcadoo.mes.costCalculation.print.dto;

import java.util.Objects;

public class MaterialCostKey {

    private String productNumber;

    private String productName;

    private String unit;

    private String technologyInputProductType;

    public MaterialCostKey(String productNumber, String productName, String unit, String technologyInputProductType) {
        this.productNumber = productNumber;
        this.productName = productName;
        this.unit = unit;
        this.technologyInputProductType = technologyInputProductType;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTechnologyInputProductType() {
        return technologyInputProductType;
    }

    public void setTechnologyInputProductType(String technologyInputProductType) {
        this.technologyInputProductType = technologyInputProductType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MaterialCostKey that = (MaterialCostKey) o;
        return productNumber.equals(that.productNumber) && productName.equals(that.productName) && unit.equals(that.unit)
                && technologyInputProductType.equals(that.technologyInputProductType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productNumber, productName, unit, technologyInputProductType);
    }
}
