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

package com.qcadoo.mes.model.validators.internal;

import java.util.List;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.TreeType;
import com.qcadoo.mes.model.validators.FieldValidator;

public final class RequiredValidator implements FieldValidator {

    private static final String MISSING_ERROR = "core.validate.field.error.missing";

    private static final String MISSING_RELATION_ERROR = "core.validate.field.error.missingRelation";

    private String errorMessage = MISSING_ERROR;

    private String errorRelationMessage = MISSING_RELATION_ERROR;

    @Override
    @SuppressWarnings("rawtypes")
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {

        if (fieldDefinition.getType() instanceof HasManyType) {
            if (validatedEntity.getField(fieldDefinition.getName()) == null
                    || ((List) validatedEntity.getField(fieldDefinition.getName())).isEmpty()) {
                validatedEntity.addError(fieldDefinition, errorRelationMessage);
                return false;
            }
        } else if (fieldDefinition.getType() instanceof TreeType) {
            if (validatedEntity.getField(fieldDefinition.getName()) == null
                    || ((List) validatedEntity.getField(fieldDefinition.getName())).isEmpty()) {
                validatedEntity.addError(fieldDefinition, errorRelationMessage);
                return false;
            }
        } else if (value == null) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        this.errorRelationMessage = errorMessage;
        return this;
    }

}
