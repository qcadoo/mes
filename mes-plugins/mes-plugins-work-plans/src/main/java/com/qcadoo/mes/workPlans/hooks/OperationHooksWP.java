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
package com.qcadoo.mes.workPlans.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.OperationFieldsWP;
import com.qcadoo.mes.workPlans.constants.OperationInputColumnFields;
import com.qcadoo.mes.workPlans.constants.OperationOutputColumnFields;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.ParameterInputColumnFields;
import com.qcadoo.mes.workPlans.constants.ParameterOutputColumnFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationHooksWP {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private WorkPlansService workPlansService;

    public void onCreate(final DataDefinition operationDD, final Entity operation) {
        copyColumnForProducts(operationDD, operation);
    }

    private void copyColumnForProducts(final DataDefinition operationDD, final Entity operation) {
        if (!shouldPropagateValuesFromLowerInstance(operation)) {
            return;
        }

        List<Entity> operationInputColumns = Lists.newArrayList();
        List<Entity> operationOutputColumns = Lists.newArrayList();

        for (Entity parameterInputColumn : getParameterHasManyField(ParameterFieldsWP.PARAMETER_INPUT_COLUMNS)) {
            Entity columnForInputProducts = parameterInputColumn
                    .getBelongsToField(ParameterInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS);

            Entity operationInputColumn = workPlansService.getOperationInputColumnDD().create();

            operationInputColumn.setField(OperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS, columnForInputProducts);

            operationInputColumns.add(operationInputColumn);
        }

        for (Entity parameterOutputColumn : getParameterHasManyField(ParameterFieldsWP.PARAMETER_OUTPUT_COLUMNS)) {
            Entity columnForOutputProducts = parameterOutputColumn
                    .getBelongsToField(ParameterOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS);

            Entity operationOutputColumn = workPlansService.getOperationOutputColumnDD().create();

            operationOutputColumn.setField(OperationOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS, columnForOutputProducts);

            operationOutputColumns.add(operationOutputColumn);
        }

        operation.setField(OperationFieldsWP.OPERATION_INPUT_COLUMNS, operationInputColumns);
        operation.setField(OperationFieldsWP.OPERATION_OUTPUT_COLUMNS, operationOutputColumns);
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity operation) {
        return (operation.getField(OperationFieldsWP.OPERATION_INPUT_COLUMNS) == null)
                && (operation.getField(OperationFieldsWP.OPERATION_OUTPUT_COLUMNS) == null);
    }

    private List<Entity> getParameterHasManyField(final String fieldName) {
        List<Entity> hasManyFieldValue = parameterService.getParameter().getHasManyField(fieldName);

        if (hasManyFieldValue == null) {
            hasManyFieldValue = Lists.newArrayList();
        }

        return hasManyFieldValue;
    }

}
