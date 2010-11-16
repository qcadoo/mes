/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.internal;

import java.util.AbstractList;
import java.util.List;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;

public final class EntityList extends AbstractList<Entity> {

    private final DataDefinition dataDefinition;

    private final Long parentId;

    private final FieldDefinition joinFieldDefinition;

    private List<Entity> entities = null;

    public EntityList(final DataDefinition dataDefinition, final String joinFieldName, final Long parentId) {
        this.dataDefinition = dataDefinition;
        this.joinFieldDefinition = dataDefinition.getField(joinFieldName);
        this.parentId = parentId;
    }

    private void loadEntities() {
        if (entities == null) {
            entities = find().list().getEntities();
        }
    }

    public SearchCriteriaBuilder find() {
        return dataDefinition.find().restrictedWith(Restrictions.belongsTo(joinFieldDefinition, parentId));
    }

    @Override
    public Entity get(final int index) {
        if (entities == null) {
            loadEntities();
        }
        return entities.get(index);
    }

    @Override
    public int size() {
        if (entities == null) {
            loadEntities();
        }
        return entities.size();
    }

    @Override
    public String toString() {
        return "EntityList[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "]["
                + joinFieldDefinition.getName() + "=" + parentId + "]";
    }

}
