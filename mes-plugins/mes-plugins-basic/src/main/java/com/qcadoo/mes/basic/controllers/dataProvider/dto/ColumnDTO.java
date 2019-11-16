package com.qcadoo.mes.basic.controllers.dataProvider.dto;

public class ColumnDTO {

    private String id;

    private String name;

    private String unit;

    private String dataType;

    public ColumnDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ColumnDTO(String id, String name, String dataType) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
    }

    public ColumnDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
