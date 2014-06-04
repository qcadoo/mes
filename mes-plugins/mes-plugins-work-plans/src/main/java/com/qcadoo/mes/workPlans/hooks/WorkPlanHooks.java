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
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.ParameterOrderColumnFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanOrderColumnFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WorkPlanHooks {

    @Autowired
    private WorkPlansService workPlansService;

    @Autowired
    private ParameterService parameterService;

    public void onCreate(final DataDefinition workPlanDD, final Entity workPlan) {
        copyColumnForOrders(workPlan);
    }

    public void onCopy(final DataDefinition workPlanDD, final Entity workPlan) {
        clearGeneratedOnCopy(workPlan);
    }

    private void copyColumnForOrders(final Entity workPlan) {
        if (!shouldPropagateValuesFromLowerInstance(workPlan)) {
            return;
        }

        List<Entity> workPlanOrderColumns = Lists.newArrayList();

        for (Entity parameterOrderColumn : getParameterHasManyField(ParameterFieldsWP.PARAMETER_ORDER_COLUMNS)) {
            Entity columnForOrders = parameterOrderColumn.getBelongsToField(ParameterOrderColumnFields.COLUMN_FOR_ORDERS);

            Entity workPlanOrderColumn = workPlansService.getWorkPlanOrderColumnDD().create();

            workPlanOrderColumn.setField(WorkPlanOrderColumnFields.COLUMN_FOR_ORDERS, columnForOrders);

            workPlanOrderColumns.add(workPlanOrderColumn);
        }

        workPlan.setField(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS, workPlanOrderColumns);
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity workPlan) {
        return (workPlan.getField(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS) == null);
    }

    private List<Entity> getParameterHasManyField(final String fieldName) {
        List<Entity> hasManyFieldValue = parameterService.getParameter().getHasManyField(fieldName);

        if (hasManyFieldValue == null) {
            hasManyFieldValue = Lists.newArrayList();
        }

        return hasManyFieldValue;
    }

    private void clearGeneratedOnCopy(final Entity workPlan) {
        workPlan.setField(WorkPlanFields.FILE_NAME, null);
        workPlan.setField(WorkPlanFields.GENERATED, false);
        workPlan.setField(WorkPlanFields.DATE, null);
        workPlan.setField(WorkPlanFields.WORKER, null);
    }

}
