package com.qcadoo.mes.core.data.internal.types;

import java.util.LinkedHashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.types.LookupedFieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class BelongsToType implements LookupedFieldType {

    private final DataDefinition dataDefinition;

    private final DataAccessService dataAccessService;

    private final String lookupFieldName;

    private final boolean eagerFetch;

    public BelongsToType(final DataDefinition dataDefinition, final String lookupFieldName, final boolean eagerFetch,
            final DataAccessService dataAccessService) {
        this.dataDefinition = dataDefinition;
        this.lookupFieldName = lookupFieldName;
        this.eagerFetch = eagerFetch;
        this.dataAccessService = dataAccessService;
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
        SearchResult resultSet = dataAccessService.find(SearchCriteriaBuilder.forEntity(dataDefinition)
                .orderBy(Order.asc(lookupFieldName)).build());
        Map<Long, String> possibleValues = new LinkedHashMap<Long, String>();

        for (Entity entity : resultSet.getEntities()) {
            possibleValues.put(entity.getId(), (String) entity.getField(lookupFieldName));
        }

        return possibleValues;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
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

}
