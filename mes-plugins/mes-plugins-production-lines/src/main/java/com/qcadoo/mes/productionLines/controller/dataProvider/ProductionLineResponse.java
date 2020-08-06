package com.qcadoo.mes.productionLines.controller.dataProvider;

public class ProductionLineResponse {

    private Long id;

    private String number;

    private String name;

    private String unit;

    private final StatusCode code;

    private String message;

    public ProductionLineResponse(StatusCode code) {
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
