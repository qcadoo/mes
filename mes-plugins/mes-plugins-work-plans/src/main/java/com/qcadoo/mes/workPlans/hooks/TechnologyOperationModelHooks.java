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

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

@Service
public class TechnologyOperationModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyColumnForProducts(final DataDefinition technologyOperationDD, final Entity technologyOperation) {
        if ((technologyOperation.getField("technologyOperationInputComponents") == null)
                && (technologyOperation.getField("technologyOperationOutputComponents") == null)) {
            Entity operation = technologyOperation.getBelongsToField("operation");

            EntityList operationInputComponents = getOperationHasManyField(operation.getId(), "operationInputComponents");
            EntityList operationOutputComponents = getOperationHasManyField(operation.getId(), "operationOutputComponents");

            ArrayList<Entity> technologyOperationInputComponents = Lists.newArrayList();
            ArrayList<Entity> technologyOperationOutputComponents = Lists.newArrayList();

            if (operationInputComponents != null) {
                for (Entity operationInputComponent : operationInputComponents) {
                    Entity columnForInputProducts = operationInputComponent.getBelongsToField("columnForInputProducts");

                    Entity technologyOperationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_INPUT_COMPONENT).create();

                    technologyOperationInputComponent.setField("columnForInputProducts", columnForInputProducts);

                    technologyOperationInputComponents.add(technologyOperationInputComponent);
                }
            }

            if (operationOutputComponents != null) {
                for (Entity operationOutputComponent : operationOutputComponents) {
                    Entity columnForOutputProducts = operationOutputComponent.getBelongsToField("columnForOutputProducts");

                    Entity technologyOperationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_TECHNOLOGY_OPERATION_OUTPUT_COMPONENT).create();

                    technologyOperationOutputComponent.setField("columnForOutputProducts", columnForOutputProducts);

                    technologyOperationOutputComponents.add(technologyOperationOutputComponent);
                }
            }

            technologyOperation.setField("technologyOperationInputComponents", technologyOperationInputComponents);
            technologyOperation.setField("technologyOperationOutputComponents", technologyOperationOutputComponents);
        }
    }

    public EntityList getOperationHasManyField(Long operationId, String fieldName) {
        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get(operationId);

        if ((operation == null) || (operation.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return operation.getHasManyField(fieldName);
        }
    }

}
