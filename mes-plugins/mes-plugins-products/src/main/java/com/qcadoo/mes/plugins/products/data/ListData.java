package com.qcadoo.mes.plugins.products.data;

import java.util.List;

import com.qcadoo.mes.core.data.beans.Entity;

public class ListData {

    private int totalNumberOfEntities;

    private List<Entity> entities;

    public ListData(int totalNumberOfEntities, List<Entity> entities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
        this.entities = entities;
    }

    public final int getTotalNumberOfEntities() {
        return totalNumberOfEntities;
    }

    public final void setTotalNumberOfEntities(int totalNumberOfEntities) {
        this.totalNumberOfEntities = totalNumberOfEntities;
    }

    public final List<Entity> getEntities() {
        return entities;
    }

    public final void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

}
