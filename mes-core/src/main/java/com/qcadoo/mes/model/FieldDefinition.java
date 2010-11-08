package com.qcadoo.mes.model;

import java.util.List;

import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.validators.FieldValidator;

/**
 * Field defines database field or custom field (according to {@link FieldDefinition#isCustomField()}).
 * 
 * Not editable field can't be changed after entity creation.
 * 
 * Definition of database field can't be modified using RAD.
 * 
 * @apiviz.has com.qcadoo.mes.core.data.definition.FieldType
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldValidator
 */
public interface FieldDefinition {

    String getName();

    String getValue(final Object value);

    FieldType getType();

    List<FieldValidator> getValidators();

    boolean isReadOnlyOnUpdate();

    boolean isReadOnly();

    boolean isRequired();

    boolean isRequiredOnCreate();

    boolean isCustomField();

    Object getDefaultValue();

    boolean isUnique();

    boolean isPersistent();

    DataDefinition getDataDefinition();

}
