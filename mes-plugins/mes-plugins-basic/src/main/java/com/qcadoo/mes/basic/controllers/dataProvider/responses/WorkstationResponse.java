package com.qcadoo.mes.basic.controllers.dataProvider.responses;

public class WorkstationResponse {

    private Long id;

    private String number;

    private String name;

    private final StatusCode code;

    private String message;

    public WorkstationResponse(StatusCode code) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
