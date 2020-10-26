package com.qcadoo.mes.technologies.controller.dataProvider;

import java.math.BigDecimal;

public class OperationMaterialDto {

    private Long productInId;

    private Long tocId;

    private Long operationId;

    private String node;

    private String operationNumber;

    private Long productId;

    private Long index;

    private String product;

    private String productNumber;

    private String productName;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal quantityPerUnit;

    private Long workstationId;

    private String workstationNumber;

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

    public Long getTocId() {
        return tocId;
    }

    public void setTocId(Long tocId) {
        this.tocId = tocId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(Long workstationId) {
        this.workstationId = workstationId;
    }

    public String getWorkstationNumber() {
        return workstationNumber;
    }

    public void setWorkstationNumber(String workstationNumber) {
        this.workstationNumber = workstationNumber;
    }
}
