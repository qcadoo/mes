package com.qcadoo.mes.materialRequirements.print;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class WarehouseDateKey {

    private Long warehouseId = 0L;

    private String warehouseNumber = "";

    private Date date = null;

    public WarehouseDateKey(final MaterialRequirementEntry mr, final boolean includeWarehouse,
            final boolean includeStartDateOrder) {
        if (includeWarehouse && Objects.nonNull(mr.getWarehouseId())) {
            this.warehouseId = mr.getWarehouseId();
            this.warehouseNumber = mr.getWarehouseNumber();
        }
        if (includeStartDateOrder && Objects.nonNull(mr.getOrderStartDate())) {
            this.date = new Date(mr.getOrderStartDate().getTime());
        }
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

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        if (Objects.nonNull(date)) {
            this.date = new Date(date.getTime());
        } else {
            this.date = null;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WarehouseDateKey that = (WarehouseDateKey) o;

        return new EqualsBuilder().append(warehouseId, that.warehouseId).append(warehouseNumber, that.warehouseNumber)
                .append(date, that.date).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(warehouseId).append(warehouseNumber).append(date).toHashCode();
    }

}
