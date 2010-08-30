package com.qcadoo.mes.core.data.definition;

import java.util.Map;

public abstract class ViewElementDefinition {

    public static final int TYPE_GRID = 1;

    public static final int TYPE_FORM = 2;

    private String name;

    private DataDefinition dataDefinition;

    private Map<String, String> options;

    private Map<String, String> events;

    private DataDefinition parent;

    public abstract int getType();

    public ViewElementDefinition(String name, DataDefinition dataDefinition) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public void setDataDefinition(DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Map<String, String> getEvents() {
        return events;
    }

    public void setEvents(Map<String, String> events) {
        this.events = events;
    }

    public DataDefinition getParent() {
        return parent;
    }

    public void setParent(DataDefinition parent) {
        this.parent = parent;
    }

}
