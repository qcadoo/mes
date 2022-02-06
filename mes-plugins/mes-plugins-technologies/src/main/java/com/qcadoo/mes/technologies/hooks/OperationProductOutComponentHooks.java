/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.validators.TechnologyTreeValidators;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.Objects;

@Service
public class OperationProductOutComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidators technologyTreeValidators;

    @Autowired
    private TranslationService translationService;

    public boolean validatesWith(final DataDefinition operationProductInComponentDD, final Entity operationProductInComponent) {
        boolean isValid = true;

        isValid = isValid && technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(operationProductInComponentDD,
                operationProductInComponent);
        isValid = isValid && technologyService.invalidateIfAlreadyInTheSameOperation(operationProductInComponentDD,
                operationProductInComponent);

        return isValid;
    }

    public void onSave(final DataDefinition operationProductOutComponentDD, final Entity operationProductOutComponent) {
        if(Objects.isNull(operationProductOutComponent.getField(OperationProductOutComponentFields.WASTE))) {
            operationProductOutComponent.setField(OperationProductOutComponentFields.WASTE, false);
        }
    }
}
