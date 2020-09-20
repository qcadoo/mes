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

    public OrderCreationResponse(StatusCode code) {
        this.code = code;
    }

    public OrderCreationResponse(String message) {
        this.code = code;
        this.message = message;
    }

    public StatusCode getCode() {
        return code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(StatusCode code) {
        this.code = code;
    }

    public OrderHolder getOrder() {
        return order;
    }

    public void setOrder(OrderHolder order) {
        this.order = order;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public List<OperationalTaskHolder> getOperationalTasks() {
        return operationalTasks;
    }

    public void setOperationalTasks(List<OperationalTaskHolder> operationalTasks) {
        this.operationalTasks = operationalTasks;
    }
}
