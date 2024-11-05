package com.qcadoo.mes.orders.controllers.responses;

public class OperationalTaskFinishResponse {
    private final StatusCode code;

    private String message;

    public OperationalTaskFinishResponse() {
        this.code = StatusCode.OK;
    }

    public String getMessage() {
        return message;
    }

    public StatusCode getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum StatusCode {
        OK, ERROR;
    }
}
