package com.qcadoo.mes.core.data.definition;

import java.util.Set;

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

    FieldType getType();

    Set<FieldValidator> getValidators();

    boolean isEditable();

    boolean isRequired();

    boolean isCustomField();

    boolean isHidden();

    Object getDefaultValue();

    boolean isUnique();

}
