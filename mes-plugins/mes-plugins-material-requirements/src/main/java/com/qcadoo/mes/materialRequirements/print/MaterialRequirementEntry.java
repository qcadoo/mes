package com.qcadoo.mes.materialRequirements.print;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.qcadoo.model.api.Entity;

public class MaterialRequirementEntry {

    private Long id;

    private String number;

    private String name;

    private BigDecimal plannedQuantity;

    private String unit;

    private Long warehouseId;

    private String warehouseNumber;

    private Long orderStartDate;

    private Entity product;

    private Entity warehouse;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(final BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(final Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(final String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public Long getOrderStartDate() {
        return orderStartDate;
    }

    public void setOrderStartDate(final Long orderStartDate) {
        this.orderStartDate = orderStartDate;
    }

    public Entity getProduct() {
        return product;
    }

    public void setProduct(final Entity product) {
        this.product = product;
    }

    public Entity getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(final Entity warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MaterialRequirementEntry that = (MaterialRequirementEntry) o;

        return new EqualsBuilder().append(id, that.id).append(number, that.number).append(name, that.name)
                .append(plannedQuantity, that.plannedQuantity).append(unit, that.unit).append(warehouseId, that.warehouseId)
                .append(warehouseNumber, that.warehouseNumber).append(orderStartDate, that.orderStartDate).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(number).append(name).append(plannedQuantity).append(unit)
                .append(warehouseId).append(warehouseNumber).append(orderStartDate).toHashCode();
    }

}
