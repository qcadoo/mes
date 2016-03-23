package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto;

import com.google.common.base.Objects;

public class WorkTimeDTO {

    private Long staffworkTimeId;
    private String staffWorkTimeWorker;
    private Integer staffWorkTimeLaborTime;

    public Long getStaffworkTimeId() {
        return staffworkTimeId;
    }

    public void setStaffworkTimeId(Long staffworkTimeId) {
        this.staffworkTimeId = staffworkTimeId;
    }

    public String getStaffWorkTimeWorker() {
        return staffWorkTimeWorker;
    }

    public void setStaffWorkTimeWorker(String staffWorkTimeWorker) {
        this.staffWorkTimeWorker = staffWorkTimeWorker;
    }

    public Integer getStaffWorkTimeLaborTime() {
        return staffWorkTimeLaborTime;
    }

    public void setStaffWorkTimeLaborTime(Integer staffWorkTimeLaborTime) {
        this.staffWorkTimeLaborTime = staffWorkTimeLaborTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkTimeDTO that = (WorkTimeDTO) o;
        return Objects.equal(staffWorkTimeLaborTime, that.staffWorkTimeLaborTime)
                && Objects.equal(staffworkTimeId, that.staffworkTimeId)
                && Objects.equal(staffWorkTimeWorker, that.staffWorkTimeWorker);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(staffworkTimeId, staffWorkTimeWorker, staffWorkTimeLaborTime);
    }
}
