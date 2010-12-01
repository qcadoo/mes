/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.viewold.components.grid.ListData;

public final class HasManyEntitiesType implements HasManyType {

    private final String entityName;

    private final String joinFieldName;

    private final DataDefinitionService dataDefinitionService;

    private final String pluginIdentifier;

    private final Cascade cascade;

    public HasManyEntitiesType(final String pluginIdentifier, final String entityName, final String joinFieldName,
            final Cascade cascade, final DataDefinitionService dataDefinitionService) {
        this.pluginIdentifier = pluginIdentifier;
        this.entityName = entityName;
        this.joinFieldName = joinFieldName;
        this.cascade = cascade;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public boolean isSearchable() {
        return false;
    }

    @Override
    public boolean isOrderable() {
        return false;
    }

    @Override
    public boolean isAggregable() {
        return false;
    }

    @Override
    public Class<?> getType() {
        return ListData.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        return null;
    }

    @Override
    public String toString(final Object value) {
        return null;
    }

    @Override
    public String getJoinFieldName() {
        return joinFieldName;
    }

    @Override
    public Cascade getCascade() {
        return cascade;
    }

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(pluginIdentifier, entityName);
    }

}
