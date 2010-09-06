package com.qcadoo.mes.core.data.validation;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.FieldValidator
 */

public interface FieldValidatorFactory {

    FieldValidator required();

    FieldValidator unique();

    FieldValidator length(final int maxLenght);

    FieldValidator range(final Object from, final Object to);

    FieldValidator custom(final String beanName, final String methodName);

    EntityValidator customEntity(final String beanName, final String methodName);

    FieldValidator requiredOnCreation();

    FieldValidator precisionAndScale(int scale, int presition);

}
