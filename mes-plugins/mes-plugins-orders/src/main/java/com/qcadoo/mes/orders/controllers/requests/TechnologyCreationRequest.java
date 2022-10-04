package com.qcadoo.mes.orders.controllers.requests;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.controllers.dto.TechnologyOperationDto;
import com.qcadoo.mes.technologies.controller.dataProvider.MaterialDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TechnologyCreationRequest {

    private Long productId;

    private BigDecimal quantity;

    private String description;

    private String typeOfProductionRecording;

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

    public String getTypeOfProductionRecording() {
        return typeOfProductionRecording;
    }

    public void setTypeOfProductionRecording(String typeOfProductionRecording) {
        this.typeOfProductionRecording = typeOfProductionRecording;
    }

    public List<MaterialDto> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialDto> materials) {
        this.materials = materials;
    }

    public List<TechnologyOperationDto> getTechnologyOperations() {
        return technologyOperations;
    }

    public void setTechnologyOperations(List<TechnologyOperationDto> technologyOperations) {
        this.technologyOperations = technologyOperations;
    }
}
