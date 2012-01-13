/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.1.1
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
package com.qcadoo.mes.workPlans.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

@Service
public class TechnologyOperationComponentModelValidators {

    public final boolean checkIfColumnForInputProductsIsNotAlreadyUsed(final DataDefinition inputComponentDD,
            final Entity inputComponent) {
        Entity columnForInputProducts = inputComponent.getBelongsToField("columnForInputProducts");
        Entity parameter = inputComponent.getBelongsToField("technologyOperationComponent");

        EntityList parameterInputComponents = parameter.getHasManyField("technologyOperationInputComponents");

        if (inputComponent.getId() == null) {
            if (columnForInputProducts == null) {
                return true;
            } else {
                if (parameterInputComponents == null) {
                    return true;
                } else {
                    for (Entity parameterInputComponent : parameterInputComponents) {
                        Entity column = parameterInputComponent.getBelongsToField("columnForInputProducts");
                        if (column.getId().equals(columnForInputProducts.getId())) {
                            inputComponent.addError(inputComponentDD.getField("columnForInputProducts"),
                                    "workPlans.columnForInputProducts.message.columnForInputProductsIsAlreadyUsed");

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public final boolean checkIfColumnForOutputProductsIsNotAlreadyUsed(final DataDefinition outputComponentDD,
            final Entity outputComponent) {
        Entity columnForOutputProducts = outputComponent.getBelongsToField("columnForOutputProducts");
        Entity parameter = outputComponent.getBelongsToField("technologyOperationComponent");

        EntityList parameterOutputComponents = parameter.getHasManyField("technologyOperationOutputComponents");

        if (outputComponent.getId() == null) {
            if (columnForOutputProducts == null) {
                return true;
            } else {
                if (parameterOutputComponents == null) {
                    return true;
                } else {
                    for (Entity parameterOutputComponent : parameterOutputComponents) {
                        Entity column = parameterOutputComponent.getBelongsToField("columnForOutputProducts");
                        if (column.getId().equals(columnForOutputProducts.getId())) {
                            outputComponent.addError(outputComponentDD.getField("columnForOutputProducts"),
                                    "workPlans.columnForOutputProducts.message.columnForOutputProductsIsAlreadyUsed");

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

}
