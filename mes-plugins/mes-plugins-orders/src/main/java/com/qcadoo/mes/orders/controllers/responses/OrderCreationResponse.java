package com.qcadoo.mes.orders.controllers.responses;


public class OrderCreationResponse {

    private StatusCode code;

    private String number;

    private String message;

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
}
