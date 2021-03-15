package com.qcadoo.mes.masterOrders.controllers.orders;

import com.google.common.collect.Lists;

import java.util.List;

public class GenerateOrdersSalePlanResponse {

    private GenerateOrdersSalePlanResponse.SimpleResponseStatus status;

    private List<String> messages = Lists.newArrayList();

    private List<String> errorMessages = Lists.newArrayList();

    private Long entityId;

    public GenerateOrdersSalePlanResponse() {
        super();
        this.status = GenerateOrdersSalePlanResponse.SimpleResponseStatus.OK;
    }

    public GenerateOrdersSalePlanResponse(List<String> messages, List<String> errorMessages) {
        super();
        this.status = GenerateOrdersSalePlanResponse.SimpleResponseStatus.ERROR;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public void setStatus(final GenerateOrdersSalePlanResponse.SimpleResponseStatus status) {
        this.status = status;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public GenerateOrdersSalePlanResponse.SimpleResponseStatus getStatus() {
        return status;
    }

    public enum SimpleResponseStatus {
        OK, ERROR;
    }

}
