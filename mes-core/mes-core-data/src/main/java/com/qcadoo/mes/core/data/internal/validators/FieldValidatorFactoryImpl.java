package com.qcadoo.mes.core.data.internal.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.internal.hooks.HookFactory;
import com.qcadoo.mes.core.data.validation.EntityValidator;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class FieldValidatorFactoryImpl implements FieldValidatorFactory {

    @Autowired
    private HookFactory hookFactory;

    @Override
    public FieldValidator required() {
        return new RequiredValidator();
    }

    @Override
    public FieldValidator requiredOnCreate() {
        return new RequiredOnCreateValidator();
    }

    @Override
    public FieldValidator unique() {
        return new UniqueValidator();
    }

    @Override
    public FieldValidator length(final int maxLenght) {
        return new MaxLenghtValidator(maxLenght);
    }

    @Override
    public FieldValidator precisionAndScale(final int presition, final int scale) {
        return new MaxPrecisionAndScaleValidator(presition, scale);
    }

    @Override
    public FieldValidator range(final Object from, final Object to) {
        return new RangeValidator(from, to);
    }

    @Override
    public FieldValidator custom(final String beanName, final String validateMethodName) {
        return new CustomValidator(hookFactory.getHook(beanName, validateMethodName));
    }

    @Override
    public EntityValidator customEntity(final String beanName, final String validateMethodName) {
        return new CustomEntityValidator(hookFactory.getHook(beanName, validateMethodName));
    }
}
