package com.qcadoo.mes.core.data.internal.validators;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class BeanMethodValidator implements FieldValidator {

    private final Object bean;

    private final String staticValidateMethodName;

    private String errorMessage = "";

    public BeanMethodValidator(final Object bean, final String staticValidateMethodName) {
        this.bean = bean;
        this.staticValidateMethodName = staticValidateMethodName;
    }

    @Override
    public boolean validate(final FieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        // TODO masz
        // if(bean.staticValidateMethodName(value)) {
        // validationResults.addError(fieldDefinition, errorMessage);
        // return false;
        // }
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
