/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
