package com.qcadoo.mes.view.components.grid;

import java.util.List;

import com.qcadoo.mes.api.Entity;

public final class ListData {

    private Integer totalNumberOfEntities;

    private Long selectedEntityId;

    private List<Entity> entities;

    private final String contextFieldName;

    private final Long contextId;

    public ListData() {
        this.contextFieldName = null;
        this.contextId = null;
    }

    public ListData(final int totalNumberOfEntities, final List<Entity> entities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
        this.contextFieldName = null;
        this.contextId = null;
    }

    public ListData(final int totalNumberOfEntities, final List<Entity> entities, final String contextFieldName, final Long contextId) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
        this.contextFieldName = contextFieldName;
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

    public String getContextFieldName() {
        return contextFieldName;
    }

    public Long getContextId() {
        return contextId;
    }

}
