package com.qcadoo.mes.cmmsMachineParts.dto;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class ActionForPlannedEventDTO implements AbstractDTO {

    private Long id;

    private String action;

    private Long actionId;

    private Long responsibleWorkerId;

    private String responsibleWorker;

    private String description;

    private String state;

    private String reason;

    public ActionForPlannedEventDTO() {
    }

    public ActionForPlannedEventDTO(Long id, String action, Long actionId, Long responsibleWorkerId, String responsibleWorker,
            String description, String state, String reason) {
        this.id = id;
        this.action = action;
        this.actionId = actionId;
        this.responsibleWorkerId = responsibleWorkerId;
        this.responsibleWorker = responsibleWorker;
        this.description = description;
        this.state = state;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getResponsibleWorkerId() {
        return responsibleWorkerId;
    }

    public void setResponsibleWorkerId(Long responsibleWorkerId) {
        this.responsibleWorkerId = responsibleWorkerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ActionForPlannedEventDTO that = (ActionForPlannedEventDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (action != null ? !action.equals(that.action) : that.action != null)
            return false;
        if (responsibleWorkerId != null ? !responsibleWorkerId.equals(that.responsibleWorkerId)
                : that.responsibleWorkerId != null)
            return false;
        if (responsibleWorker != null ? !responsibleWorker.equals(that.responsibleWorker) : that.responsibleWorker != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (state != null ? !state.equals(that.state) : that.state != null)
            return false;
        return reason != null ? reason.equals(that.reason) : that.reason == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (responsibleWorkerId != null ? responsibleWorkerId.hashCode() : 0);
        result = 31 * result + (responsibleWorker != null ? responsibleWorker.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }
}
