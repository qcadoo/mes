package com.qcadoo.mes.orders.controllers.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.controllers.dto.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dto.OrderHolder;

import java.util.List;

public class OrderCreationResponse {

    private StatusCode code;

    private String number;

    private String message;

    private String additionalInformation;

    private OrderHolder order;

    private List<OperationalTaskHolder> operationalTasks = Lists.newArrayList();

    public enum StatusCode {
        OK, ERROR;
    }

    public OrderCreationResponse(final StatusCode code) {
        this.code = code;
    }

    public OrderCreationResponse(final String message) {
        this.code = code;
        this.message = message;
    }

    public StatusCode getCode() {
        return code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setCode(final StatusCode code) {
        this.code = code;
    }

    public OrderHolder getOrder() {
        return order;
    }

    public void setOrder(final OrderHolder order) {
        this.order = order;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(final String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public List<OperationalTaskHolder> getOperationalTasks() {
        return operationalTasks;
    }

    public void setOperationalTasks(final List<OperationalTaskHolder> operationalTasks) {
        this.operationalTasks = operationalTasks;
    }

}
