/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Order;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.BelongsToType;

public final class BelongsToEntityType implements BelongsToType {

    private final DataDefinitionService dataDefinitionService;

    private final String lookupFieldName;

    private final String pluginIdentifier;

    private final String entityName;

    private final boolean lazyLoading;

    public BelongsToEntityType(final String pluginIdentifier, final String entityName, final String lookupFieldName,
            final DataDefinitionService dataDefinitionService, final boolean lazyLoading) {
        this.pluginIdentifier = pluginIdentifier;
        this.entityName = entityName;
        this.lookupFieldName = lookupFieldName;
        this.dataDefinitionService = dataDefinitionService;
        this.lazyLoading = lazyLoading;
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
        return Object.class;
    }

    @Override
    public Map<Long, String> lookup(final String prefix) {
        SearchResult resultSet = getDataDefinition().find().orderBy(Order.asc(lookupFieldName)).list();
        Map<Long, String> possibleValues = new LinkedHashMap<Long, String>();

        for (Entity entity : resultSet.getEntities()) {
            possibleValues.put(entity.getId(), (String) entity.getField(lookupFieldName));
        }

        return possibleValues;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        return value;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(((Entity) value).getId());
    }

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(pluginIdentifier, entityName);
    }

    @Override
    public String getLookupFieldName() {
        return lookupFieldName;
    }

    @Override
    public boolean isLazyLoading() {
        return lazyLoading;
    }

}
