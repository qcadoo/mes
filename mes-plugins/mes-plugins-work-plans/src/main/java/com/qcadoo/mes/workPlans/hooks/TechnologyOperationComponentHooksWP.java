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
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.OperationFieldsWP;
import com.qcadoo.mes.workPlans.constants.OperationInputColumnFields;
import com.qcadoo.mes.workPlans.constants.OperationOutputColumnFields;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationComponentFieldsWP;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationInputColumnFields;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationOutputColumnFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentHooksWP {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private WorkPlansService workPlansService;

    public void onCreate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        copyColumnForProducts(technologyOperationComponentDD, technologyOperationComponent);
    }

    private void copyColumnForProducts(final DataDefinition technologyOperationComponentDD,
            final Entity technologyOperationComponent) {
        if (!shouldPropagateValuesFromLowerInstance(technologyOperationComponent)) {
            return;
        }

        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (operation == null) {
            return;
        }

        List<Entity> technologyOperationInputColumns = Lists.newArrayList();
        List<Entity> technologyOperationOutputColumns = Lists.newArrayList();

        for (Entity operationInputColumn : getOperationHasManyField(operation.getId(), OperationFieldsWP.OPERATION_INPUT_COLUMNS)) {
            Entity columnForInputProducts = operationInputColumn
                    .getBelongsToField(OperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS);

            Entity technologyOperationInputColumn = workPlansService.getTechnologyOperationInputColumnDD().create();

            technologyOperationInputColumn.setField(TechnologyOperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS,
                    columnForInputProducts);

            technologyOperationInputColumns.add(technologyOperationInputColumn);
        }

        for (Entity operationOutputColumn : getOperationHasManyField(operation.getId(),
                OperationFieldsWP.OPERATION_OUTPUT_COLUMNS)) {
            Entity columnForOutputProducts = operationOutputColumn
                    .getBelongsToField(OperationOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS);

            Entity technologyOperationOutputColumn = workPlansService.getTechnologyOperationOutputColumnDD().create();

            technologyOperationOutputColumn.setField(TechnologyOperationOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS,
                    columnForOutputProducts);

            technologyOperationOutputColumns.add(technologyOperationOutputColumn);
        }

        technologyOperationComponent.setField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_INPUT_COLUMNS,
                technologyOperationInputColumns);
        technologyOperationComponent.setField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_OUTPUT_COLUMNS,
                technologyOperationOutputColumns);
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity technologyOperationComponent) {
        return (technologyOperationComponent.getField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_INPUT_COLUMNS) == null)
                && (technologyOperationComponent
                        .getField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_OUTPUT_COLUMNS) == null);
    }

    private List<Entity> getOperationHasManyField(final Long operationId, final String fieldName) {
        List<Entity> hasManyFieldValue = null;

        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get(operationId);

        if ((operation == null) || (operation.getHasManyField(fieldName) == null)) {
            hasManyFieldValue = Lists.newArrayList();
        } else {
            hasManyFieldValue = operation.getHasManyField(fieldName);
        }

        return hasManyFieldValue;
    }

}
