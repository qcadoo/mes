package com.qcadoo.mes.orders.controllers.requests;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.controllers.dto.TechnologyOperationDto;
import com.qcadoo.mes.technologies.controller.dataProvider.MaterialDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderCreationRequest {

    private Long productId;

    private BigDecimal quantity;

    private String description;

    private String typeOfProductionRecording;

    private Long technologyId;

    private Long productionLineId;

    private Date startDate;

    private Date finishDate;

    private List<MaterialDto> materials = Lists.newArrayList();

    private List<TechnologyOperationDto> technologyOperations = Lists.newArrayList();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTechnologyId() {
        return technologyId;
    }

    public void setTechnologyId(Long technologyId) {
        this.technologyId = technologyId;
    }

    public Long getProductionLineId() {
        return productionLineId;
    }

    public void setProductionLineId(Long productionLineId) {
        this.productionLineId = productionLineId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public List<MaterialDto> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialDto> materials) {
        this.materials = materials;
    }

    public String getTypeOfProductionRecording() {
        return typeOfProductionRecording;
    }

    public void setTypeOfProductionRecording(String typeOfProductionRecording) {
        this.typeOfProductionRecording = typeOfProductionRecording;
    }

    public List<TechnologyOperationDto> getTechnologyOperations() {
        return technologyOperations;
    }

    public void setTechnologyOperations(List<TechnologyOperationDto> technologyOperations) {
        this.technologyOperations = technologyOperations;
    }
}
