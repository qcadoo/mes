/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.model.internal.validators;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.api.ErrorMessageDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;

public final class CustomValidator implements FieldHookDefinition, ErrorMessageDefinition {

    private static final String CUSTOM_ERROR = "core.validate.field.error.custom";

    private final FieldHookDefinition fieldHook;

    private String errorMessage = CUSTOM_ERROR;

    private FieldDefinition fieldDefinition;

    public CustomValidator(final FieldHookDefinition fieldHook) {
        this.fieldHook = fieldHook;
    }

    @Override
    public void initialize(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
        fieldHook.initialize(dataDefinition, fieldDefinition);
    }

    @Override
    public boolean call(final Entity entity, final Object oldValue, final Object newValue) {
        boolean result = fieldHook.call(entity, oldValue, newValue);

        if (result) {
            return true;
        }

        entity.addError(fieldDefinition, errorMessage);

        return false;
    }

    @Override
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
