package com.qcadoo.mes.technologies.controller.dataProvider;

import com.google.common.collect.Lists;

import java.util.List;

public class OperationsResponse {

    private List<OperationDto> operations = Lists.newArrayList();

    public OperationsResponse(List<OperationDto> operations) {
        this.operations = operations;
    }

    public List<OperationDto> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationDto> operations) {
        this.operations = operations;
    }
}
