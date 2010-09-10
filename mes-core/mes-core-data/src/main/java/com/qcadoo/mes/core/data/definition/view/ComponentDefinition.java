package com.qcadoo.mes.core.data.definition.view;

import java.util.Map;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public abstract class ComponentDefinition {

    public static final int TYPE_GRID = 1;

    public static final int TYPE_FORM = 2;

    private String name;

    private String header;

    private DataDefinition dataDefinition;

    private Map<String, String> options;

    private Map<String, String> events;

    private String parent; // null, "entityId", "viewElement:{name}"

    private String parentField;

    private String correspondingViewName;

    private boolean correspondingViewModal = false;

    public abstract int getType();

    public ComponentDefinition(final String name, final DataDefinition dataDefinition) {
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

    public String getParentField() {
        return parentField;
    }

    public void setParentField(final String parentField) {
        this.parentField = parentField;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getCorrespondingViewName() {
        return correspondingViewName;
    }

    public void setCorrespondingViewName(final String correspondingViewName) {
        this.correspondingViewName = correspondingViewName;
    }

    public boolean isCorrespondingViewModal() {
        return correspondingViewModal;
    }

    public void setCorrespondingViewModal(final boolean correspondingViewModal) {
        this.correspondingViewModal = correspondingViewModal;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

}
