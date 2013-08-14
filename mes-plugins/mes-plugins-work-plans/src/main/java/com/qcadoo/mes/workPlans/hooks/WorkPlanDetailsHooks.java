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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class WorkPlanDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public final void disableFormForGeneratedWorkPlan(final ViewDefinitionState view) {
        FormComponent workPlanForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(WorkPlanFields.GENERATED);

        if (workPlanForm == null) {
            return;
        }

        if (workPlanForm.getEntityId() == null) {
            view.getComponentByReference(WorkPlanFields.ORDERS).setEnabled(false);
            view.getComponentByReference(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS).setEnabled(false);
        } else {
            boolean isEnabled = ("0".equals(generatedField.getFieldValue()));

            view.getComponentByReference(WorkPlanFields.NAME).setEnabled(isEnabled);
            view.getComponentByReference(WorkPlanFields.TYPE).setEnabled(isEnabled);
            view.getComponentByReference(WorkPlanFields.DONT_PRINT_ORDERS_IN_WORK_PLANS).setEnabled(isEnabled);
            view.getComponentByReference(WorkPlanFields.ORDERS).setEnabled(isEnabled);
            view.getComponentByReference(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS).setEnabled(isEnabled);
        }
    }

    public void setWorkPlanDefaultValues(final ViewDefinitionState view) {
        FormComponent workPlanForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (workPlanForm.getEntityId() == null) {
            FieldComponent fieldComponent = (FieldComponent) view
                    .getComponentByReference(WorkPlanFields.DONT_PRINT_ORDERS_IN_WORK_PLANS);
            fieldComponent.setFieldValue(parameterService.getParameter().getField(
                    ParameterFieldsWP.DONT_PRINT_ORDERS_IN_WORK_PLANS));
        }
    }

}
