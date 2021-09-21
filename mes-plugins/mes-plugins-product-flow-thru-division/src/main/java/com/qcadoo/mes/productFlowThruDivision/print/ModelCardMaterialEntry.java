package com.qcadoo.mes.productFlowThruDivision.print;

import java.math.BigDecimal;
import java.util.Objects;

class ModelCardMaterialEntry {

    private Long id;

    private String number;

    private String name;

    private BigDecimal norm;

    private BigDecimal price;

    private BigDecimal materialUnitCost;

    private BigDecimal neededQuantity;

    private BigDecimal currentStock;

    private String unit;

    private Long technologyInputProductTypeId;

    private String technologyInputProductTypeName;

    private String description;

    private Long warehouseId;

    private Long sizeGroupId;

    private String sizeGroupNumber;

    private Long parentId;

    private Integer priority;

    private String nodeNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getNorm() {
        return norm;
    }

    public void setNorm(BigDecimal norm) {
        this.norm = norm;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getTechnologyInputProductTypeId() {
        return technologyInputProductTypeId;
    }

    public void setTechnologyInputProductTypeId(Long technologyInputProductTypeId) {
        this.technologyInputProductTypeId = technologyInputProductTypeId;
    }

    public String getTechnologyInputProductTypeName() {
        return technologyInputProductTypeName;
    }

    public void setTechnologyInputProductTypeName(String technologyInputProductTypeName) {
        this.technologyInputProductTypeName = technologyInputProductTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getMaterialUnitCost() {
        return materialUnitCost;
    }

    public void setMaterialUnitCost(BigDecimal materialUnitCost) {
        this.materialUnitCost = materialUnitCost;
    }

    public BigDecimal getNeededQuantity() {
        return neededQuantity;
    }

    public void setNeededQuantity(BigDecimal neededQuantity) {
        this.neededQuantity = neededQuantity;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getSizeGroupId() {
        return sizeGroupId;
    }

    public void setSizeGroupId(Long sizeGroupId) {
        this.sizeGroupId = sizeGroupId;
    }

    public String getSizeGroupNumber() {
        return sizeGroupNumber;
    }

    public void setSizeGroupNumber(String sizeGroupNumber) {
        this.sizeGroupNumber = sizeGroupNumber;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ModelCardMaterialEntry that = (ModelCardMaterialEntry) o;
        return Objects.equals(id, that.id) && Objects.equals(technologyInputProductTypeId, that.technologyInputProductTypeId)
                && Objects.equals(sizeGroupId, that.sizeGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, technologyInputProductTypeId, sizeGroupId);
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
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
