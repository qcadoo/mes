package com.qcadoo.mes.api;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.internal.EntityList;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;

/**
 * Object represents data from the database tables - with and without custom fields - and virtual tables - build using only custom
 * fields. All fields - database's fields and custom fields - are aggregated into key-value map. The key is the name of the field
 * from its definition - {@link com.qcadoo.mes.core.data.definition.FieldDefinition#getName()}.
 * 
 * Value type must be the same as the type defined in {@link com.qcadoo.mes.core.data.definition.FieldDefinition#getType()}.
 */
public interface Entity {

    void setId(Long id);

    Long getId();

    String getName();

    String getPluginIdentifier();

    Object getField(String fieldName);

    String getStringField(String fieldName);

    Entity getBelongsToField(String fieldName);

    EntityList getHasManyField(String fieldName);

    void setField(String fieldName, Object fieldValue);

    Map<String, Object> getFields();

    void addGlobalError(String message, String... vars);

    void addError(FieldDefinition fieldDefinition, String message, String... vars);

    List<ErrorMessage> getGlobalErrors();

    Map<String, ErrorMessage> getErrors();

    ErrorMessage getError(String fieldName);

    boolean isValid();

    boolean isFieldValid(String fieldName);

    Entity copy();

}
