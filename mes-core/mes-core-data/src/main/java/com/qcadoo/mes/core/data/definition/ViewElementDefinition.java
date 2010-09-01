package com.qcadoo.mes.core.data.definition;

import java.util.Map;

public abstract class ViewElementDefinition {

    public static final int TYPE_GRID = 1;

    public static final int TYPE_FORM = 2;

    private String name;

    private DataDefinition dataDefinition;

    private Map<String, String> options;

    private Map<String, String> events;

    private DataDefinition parentDefinition; // null, url, viewElement:{name}

    private String parentField;

    private ViewDefinition correspondingView;

    public abstract int getType();

    public ViewElementDefinition(final String name, final DataDefinition dataDefinition) {
        this.name = name;
        this.dataDefinition = dataDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public void setDataDefinition(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    public Map<String, String> getEvents() {
        return events;
    }

    public void setEvents(final Map<String, String> events) {
        this.events = events;
    }

    public DataDefinition getParentDefinition() {
        return parentDefinition;
    }

    public void setParentDefinition(final DataDefinition parentDefinition) {
        this.parentDefinition = parentDefinition;
    }

    public String getParentField() {
        return parentField;
    }

    public void setParentField(final String parentField) {
        this.parentField = parentField;
    }

    public ViewDefinition getCorrespondingView() {
        return correspondingView;
    }

    public void setCorrespondingView(ViewDefinition correspondingView) {
        this.correspondingView = correspondingView;
    }

}
