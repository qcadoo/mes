package com.qcadoo.mes.core.data.internal.validators;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class CustomValidator implements FieldValidator {

    private static final String UNKNOWN_ERROR = "core.validation.error.unknown";

    private static final String CUSTOM_ERROR = "core.validation.error.custom";

    private static final Logger LOG = LoggerFactory.getLogger(CustomValidator.class);

    private final Object bean;

    private final String staticValidateMethodName;

    private String errorMessage = CUSTOM_ERROR;

    public CustomValidator(final Object bean, final String staticValidateMethodName) {
        this.bean = bean;
        this.staticValidateMethodName = staticValidateMethodName;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        try {
            boolean result = (Boolean) MethodUtils.invokeMethod(bean, staticValidateMethodName, new Object[] { value });
            if (result) {
                return true;
            } else {
                validationResults.addError(fieldDefinition, errorMessage);
                return false;
            }
        } catch (NoSuchMethodException e) {
            LOG.warn("custom validation method is not exist", e);
        } catch (IllegalAccessException e) {
            LOG.warn("problem while calling custom validation method", e);
        } catch (InvocationTargetException e) {
            LOG.warn("problem while calling custom validation method", e);
        } catch (ClassCastException e) {
            LOG.warn("custom validation method has returned not boolean type", e);
        }
        validationResults.addError(fieldDefinition, UNKNOWN_ERROR);
        return false;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity,
            final ValidationResults validationResults) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
