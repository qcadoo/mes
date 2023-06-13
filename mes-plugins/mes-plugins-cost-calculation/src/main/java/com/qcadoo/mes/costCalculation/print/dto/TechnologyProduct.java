package com.qcadoo.mes.costCalculation.print.dto;

import java.math.BigDecimal;

public class TechnologyProduct {

    private String technologyNumber;

    private String productNumber;

    private String productName;

    private String unit;

    private BigDecimal productQuantity;

    private String productType;

    public String getTechnologyNumber() {
        return technologyNumber;
    }

    public void setTechnologyNumber(String technologyNumber) {
        this.technologyNumber = technologyNumber;
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

    public BigDecimal getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(BigDecimal productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }
}
