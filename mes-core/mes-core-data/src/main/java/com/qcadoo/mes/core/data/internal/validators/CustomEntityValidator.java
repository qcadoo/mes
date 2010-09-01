package com.qcadoo.mes.core.data.internal.validators;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.validation.EntityValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class CustomEntityValidator implements EntityValidator {

    private static final String UNKNOWN_ERROR = "core.validation.error.unknown";

    private static final String CUSTOM_ERROR = "core.validation.error.customEntity";

    private static final Logger LOG = LoggerFactory.getLogger(CustomEntityValidator.class);

    private final Object bean;

    private final String staticValidateMethodName;

    private String errorMessage = CUSTOM_ERROR;

    public CustomEntityValidator(final Object bean, final String staticValidateMethodName) {
        this.bean = bean;
        this.staticValidateMethodName = staticValidateMethodName;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final Entity entity, final ValidationResults validationResults) {
        try {
            boolean result = (Boolean) MethodUtils.invokeMethod(bean, staticValidateMethodName, new Object[] { entity });
            if (result) {
                return true;
            } else {
                validationResults.addGlobalError(errorMessage);
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
        validationResults.addGlobalError(UNKNOWN_ERROR);
        return false;
    }

    @Override
    public EntityValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
