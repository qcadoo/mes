package com.qcadoo.mes.productFlowThruDivision.print;

import java.math.BigDecimal;
import java.util.Set;

class QuantityNormDescriptions {

    private BigDecimal neededQuantity;

    private Set<String> descriptions;

    private BigDecimal norm;

    private BigDecimal materialUnitCost;

    private BigDecimal currentStock;

    private Set<Long> warehouseIds;

    private Integer priority;

    private String nodeNumber;

    public Set<Long> getWarehouseIds() {
        return warehouseIds;
    }

    public void setWarehouseIds(Set<Long> warehouseIds) {
        this.warehouseIds = warehouseIds;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getNeededQuantity() {
        return neededQuantity;
    }

    public void setNeededQuantity(BigDecimal neededQuantity) {
        this.neededQuantity = neededQuantity;
    }

    public Set<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Set<String> descriptions) {
        this.descriptions = descriptions;
    }

    public BigDecimal getNorm() {
        return norm;
    }

    public void setNorm(BigDecimal norm) {
        this.norm = norm;
    }

    public BigDecimal getMaterialUnitCost() {
        return materialUnitCost;
    }

    public void setMaterialUnitCost(BigDecimal materialUnitCost) {
        this.materialUnitCost = materialUnitCost;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(String nodeNumber) {
        this.nodeNumber = nodeNumber;
    }
}
