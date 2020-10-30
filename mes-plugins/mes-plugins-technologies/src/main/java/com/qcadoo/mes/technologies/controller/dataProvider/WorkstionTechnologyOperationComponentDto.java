package com.qcadoo.mes.technologies.controller.dataProvider;

public class WorkstionTechnologyOperationComponentDto {

    private Long technologyOperationComponentId;

    private Long workstationId;

    private String workstationName;

    private String workstationNumber;

    public Long getTechnologyOperationComponentId() {
        return technologyOperationComponentId;
    }

    public void setTechnologyOperationComponentId(Long technologyOperationComponentId) {
        this.technologyOperationComponentId = technologyOperationComponentId;
    }

    public Long getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(Long workstationId) {
        this.workstationId = workstationId;
    }

    public String getWorkstationName() {
        return workstationName;
    }

    public void setWorkstationName(String workstationName) {
        this.workstationName = workstationName;
    }

    public String getWorkstationNumber() {
        return workstationNumber;
    }

    public void setWorkstationNumber(String workstationNumber) {
        this.workstationNumber = workstationNumber;
    }
}
