package com.qcadoo.mes.costCalculation.print.dto;

import java.math.BigDecimal;

public class CostCalculationMaterialBySize {

    private String technologyNumber;

    private String productNumber;

    private String technologyInputProductType;

    private String sizeGroupNumber;

    private String unit;

    private BigDecimal quantity;

    private Long materialId;

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

    public String getTechnologyInputProductType() {
        return technologyInputProductType;
    }

    public void setTechnologyInputProductType(String technologyInputProductType) {
        this.technologyInputProductType = technologyInputProductType;
    }

    public String getSizeGroupNumber() {
        return sizeGroupNumber;
    }

    public void setSizeGroupNumber(String sizeGroupNumber) {
        this.sizeGroupNumber = sizeGroupNumber;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }


    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }
}
