package com.qcadoo.mes.basic.controllers.dataProvider.requests;

public class FaultTypeRequest {

    private Long workstationId;

    private Long subassemblyId;

    public Long getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(Long workstationId) {
        this.workstationId = workstationId;
    }

    public Long getSubassemblyId() {
        return subassemblyId;
    }

    public void setSubassemblyId(Long subassemblyId) {
        this.subassemblyId = subassemblyId;
    }
}
