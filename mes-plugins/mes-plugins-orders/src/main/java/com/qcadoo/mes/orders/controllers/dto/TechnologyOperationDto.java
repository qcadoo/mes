package com.qcadoo.mes.orders.controllers.dto;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.controller.dataProvider.MaterialDto;

import java.util.List;

public class TechnologyOperationDto {

    private Long id;

    private Long operationId;

    private Long workstationId;

    private String node;

    private List<MaterialDto> materials = Lists.newArrayList();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(Long workstationId) {
        this.workstationId = workstationId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<MaterialDto> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialDto> materials) {
        this.materials = materials;
    }
}
