package com.qcadoo.mes.basic.controllers.dataProvider.dto;

public class ColumnDTO implements AbstractDTO {

    private String id;

    private String name;

    private String field;

    private boolean sortable = true;

    public ColumnDTO(String id, String name, String field) {
        this.id = id;
        this.name = name;
        this.field = field;
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }
}
