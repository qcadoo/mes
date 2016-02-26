package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto;

import com.google.common.base.Objects;

import java.util.Date;

public class StateChangeDTO {

    private Long stateChangeId;
    private Date stateChangeDateAndTime;
    private String stateChangeSourceState;
    private String stateChangeTargetState;
    private String stateStatus;
    private String stateWorker;

    public Long getStateChangeId() {
        return stateChangeId;
    }

    public void setStateChangeId(Long stateChangeId) {
        this.stateChangeId = stateChangeId;
    }

    public Date getStateChangeDateAndTime() {
        return stateChangeDateAndTime;
    }

    public void setStateChangeDateAndTime(Date stateChangeDateAndTime) {
        this.stateChangeDateAndTime = stateChangeDateAndTime;
    }

    public String getStateChangeSourceState() {
        return stateChangeSourceState;
    }

    public void setStateChangeSourceState(String stateChangeSourceState) {
        this.stateChangeSourceState = stateChangeSourceState;
    }

    public String getStateChangeTargetState() {
        return stateChangeTargetState;
    }

    public void setStateChangeTargetState(String stateChangeTargetState) {
        this.stateChangeTargetState = stateChangeTargetState;
    }

    public String getStateStatus() {
        return stateStatus;
    }

    public void setStateStatus(String stateStatus) {
        this.stateStatus = stateStatus;
    }

    public String getStateWorker() {
        return stateWorker;
    }

    public void setStateWorker(String stateWorker) {
        this.stateWorker = stateWorker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StateChangeDTO that = (StateChangeDTO) o;
        return Objects.equal(stateChangeId, that.stateChangeId)
                && Objects.equal(stateChangeDateAndTime, that.stateChangeDateAndTime)
                && Objects.equal(stateChangeSourceState, that.stateChangeSourceState)
                && Objects.equal(stateChangeTargetState, that.stateChangeTargetState)
                && Objects.equal(stateStatus, that.stateStatus)
                && Objects.equal(stateWorker, that.stateWorker);
    }

    @Override
    public int hashCode() {
        return Objects
                .hashCode(stateChangeId, stateChangeDateAndTime, stateChangeSourceState, stateChangeTargetState, stateStatus,
                        stateWorker);
    }
}
