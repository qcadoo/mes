/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.technologies.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentReferenceMode;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentValidators {

    public boolean validate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        boolean isValid = true;

        // TODO DEV_TEAM validations can be uncommented when we fixed problem with add reference technology
        // isValid = isValid && validateEntityTypeOfTechnologyOperationComponent(technologyOperationComponentDD,
        // technologyOperationComponent);
        // isValid = isValid
        // && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(technologyOperationComponentDD,
        // technologyOperationComponent);

        return isValid;
    }

    public boolean validateEntityTypeOfTechnologyOperationComponent(final DataDefinition technologyOperationComponentDD,
            final Entity technologyOperationComponent) {
        boolean isValid = true;

        if (TechnologyOperationComponentType.OPERATION.getStringValue().equals(
                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
            if (technologyOperationComponent.getField(TechnologyOperationComponentFields.OPERATION) == null) {
                technologyOperationComponent.addError(
                        technologyOperationComponentDD.getField(TechnologyOperationComponentFields.OPERATION),
                        "qcadooView.validate.field.error.missing");

                isValid = false;
            }
        } else if (TechnologyOperationComponentType.REFERENCE_TECHNOLOGY.getStringValue().equals(
                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
            if (technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY) == null) {
                technologyOperationComponent.addError(
                        technologyOperationComponentDD.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY),
                        "qcadooView.validate.field.error.missing");

                isValid = false;
            }
            if (technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCE_MODE) == null) {
                technologyOperationComponent.setField(TechnologyOperationComponentFields.REFERENCE_MODE,
                        TechnologyOperationComponentReferenceMode.REFERENCE.getStringValue());
            }
        } else {
            throw new IllegalStateException("unknown entityType");
        }

        return isValid;
    }

}
