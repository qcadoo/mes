package com.qcadoo.mes.model;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.validators.EntityValidator;

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

    FieldDefinition getPriorityField();

    List<EntityValidator> getValidators();

    boolean isPrioritizable();

}
