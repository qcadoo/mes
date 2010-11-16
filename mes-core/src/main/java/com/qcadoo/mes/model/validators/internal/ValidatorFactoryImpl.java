/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.validators.internal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.validators.EntityValidator;
import com.qcadoo.mes.model.validators.FieldValidator;

@Service
public final class ValidatorFactoryImpl implements ValidatorFactory {

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
    public FieldValidator length(final Integer min, final Integer is, final Integer max) {
        return new LengthValidator(min, is, max);
    }

    @Override
    public FieldValidator precision(final Integer min, final Integer is, final Integer max) {
        return new PrecisionValidator(min, is, max);
    }

    @Override
    public FieldValidator scale(final Integer min, final Integer is, final Integer max) {
        return new ScaleValidator(min, is, max);
    }

    @Override
    public FieldValidator range(final Object from, final Object to, final boolean inclusive) {
        return new RangeValidator(from, to, inclusive);
    }

    @Override
    public FieldValidator custom(final HookDefinition validateHook) {
        return new CustomValidator(validateHook);
    }

    @Override
    public EntityValidator customEntity(final HookDefinition entityValidateHook) {
        return new CustomEntityValidator(entityValidateHook);
    }
}
