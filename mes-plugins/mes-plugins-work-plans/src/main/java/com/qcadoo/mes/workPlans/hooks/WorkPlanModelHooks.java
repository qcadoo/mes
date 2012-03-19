/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
public class WorkPlanModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final boolean clearGeneratedOnCopy(final DataDefinition workPlanDD, final Entity workPlan) {
        workPlan.setField("fileName", null);
        workPlan.setField("generated", false);
        workPlan.setField("date", null);
        workPlan.setField("worker", null);

        return true;
    }

    public void copyColumnForOrders(final DataDefinition workPlanDD, final Entity workPlan) {
        if (!shouldPropagateValuesFromLowerInstance(workPlan)) {
            return;
        }

        EntityList parameterOrderColumns = getParameterHasManyField("parameterOrderColumns");

        ArrayList<Entity> workPlanOrderColumns = Lists.newArrayList();

        if (parameterOrderColumns != null) {
            for (Entity parameterOrderColumn : parameterOrderColumns) {
                Entity columnForOrders = parameterOrderColumn.getBelongsToField("columnForOrders");

                Entity workPlanOrderColumn = dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                        WorkPlansConstants.MODEL_WORK_PLAN_ORDER_COLUMN).create();

                workPlanOrderColumn.setField("columnForOrders", columnForOrders);

                workPlanOrderColumns.add(workPlanOrderColumn);
            }
        }

        workPlan.setField("workPlanOrderColumns", workPlanOrderColumns);
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity operation) {
        return (operation.getField("workPlanOrderColumns") == null);
    }

    private EntityList getParameterHasManyField(final String fieldName) {
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                .uniqueResult();

        if ((parameter == null) || (parameter.getHasManyField(fieldName) == null)) {
            return null;
        } else {
            return parameter.getHasManyField(fieldName);
        }
    }

}
