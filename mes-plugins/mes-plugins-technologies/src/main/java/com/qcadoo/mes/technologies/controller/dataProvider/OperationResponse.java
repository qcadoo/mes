package com.qcadoo.mes.technologies.controller.dataProvider;


public class OperationResponse {

    private Long id;

    private String number;

    private final StatusCode code;

    private String message;

    public OperationResponse(StatusCode code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public StatusCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum StatusCode {
        OK, ERROR;
    }
}
