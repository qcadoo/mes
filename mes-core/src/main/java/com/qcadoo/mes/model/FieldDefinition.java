package com.qcadoo.mes.model;

import java.util.List;

import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.validators.FieldValidator;

/**
 * Object defines database field.
 * 
 * @apiviz.has com.qcadoo.mes.core.data.definition.FieldType
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldValidator
 * @apiviz.has com.qcadoo.mes.model.DataDefinition
 */
public interface FieldDefinition {

    /**
     * Return field's name.
     * 
     * @return field's name
     */
    String getName();

    /**
     * {@link FieldType#toString(Object)}
     */
    String getValue(final Object value);

    /**
     * Return field's type.
     * 
     * @return field's type
     */
    FieldType getType();

    /**
     * Return all defined field's validators.
     * 
     * @return field's validators
     */
    List<FieldValidator> getValidators();

    /**
     * Return true if this field is readonly on update.
     * 
     * @return is readonly on update
     */
    boolean isReadOnlyOnUpdate();

    /**
     * Return true if this field is readonly.
     * 
     * @return is readonly
     */
    boolean isReadOnly();

    /**
     * Return true if this field is required.
     * 
     * @return is required
     */
    boolean isRequired();

    /**
     * Return true if this field is required on create.
     * 
     * @return is required on create
     */
    boolean isRequiredOnCreate();

    /**
     * Return default value for this field.
     * 
     * @return default value
     */
    Object getDefaultValue();

    /**
     * Return true if this field is unique.
     * 
     * @return is unique
     */
    boolean isUnique();

    /**
     * Return true if this field is unique persistent (will be saved in database).
     * 
     * @return is persistent
     */
    boolean isPersistent();

    /**
     * Return data definition which this field belongs to.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

}
