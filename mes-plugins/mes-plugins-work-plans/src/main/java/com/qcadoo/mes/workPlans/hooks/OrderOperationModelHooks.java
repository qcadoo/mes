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
public class OrderOperationModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyColumnForProducts(final DataDefinition orderOperationDD, final Entity orderOperation) {
        if ((orderOperation.getField("orderOperationInputComponents") == null)
                && (orderOperation.getField("orderOperationOutputComponents") == null)) {
            Entity technologyOperation = orderOperation.getBelongsToField("technologyOperationComponent");

            for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                orderOperation.setField(workPlanParameter,
                        getTechnologyOperationField(technologyOperation.getId(), workPlanParameter));
            }

            EntityList technologyOperationInputComponents = getTechnologyOperationHasManyField(technologyOperation.getId(),
                    "technologyOperationInputComponents");
            EntityList technologyOperationOutputComponents = getTechnologyOperationHasManyField(technologyOperation.getId(),
                    "technologyOperationOutputComponents");

            ArrayList<Entity> orderOperationInputComponents = Lists.newArrayList();
            ArrayList<Entity> orderOperationOutputComponents = Lists.newArrayList();

            if (technologyOperationInputComponents != null) {
                for (Entity technologyOperationInputComponent : technologyOperationInputComponents) {
                    Entity columnForInputProducts = technologyOperationInputComponent.getBelongsToField("columnForInputProducts");

                    Entity orderOperationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_ORDER_OPERATION_INPUT_COMPONENT).create();

                    orderOperationInputComponent.setField("columnForInputProducts", columnForInputProducts);

                    orderOperationInputComponents.add(orderOperationInputComponent);
                }
            }

            if (technologyOperationOutputComponents != null) {
                for (Entity technologyOperationOutputComponent : technologyOperationOutputComponents) {
                    Entity columnForOutputProducts = technologyOperationOutputComponent
                            .getBelongsToField("columnForOutputProducts");

                    Entity orderOperationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_ORDER_OPERATION_OUTPUT_COMPONENT).create();

                    orderOperationOutputComponent.setField("columnForOutputProducts", columnForOutputProducts);

                    orderOperationOutputComponents.add(orderOperationOutputComponent);
                }
            }

            orderOperation.setField("orderOperationInputComponents", orderOperationInputComponents);
            orderOperation.setField("orderOperationOutputComponents", orderOperationOutputComponents);
        }
    }

    public EntityList getTechnologyOperationHasManyField(Long operationId, String fieldName) {
        Entity technologyOperation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(operationId);

        if ((technologyOperation == null) || (technologyOperation.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return technologyOperation.getHasManyField(fieldName);
        }
    }

    public Object getTechnologyOperationField(Long technologyOperationId, String fieldName) {
        Entity technologyOperation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperationId);

        if ((technologyOperation == null) || (technologyOperation.getField(fieldName) == null)) {
            return null;
        } else {
            return technologyOperation.getField(fieldName);
        }
    }
}
