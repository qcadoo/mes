package com.qcadoo.mes.core.data.internal.types;

import java.util.LinkedHashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.types.LookupedFieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class BelongsToFieldType implements LookupedFieldType {

    private final String entityName;

    private final DataAccessService dataAccessService;

    private final String lookupFieldName;

    private final boolean eagerFetch;

    public BelongsToFieldType(final String entityName, final String lookupFieldName, final boolean eagerFetch,
            final DataAccessService dataAccessService) {
        this.entityName = entityName;
        this.lookupFieldName = lookupFieldName;
        this.eagerFetch = eagerFetch;
        this.dataAccessService = dataAccessService;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO;
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
        ResultSet resultSet = dataAccessService.find(entityName,
                SearchCriteriaBuilder.forEntity(entityName).orderBy(Order.asc(lookupFieldName)).build());
        Map<Long, String> possibleValues = new LinkedHashMap<Long, String>();

        for (Entity entity : resultSet.getResults()) {
            possibleValues.put(entity.getId(), (String) entity.getField(lookupFieldName));
        }

        return possibleValues;
    }

    public String getEntityName() {
        return entityName;
    }

    public boolean isEagerFetch() {
        return eagerFetch;
    }

    @Override
    public Object fromString(FieldDefinition fieldDefinition, String value, ValidationResults validationResults) {
        throw new IllegalStateException("belongsTo field type doesn't suppont fromString method");
    }

    @Override
    public boolean validate(FieldDefinition fieldDefinition, Object value, ValidationResults validationResults) {
        return true;
    }

}
