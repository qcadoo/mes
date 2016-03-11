package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto;

import com.google.common.base.Objects;

public class PlannedEventRealizationDTO {

    private Long realizationId;
    private String realizationWorkerName;
    private String realizationWorkerSurname;
    private Integer realizationDuration;

    public Long getRealizationId() {
        return realizationId;
    }

    public void setRealizationId(Long realizationId) {
        this.realizationId = realizationId;
    }

    public String getRealizationWorkerName() {
        return realizationWorkerName;
    }

    public void setRealizationWorkerName(String realizationWorkerName) {
        this.realizationWorkerName = realizationWorkerName;
    }

    public String getRealizationWorkerSurname() {
        return realizationWorkerSurname;
    }

    public void setRealizationWorkerSurname(String realizationWorkerSurname) {
        this.realizationWorkerSurname = realizationWorkerSurname;
    }

    public Integer getRealizationDuration() {
        return realizationDuration;
    }

    public void setRealizationDuration(Integer realizationDuration) {
        this.realizationDuration = realizationDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlannedEventRealizationDTO that = (PlannedEventRealizationDTO) o;
        return Objects.equal(realizationId, that.realizationId)
                && Objects.equal(realizationWorkerName, that.realizationWorkerName)
                && Objects.equal(realizationWorkerSurname, that.realizationWorkerSurname)
                && Objects.equal(realizationDuration, that.realizationDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(realizationId, realizationWorkerName, realizationWorkerSurname, realizationDuration);
    }
}
