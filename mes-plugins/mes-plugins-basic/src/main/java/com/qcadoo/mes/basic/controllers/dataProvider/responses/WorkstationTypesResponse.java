package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.WorkstationTypeDto;

import java.util.List;

public class WorkstationTypesResponse {

    private List<WorkstationTypeDto> workstationTypes = Lists.newArrayList();

    public List<WorkstationTypeDto> getWorkstationTypes() {
        return workstationTypes;
    }

    public void setWorkstationTypes(List<WorkstationTypeDto> workstationTypes) {
        this.workstationTypes = workstationTypes;
    }

    public WorkstationTypesResponse(List<WorkstationTypeDto> workstationTypes) {
        this.workstationTypes = workstationTypes;
    }
}
