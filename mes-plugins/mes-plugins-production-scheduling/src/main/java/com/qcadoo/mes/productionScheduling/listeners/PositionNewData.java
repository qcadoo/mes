package com.qcadoo.mes.productionScheduling.listeners;

import java.util.Date;

class PositionNewData {

    private Integer laborWorkTime;

    private Integer machineWorkTime;

    private Integer additionalTime;

    private Date startDate;

    private Date finishDate;

    public PositionNewData(Integer laborWorkTime, Integer machineWorkTime, Integer additionalTime, Date newStartDate,
            Date newFinishDate) {
        this.laborWorkTime = laborWorkTime;
        this.machineWorkTime = machineWorkTime;
        this.additionalTime = additionalTime;
        startDate = newStartDate;
        finishDate = newFinishDate;
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
}
