package com.qcadoo.mes.core.model;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.validation.EntityValidator;

public interface DataDefinition {

    String getName();

    String getPluginIdentifier();

    Entity get(final Long id);

    void delete(final Long id);

    Entity save(final Entity entity);

    SearchCriteriaBuilder find();

    void move(final Long id, final int offset);

    void moveTo(final Long id, final int position);

    Map<String, FieldDefinition> getFields();

    FieldDefinition getField(final String fieldName);

    List<EntityValidator> getValidators();

    boolean isPrioritizable();

    FieldDefinition getPriorityField();

}
