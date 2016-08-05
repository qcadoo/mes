package com.qcadoo.mes.cmmsMachineParts.dto;

public class ActionForPlannedEventDto {

    private String action;

    private String responsibleWorker;

    private String description;

    private String state;

    private String reason;

    public ActionForPlannedEventDto() {
    }

    public ActionForPlannedEventDto(String action, String responsibleWorker, String description, String state, String reason) {
        this.action = action;
        this.responsibleWorker = responsibleWorker;
        this.description = description;
        this.state = state;
        this.reason = reason;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResponsibleWorker() {
        return responsibleWorker;
    }

    public void setResponsibleWorker(String responsibleWorker) {
        this.responsibleWorker = responsibleWorker;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
