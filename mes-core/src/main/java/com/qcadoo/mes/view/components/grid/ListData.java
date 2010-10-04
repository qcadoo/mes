package com.qcadoo.mes.view.components.grid;

import java.util.List;

import com.qcadoo.mes.api.Entity;

public final class ListData {

    private Integer totalNumberOfEntities;

    private Long selectedEntityId;

    private List<Entity> entities;

    private final String contextField;

    private final Long contextId;

    public ListData() {
        this.contextField = null;
        this.contextId = null;
    }

    public ListData(final int totalNumberOfEntities, final List<Entity> entities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
        this.contextField = null;
        this.contextId = null;
    }

    public ListData(final int totalNumberOfEntities, final List<Entity> entities, final String contextField, final Long contextId) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
        this.contextField = contextField;
        this.contextId = contextId;
    }

    public Integer getTotalNumberOfEntities() {
        return totalNumberOfEntities;
    }

    public void setTotalNumberOfEntities(final Integer totalNumberOfEntities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(final List<Entity> entities) {
        this.entities = entities;
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
    }

    public String getContextField() {
        return contextField;
    }

    public Long getContextId() {
        return contextId;
    }

}
