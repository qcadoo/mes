package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.WorkstationDto;

import java.util.List;

public class WorkstationsResponse {

    private List<WorkstationDto> workstations = Lists.newArrayList();

    public List<WorkstationDto> getWorkstations() {
        return workstations;
    }

    public void setWorkstations(List<WorkstationDto> workstations) {
        this.workstations = workstations;
    }
}
