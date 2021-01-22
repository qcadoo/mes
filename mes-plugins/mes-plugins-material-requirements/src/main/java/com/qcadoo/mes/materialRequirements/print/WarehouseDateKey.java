package com.qcadoo.mes.materialRequirements.print;

import java.util.Objects;

public class WarehouseDateKey {

    private Long warehouseId = 0L;

    private String warehouseNumber = "";

    private Long date = 0L;

    public WarehouseDateKey(MaterialRequirementEntry mr, boolean includeWarehouse, boolean includeStartDateOrder) {
        if (includeWarehouse && Objects.nonNull(mr.getWarehouseId())) {
            this.warehouseId = mr.getWarehouseId();
            this.warehouseNumber = mr.getWarehouseNumber();
        }
        if (includeStartDateOrder && Objects.nonNull(mr.getOrderStartDate())) {
            this.date = mr.getOrderStartDate();
        }
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WarehouseDateKey))
            return false;
        WarehouseDateKey that = (WarehouseDateKey) o;
        return Objects.equals(warehouseId, that.warehouseId) && Objects.equals(warehouseNumber, that.warehouseNumber)
                && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouseId, warehouseNumber, date);
    }
}
