package com.qcadoo.mes.api;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;

/**
 * Object represents data from the database tables. All fields are aggregated into key-value map. The key is the name of the field
 * from its definition - {@link com.qcadoo.mes.model.FieldDefinition#getName()}.
 * 
 * @apiviz.uses com.qcadoo.mes.model.FieldDefinition
 * @apiviz.uses com.qcadoo.mes.model.validators.ErrorMessage
 */
public interface Entity {

    /**
     * Set the entity's id.
     * 
     * @param id
     *            the entity's name
     */
    void setId(Long id);

    /**
     * Return the entity's id.
     * 
     * @return the entity's id
     */
    Long getId();

    /**
     * Return the entity's name.
     * 
     * @return the entity's name
     */
    String getName();

    /**
     * Return the entity's plugin identifier.
     * 
     * @return the entity's plugin identifier
     */
    String getPluginIdentifier();

    /**
     * Return the value of the field with given name.
     * 
     * @param fieldName
     *            field's name
     * @return the field's value
     */
    Object getField(String fieldName);

    /**
     * Return the value, casted to string, of the field with given name.
     * 
     * @param fieldName
     *            field's name
     * @return the field's value
     */
    String getStringField(String fieldName);

    /**
     * Return the value, casted to entity, of the field with given name.
     * 
     * @param fieldName
     *            field's name
     * @return the field's value
     */
    Entity getBelongsToField(String fieldName);

    /**
     * Return the value, casted to list of entities, of the field with given name.
     * 
     * @param fieldName
     *            field's name
     * @return the field's value
     */
    List<Entity> getHasManyField(String fieldName);

    /**
     * Set the value of the field with given name.
     * 
     * @param fieldName
     *            field's name
     * @param fieldValue
     *            field'value
     */
    void setField(String fieldName, Object fieldValue);

    /**
     * Return all field's values.
     * 
     * @return field's values - name - value pairs
     */
    Map<String, Object> getFields();

    /**
     * Set global error, not related with fields.
     * 
     * @param message
     *            message
     * @param vars
     *            message's vars
     */
    void addGlobalError(String message, String... vars);

    /**
     * Set error for given field.
     * 
     * @param fieldDefinition
     *            field's definition
     * @param message
     *            message
     * @param vars
     *            message's vars
     */
    void addError(FieldDefinition fieldDefinition, String message, String... vars);

    /**
     * Return all global errors.
     * 
     * @return errors
     */
    List<ErrorMessage> getGlobalErrors();

    /**
     * Return all field's errors.
     * 
     * @return fields' errors
     */
    Map<String, ErrorMessage> getErrors();

    /**
     * Return error for given field.
     * 
     * @param fieldName
     *            field's name
     * @return field's error
     */
    ErrorMessage getError(String fieldName);

    /**
     * Return true if there is no global and field's errors.
     * 
     * @return true if entity is valid
     */
    boolean isValid();

    /**
     * Return true if there is no field's errors for given field.
     * 
     * @param fieldName
     *            field's name
     * @return true if field is valid
     */
    boolean isFieldValid(String fieldName);

    /**
     * Create new entity and copy fields values.
     * 
     * @return copied entity
     */
    Entity copy();

}
