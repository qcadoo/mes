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

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.validators.EntityValidator;

public final class CustomEntityValidator implements EntityValidator {

    private static final String CUSTOM_ERROR = "core.validate.global.error.custom";

    private final HookDefinition entityValidateHook;

    public CustomEntityValidator(final HookDefinition entityValidateHook) {
        this.entityValidateHook = entityValidateHook;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final Entity entity) {
        boolean result = entityValidateHook.callWithEntityAndGetBoolean(dataDefinition, entity);
        if (result) {
            return true;
        }
        if (entity.isValid()) {
            entity.addGlobalError(CUSTOM_ERROR);
        }
        return false;
    }

}
