package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class MachinePartForEventDTO {

    private Long machinePartId;
    private String machinePartName;
    private String machinePartNumber;
    private String machinePartUnit;
    private BigDecimal machinePartPlannedQuantity;
    private BigDecimal value;
    private BigDecimal lastPurchaseCost;
    private BigDecimal priceFromDocumentPosition;
    private BigDecimal priceFromPosition;
    private BigDecimal quantityFromPosition;

    public Long getMachinePartId() {
        return machinePartId;
    }

    public void setMachinePartId(Long machinePartId) {
        this.machinePartId = machinePartId;
    }

    public String getMachinePartName() {
        return machinePartName;
    }

    public void setMachinePartName(String machinePartName) {
        this.machinePartName = machinePartName;
    }

    public String getMachinePartNumber() {
        return machinePartNumber;
    }

    public void setMachinePartNumber(String machinePartNumber) {
        this.machinePartNumber = machinePartNumber;
    }

    public String getMachinePartUnit() {
        return machinePartUnit;
    }

    public void setMachinePartUnit(String machinePartUnit) {
        this.machinePartUnit = machinePartUnit;
    }

    public BigDecimal getMachinePartPlannedQuantity() {
        return machinePartPlannedQuantity;
    }

    public void setMachinePartPlannedQuantity(BigDecimal machinePartPlannedQuantity) {
        this.machinePartPlannedQuantity = machinePartPlannedQuantity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
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
        MachinePartForEventDTO that = (MachinePartForEventDTO) o;
        return Objects.equal(machinePartId, that.machinePartId)
                && Objects.equal(machinePartName, that.machinePartName)
                && Objects.equal(machinePartNumber, that.machinePartNumber)
                && Objects.equal(machinePartUnit, that.machinePartUnit)
                && Objects.equal(machinePartPlannedQuantity, that.machinePartPlannedQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(machinePartId, machinePartName, machinePartNumber, machinePartUnit, machinePartPlannedQuantity);
    }
}
