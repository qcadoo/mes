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

@Service
public class OperationModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyColumnForProducts(final DataDefinition operationDD, final Entity operation) {
        if ((operation.getField("operationInputColumns") == null) && (operation.getField("operationOutputColumns") == null)) {
            EntityList parameterInputColumns = getParameterHasManyField("parameterInputColumns");
            EntityList parameterOutputColumns = getParameterHasManyField("parameterOutputColumns");

            ArrayList<Entity> operationInputColumns = Lists.newArrayList();
            ArrayList<Entity> operationOutputColumns = Lists.newArrayList();

            if (parameterInputColumns != null) {
                for (Entity parameterInputColumn : parameterInputColumns) {
                    Entity columnForInputProducts = parameterInputColumn.getBelongsToField("columnForInputProducts");

                    Entity operationInputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_OPERATION_INPUT_COLUMN).create();

                    operationInputColumn.setField("columnForInputProducts", columnForInputProducts);

                    operationInputColumns.add(operationInputColumn);
                }
            }

            if (parameterOutputColumns != null) {
                for (Entity parameterOutputColumn : parameterOutputColumns) {
                    Entity columnForOutputProducts = parameterOutputColumn.getBelongsToField("columnForOutputProducts");

                    Entity operationOutputColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                            WorkPlansConstants.MODEL_OPERATION_OUTPUT_COLUMN).create();

                    operationOutputColumn.setField("columnForOutputProducts", columnForOutputProducts);

                    operationOutputColumns.add(operationOutputColumn);
                }
            }

            operation.setField("operationInputColumns", operationInputColumns);
            operation.setField("operationOutputColumns", operationOutputColumns);
        }
    }

    private EntityList getParameterHasManyField(String fieldName) {
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                .uniqueResult();

        if ((parameter == null) || (parameter.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return parameter.getHasManyField(fieldName);
        }
    }

}
