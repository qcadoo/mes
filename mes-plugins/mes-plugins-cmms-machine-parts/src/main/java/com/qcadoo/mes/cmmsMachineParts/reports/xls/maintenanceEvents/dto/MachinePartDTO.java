package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class MachinePartDTO {

    private Long machinePartId;
    private String partNumber;
    private String partName;
    private String warehouseNumber;
    private BigDecimal partPlannedQuantity;
    private BigDecimal value;
    private BigDecimal lastPurchaseCost;
    private BigDecimal priceFromDocumentPosition;
    private BigDecimal priceFromPosition;
    private BigDecimal quantityFromPosition;
    private String partUnit;

    public Long getMachinePartId() {
        return machinePartId;
    }

    public void setMachinePartId(Long machinePartId) {
        this.machinePartId = machinePartId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public BigDecimal getPartPlannedQuantity() {
        return partPlannedQuantity;
    }

    public void setPartPlannedQuantity(BigDecimal partPlannedQuantity) {
        this.partPlannedQuantity = partPlannedQuantity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getPartUnit() {
        return partUnit;
    }

    public void setPartUnit(String partUnit) {
        this.partUnit = partUnit;
    }

    public BigDecimal getLastPurchaseCost() {
        return lastPurchaseCost;
    }

    public void setLastPurchaseCost(BigDecimal lastPurchaseCost) {
        this.lastPurchaseCost = lastPurchaseCost;
    }

    public BigDecimal getPriceFromDocumentPosition() {
        return priceFromDocumentPosition;
    }

    public void setPriceFromDocumentPosition(BigDecimal priceFromDocumentPosition) {
        this.priceFromDocumentPosition = priceFromDocumentPosition;
    }

    public BigDecimal getPriceFromPosition() {
        return priceFromPosition;
    }

    public void setPriceFromPosition(BigDecimal priceFromPosition) {
        this.priceFromPosition = priceFromPosition;
    }

    public BigDecimal getQuantityFromPosition() {
        return quantityFromPosition;
    }

    public void setQuantityFromPosition(BigDecimal quantityFromPosition) {
        this.quantityFromPosition = quantityFromPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MachinePartDTO that = (MachinePartDTO) o;
        return Objects.equal(machinePartId, that.machinePartId)
                && Objects.equal(partNumber, that.partNumber)
                && Objects.equal(partName, that.partName)
                && Objects.equal(warehouseNumber, that.warehouseNumber)
                && Objects.equal(partPlannedQuantity, that.partPlannedQuantity)
                && Objects.equal(value, that.value)
                && Objects.equal(partUnit, that.partUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(machinePartId, partNumber, partName, warehouseNumber, partPlannedQuantity, value, partUnit);
    }
}
