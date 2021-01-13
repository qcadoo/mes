package com.qcadoo.mes.materialRequirements.print;

import java.math.BigDecimal;

public class MaterialRequirementEntry {

    private Long warehouseId;

    private String warehouseNumber;

    private Long orderStartDate;

    private Long id;

    private String number;

    private String name;

    private BigDecimal plannedQuantity;

    private String unit;

    private BigDecimal stockStatus;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(BigDecimal stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public Long getOrderStartDate() {
        return orderStartDate;
    }

    public void setOrderStartDate(Long orderStartDate) {
        this.orderStartDate = orderStartDate;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
