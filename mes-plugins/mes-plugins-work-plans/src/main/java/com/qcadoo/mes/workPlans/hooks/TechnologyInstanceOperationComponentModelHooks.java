/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
public class TechnologyInstanceOperationComponentModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyColumnForProducts(final DataDefinition technologyInstanceOperationComponentDD,
            final Entity technologyInstanceOperationComponent) {

        if (!shouldPropagateValuesFromLowerInstance(technologyInstanceOperationComponent)) {
            return;
        }

        Entity technologyOperationComponent = technologyInstanceOperationComponent
                .getBelongsToField("technologyOperationComponent");

        if (technologyOperationComponent == null) {
            return;
        }

        for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
            technologyInstanceOperationComponent.setField(workPlanParameter,
                    getTechnologyOperationField(technologyOperationComponent.getId(), workPlanParameter));
        }

        EntityList technologyOperationInputColumns = getTechnologyOperationHasManyField(technologyOperationComponent.getId(),
                "technologyOperationInputColumns");
        EntityList technologyOperationOutputColumns = getTechnologyOperationHasManyField(technologyOperationComponent.getId(),
                "technologyOperationOutputColumns");

        ArrayList<Entity> orderOperationInputColumns = Lists.newArrayList();
        ArrayList<Entity> orderOperationOutputColumns = Lists.newArrayList();

        if (technologyOperationInputColumns != null) {
            for (Entity technologyOperationInputColumn : technologyOperationInputColumns) {
                Entity columnForInputProducts = technologyOperationInputColumn.getBelongsToField("columnForInputProducts");

                Entity orderOperationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_ORDER_OPERATION_INPUT_COLUMN).create();

                orderOperationInputColumn.setField("columnForInputProducts", columnForInputProducts);

                orderOperationInputColumns.add(orderOperationInputColumn);
            }
        }

        if (technologyOperationOutputColumns != null) {
            for (Entity technologyOperationOutputColumn : technologyOperationOutputColumns) {
                Entity columnForOutputProducts = technologyOperationOutputColumn.getBelongsToField("columnForOutputProducts");

                Entity orderOperationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_ORDER_OPERATION_OUTPUT_COLUMN).create();

                orderOperationOutputColumn.setField("columnForOutputProducts", columnForOutputProducts);

                orderOperationOutputColumns.add(orderOperationOutputColumn);
            }
        }

        technologyInstanceOperationComponent.setField("orderOperationInputColumns", orderOperationInputColumns);
        technologyInstanceOperationComponent.setField("orderOperationOutputColumns", orderOperationOutputColumns);

    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity technologyInstanceOperationComponent) {
        for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
            if (technologyInstanceOperationComponent.getField(workPlanParameter) != null) {
                return false;
            }
        }

        return (technologyInstanceOperationComponent.getField("orderOperationInputColumns") == null)
                && (technologyInstanceOperationComponent.getField("orderOperationOutputColumns") == null);
    }

    private EntityList getTechnologyOperationHasManyField(final Long operationId, final String fieldName) {
        Entity technologyOperation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(operationId);

        if ((technologyOperation == null) || (technologyOperation.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return technologyOperation.getHasManyField(fieldName);
        }
    }

    private Object getTechnologyOperationField(final Long technologyOperationId, final String fieldName) {
        Entity technologyOperation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperationId);

        if ((technologyOperation == null) || (technologyOperation.getField(fieldName) == null)) {
            return null;
        } else {
            return technologyOperation.getField(fieldName);
        }
    }
}
