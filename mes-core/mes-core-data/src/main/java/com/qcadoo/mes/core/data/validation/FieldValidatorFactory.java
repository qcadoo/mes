package com.qcadoo.mes.core.data.validation;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.FieldValidator
 */

public interface FieldValidatorFactory {

    FieldValidator required();

    FieldValidator unique();

    FieldValidator maxLength(final int maxLenght);

    FieldValidator range(final Object from, final Object to);

    FieldValidator custom(final String beanName, final String validateMethodName);

    EntityValidator customEntity(final String beanName, final String validateMethodName);

    FieldValidator requiredOnCreation();

}
