package com.qcadoo.mes.core.data.model;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.validation.EntityValidator;

public interface DataDefinition {

    Entity get(final Long id);

    void delete(final Long id);

    Entity save(final Entity entity);

    SearchCriteriaBuilder find();

    SearchResult find(final SearchCriteria searchCriteria);

    void move(final Long id, final int offset);

    void moveTo(final Long id, final int position);

    String getName();

    String getFullyQualifiedClassName();

    Map<String, FieldDefinition> getFields();

    FieldDefinition getField(final String fieldName);

    boolean isVirtualTable();

    boolean isCoreTable();

    boolean isPluginTable();

    List<EntityValidator> getValidators();

    void callCreateHook(final Entity entity);

    void callUpdateHook(final Entity entity);

    Class<?> getClassForEntity();

    Object getInstanceForEntity();

    boolean isPrioritizable();

    FieldDefinition getPriorityField();

    boolean isDeletable();

}