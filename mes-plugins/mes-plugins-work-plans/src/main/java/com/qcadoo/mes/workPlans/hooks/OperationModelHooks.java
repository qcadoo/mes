/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class OperationModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void copyColumnForProducts(final DataDefinition operationDD, final Entity operation) {
        if (!shouldPropagateValuesFromLowerInstance(operation)) {
            return;
        }

        ArrayList<Entity> operationInputColumns = Lists.newArrayList();
        for (Entity parameterInputColumn : getParameterHasManyField("parameterInputColumns")) {
            Entity columnForInputProducts = parameterInputColumn.getBelongsToField("columnForInputProducts");

            Entity operationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_OPERATION_INPUT_COLUMN).create();

            operationInputColumn.setField("columnForInputProducts", columnForInputProducts);

            operationInputColumns.add(operationInputColumn);
        }

        ArrayList<Entity> operationOutputColumns = Lists.newArrayList();
        for (Entity parameterOutputColumn : getParameterHasManyField("parameterOutputColumns")) {
            Entity columnForOutputProducts = parameterOutputColumn.getBelongsToField("columnForOutputProducts");

            Entity operationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                    WorkPlansConstants.MODEL_OPERATION_OUTPUT_COLUMN).create();

            operationOutputColumn.setField("columnForOutputProducts", columnForOutputProducts);

            operationOutputColumns.add(operationOutputColumn);
        }

        operation.setField("operationInputColumns", operationInputColumns);
        operation.setField("operationOutputColumns", operationOutputColumns);

    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity operation) {
        return (operation.getField("operationInputColumns") == null) && (operation.getField("operationOutputColumns") == null);
    }

    private List<Entity> getParameterHasManyField(final String fieldName) {
        List<Entity> hasManyFieldValue = parameterService.getParameter().getHasManyField(fieldName);
        if (hasManyFieldValue == null) {
            hasManyFieldValue = Lists.newArrayList();
        }
        return hasManyFieldValue;
    }
}
