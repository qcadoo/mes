package com.qcadoo.mes.core.data.model;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.validation.EntityValidator;

public interface ModelDefinition {

    public abstract Entity get(final Long id);

    public abstract void delete(final Long id);

    public abstract Entity save(final Entity entity);

    public abstract SearchCriteriaBuilder find();

    public abstract SearchResult find(final SearchCriteria searchCriteria);

    public abstract void move(final Long id, final int offset);

    public abstract void moveTo(final Long id, final int position);

    public abstract String getName();

    public abstract String getFullyQualifiedClassName();

    public abstract Map<String, FieldDefinition> getFields();

    public abstract FieldDefinition getField(final String fieldName);

    public abstract boolean isVirtualTable();

    public abstract boolean isCoreTable();

    public abstract boolean isPluginTable();

    public abstract List<EntityValidator> getValidators();

    public abstract void callOnCreate(final Entity entity);

    public abstract void callOnUpdate(final Entity entity);

    public abstract Class<?> getClassForEntity();

    public abstract Object getInstanceForEntity();

    public abstract boolean isPrioritizable();

    public abstract FieldDefinition getPriorityField();

    public abstract boolean isDeletable();

}