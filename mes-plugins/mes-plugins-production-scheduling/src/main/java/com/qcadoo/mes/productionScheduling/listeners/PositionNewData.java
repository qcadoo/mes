package com.qcadoo.mes.productionScheduling.listeners;

import com.qcadoo.model.api.Entity;

import java.util.Date;
import java.util.List;

class PositionNewData {

    private Integer laborWorkTime;

    private Integer machineWorkTime;

    private Integer additionalTime;

    private Date startDate;

    private Date finishDate;

    private List<Entity> workstationChangeovers;

    public PositionNewData(Integer laborWorkTime, Integer machineWorkTime, Integer additionalTime, Date newStartDate,
                           Date newFinishDate, List<Entity> workstationChangeovers) {
        this.laborWorkTime = laborWorkTime;
        this.machineWorkTime = machineWorkTime;
        this.additionalTime = additionalTime;
        this.startDate = newStartDate;
        this.finishDate = newFinishDate;
        this.workstationChangeovers = workstationChangeovers;
    }

    public Integer getLaborWorkTime() {
        return laborWorkTime;
    }

    public void setLaborWorkTime(Integer laborWorkTime) {
        this.laborWorkTime = laborWorkTime;
    }

    public Integer getMachineWorkTime() {
        return machineWorkTime;
    }

    public void setMachineWorkTime(Integer machineWorkTime) {
        this.machineWorkTime = machineWorkTime;
    }

    public Integer getAdditionalTime() {
        return additionalTime;
    }

    public void setAdditionalTime(Integer additionalTime) {
        this.additionalTime = additionalTime;
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

    public List<Entity> getWorkstationChangeovers() {
        return workstationChangeovers;
    }

    public void setWorkstationChangeovers(List<Entity> workstationChangeovers) {
        this.workstationChangeovers = workstationChangeovers;
    }
}
