package com.qcadoo.mes.technologies.controller.dataProvider;

import java.math.BigDecimal;

public class MaterialDto {

    private Long productInId;

    private Long productId;

    private Long index;

    private String product;

    private String productNumber;

    private String productName;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal quantityPerUnit;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(BigDecimal quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }

    public Long getProductInId() {
        return productInId;
    }

    public void setProductInId(Long productInId) {
        this.productInId = productInId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
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

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }
}
