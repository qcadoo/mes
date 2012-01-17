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
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class OrderOperationModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyColumnForProducts(final DataDefinition operationDD, final Entity operation) {
        if (operation.getField("operationInputComponents") == null || operation.getField("operationOutputComponents") == null) {
            EntityList parameterInputComponents = getParameterHasManyField("parameterInputComponents");
            EntityList parameterOutputComponents = getParameterHasManyField("parameterOutputComponents");

            ArrayList<Entity> operationInputComponents = Lists.newArrayList();
            ArrayList<Entity> operationOutputComponents = Lists.newArrayList();

            if (parameterInputComponents != null) {
                for (Entity parameterInputComponent : parameterInputComponents) {
                    Entity columnForInputProducts = parameterInputComponent.getBelongsToField("columnForInputProducts");

                    Entity operationInputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_OPERATION_INPUT_COMPONENT).create();

                    operationInputComponent.setField("columnForInputProducts", columnForInputProducts);

                    operationInputComponents.add(operationInputComponent);
                }
            }

            if (parameterOutputComponents != null) {
                for (Entity parameterOutputComponent : parameterOutputComponents) {
                    Entity columnForOutputProducts = parameterOutputComponent.getBelongsToField("columnForOutputProducts");

                    Entity operationOutputComponent = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_OPERATION_OUTPUT_COMPONENT).create();

                    operationOutputComponent.setField("columnForOutputProducts", columnForOutputProducts);

                    operationOutputComponents.add(operationOutputComponent);
                }
            }

            operation.setField("operationInputComponents", operationInputComponents);
            operation.setField("operationOutputComponents", operationOutputComponents);
        }
    }

    public EntityList getParameterHasManyField(String fieldName) {
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();

        Entity parameter = null;

        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }
        if ((parameter == null) || (parameter.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return parameter.getHasManyField(fieldName);
        }
    }

    public EntityList getOperationHasManyField(Long operationId, String fieldName) {
        Entity operation = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).get(
                operationId);

        if ((operation == null) || (operation.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return operation.getHasManyField(fieldName);
        }
    }
}
