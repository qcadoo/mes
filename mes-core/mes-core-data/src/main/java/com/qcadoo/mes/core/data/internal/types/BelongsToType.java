package com.qcadoo.mes.core.data.internal.types;

import java.util.LinkedHashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.types.LookupedFieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class BelongsToType implements LookupedFieldType {

    private final DataAccessService dataAccessService;

    private final DataDefinitionService dataDefinitionService;

    private final String lookupFieldName;

    private final boolean eagerFetch;

    private final String entityName;

    public BelongsToType(final String entityName, final String lookupFieldName, final boolean eagerFetch,
            final DataAccessService dataAccessService, final DataDefinitionService dataDefinitionService) {
        this.entityName = entityName;
        this.lookupFieldName = lookupFieldName;
        this.eagerFetch = eagerFetch;
        this.dataAccessService = dataAccessService;
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
        return Object.class;
    }

    @Override
    public Map<Long, String> lookup(final String prefix) {
        SearchResult resultSet = dataAccessService.find(SearchCriteriaBuilder.forEntity(getDataDefinition())
                .orderBy(Order.asc(lookupFieldName)).build());
        Map<Long, String> possibleValues = new LinkedHashMap<Long, String>();

        for (Entity entity : resultSet.getEntities()) {
            possibleValues.put(entity.getId(), (String) entity.getField(lookupFieldName));
        }

        return possibleValues;
    }

    public boolean isEagerFetch() {
        return eagerFetch;
    }

    @Override
    public Object toObject(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        return value;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(((Entity) value).getId());
    }

    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(entityName);
    }

}
