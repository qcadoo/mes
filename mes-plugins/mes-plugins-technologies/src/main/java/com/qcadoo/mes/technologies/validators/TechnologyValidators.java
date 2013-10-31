/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static com.qcadoo.mes.technologies.constants.TechnologyFields.MASTER;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class TechnologyValidators {

    @Autowired
    private ProductService productService;

    @Autowired
    private TranslationService translationService;

    public boolean validate(final DataDefinition dataDefinition, final Entity technology) {
        boolean isValid = true;
        isValid = isValid && checkTechnologyDefault(dataDefinition, technology);
        isValid = isValid && productService.checkIfProductIsNotRemoved(dataDefinition, technology);
<<<<<<< HEAD
        isValid = isValid && checkIfTreeOperationIsValid(dataDefinition, technology);
        return isValid;
    }

    public boolean checkIfTreeOperationIsValid(final DataDefinition dataDefinition, final Entity technology) {
        if (technology == null || technology.getId() == null) {
            return true;
        }
        Entity techFromDB = technology.getDataDefinition().get(technology.getId());
        if (techFromDB == null) {
            return true;
        }
        String message = "";
        boolean isValid = true;
        for (Entity operationComponent : techFromDB.getTreeField("operationComponents")) {
            if (!operationComponent.getDataDefinition().callValidators(operationComponent)) {
                isValid = false;
                message = createMessageForValidationErrors(message, operationComponent);
            }
        }
        if (!isValid) {
            technology.addGlobalError("technologies.technology.validate.error.OperationTreeNotValid", message);
        }
=======
>>>>>>> dev
        return isValid;
    }

    private String createMessageForValidationErrors(final String message, final Entity entity) {
        List<ErrorMessage> errors = Lists.newArrayList();
        if (!entity.getErrors().isEmpty()) {
            errors.addAll(entity.getErrors().values());
        }
        if (!entity.getGlobalErrors().isEmpty()) {
            errors.addAll(entity.getGlobalErrors());
        }

        StringBuilder errorMessages = new StringBuilder();
        errorMessages.append(message).append("\n");
        for (ErrorMessage error : errors) {

            if (!error.getMessage().equals("qcadooView.validate.global.error.custom")) {
                String translatedErrorMessage = translationService.translate(error.getMessage(), Locale.getDefault(),
                        error.getVars());
                errorMessages.append("- ").append(translatedErrorMessage);
                errorMessages.append(",\n ");
            }
        }
        return errorMessages.toString();
    }

    public boolean checkTechnologyDefault(final DataDefinition dataDefinition, final Entity technology) {
        if (!technology.getBooleanField(MASTER)) {
            return true;
        }
        if (!hasInCorrectStateTechnologyForMaster(technology)) {
            technology.addError(dataDefinition.getField(MASTER),
                    "technologies.technology.validate.global.error.default.incorrectState");
            return false;
        }
        return true;
    }

    private boolean hasInCorrectStateTechnologyForMaster(final Entity technology) {
        return technology.getStringField(STATE).equals(TechnologyStateStringValues.ACCEPTED)
                || technology.getStringField(STATE).equals(TechnologyStateStringValues.OUTDATED);
    }

}
