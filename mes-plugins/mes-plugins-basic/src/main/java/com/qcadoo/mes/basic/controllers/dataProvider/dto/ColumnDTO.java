package com.qcadoo.mes.basic.controllers.dataProvider.dto;

public class ColumnDTO {

    private String id;

    private String name;

    private String unit;

    private String dataType;

    public ColumnDTO(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public ColumnDTO(final String id, final String name, final String dataType) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
    }

    public ColumnDTO() {

    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }

}
