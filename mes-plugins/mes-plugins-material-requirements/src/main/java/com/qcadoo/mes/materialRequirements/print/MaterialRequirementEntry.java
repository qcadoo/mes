package com.qcadoo.mes.materialRequirements.print;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MaterialRequirementEntry {

    private Long id;

    private String number;

    private String name;

    private BigDecimal plannedQuantity;

    private String unit;

    private Long warehouseId;

    private String warehouseNumber;

    private Date orderStartDate;

    private Entity product;

    private Entity warehouse;

    private List<Entity> batches = Lists.newArrayList();

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

    public Date getOrderStartDate() {
        return orderStartDate;
    }

    public void setOrderStartDate(final Date orderStartDate) {
        if (Objects.nonNull(orderStartDate)) {
            this.orderStartDate = new Date(orderStartDate.getTime());
        } else {
            this.orderStartDate = null;
        }
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

    public List<Entity> getBatches() {
        return batches;
    }

    public void setBatches(final List<Entity> batches) {
        this.batches = batches;
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
                .append(warehouseId, that.warehouseId).append(warehouseNumber, that.warehouseNumber)
                .append(orderStartDate, that.orderStartDate).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(number).append(name).append(warehouseId).append(warehouseNumber)
                .append(orderStartDate).toHashCode();
    }

}
