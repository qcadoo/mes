package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.FaultTypeDto;

import java.util.List;

public class FaultTypeResponse {

    private List<FaultTypeDto> faultTypes = Lists.newArrayList();

    public FaultTypeResponse(List<FaultTypeDto> faultTypes) {
        this.faultTypes = faultTypes;
    }

    public List<FaultTypeDto> getFaultTypes() {
        return faultTypes;
    }

    public void setFaultTypes(List<FaultTypeDto> faultTypes) {
        this.faultTypes = faultTypes;
    }
}
