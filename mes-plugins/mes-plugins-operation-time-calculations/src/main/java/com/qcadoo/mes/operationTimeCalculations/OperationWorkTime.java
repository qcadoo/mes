package com.qcadoo.mes.operationTimeCalculations;

public class OperationWorkTime {

    private Integer laborWorkTime;

    private Integer machineWorkTime;

    private Integer duration;

    public final Integer getLaborWorkTime() {
        return laborWorkTime;
    }

    public final void setLaborWorkTime(final Integer laborWorkTime) {
        this.laborWorkTime = laborWorkTime;
    }

    public final Integer getMachineWorkTime() {
        return machineWorkTime;
    }

    public final void setMachineWorkTime(final Integer machineWorkTime) {
        this.machineWorkTime = machineWorkTime;
    }

    public final Integer getDuration() {
        return duration;
    }

    public final void setDuration(final Integer duration) {
        this.duration = duration;
    }

}
