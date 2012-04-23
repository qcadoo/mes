/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OperationViewHooks {

    @Autowired
    private ParameterService parameterService;

    public final void setOperationDefaultValues(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        setOperationDefaultValues(view);
    }

    public final void setOperationDefaultValues(final ViewDefinitionState view) {
        FormComponent form = getForm(view);

        if (form.getEntityId() == null) {
            for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                FieldComponent field = getFieldComponent(view, workPlanParameter);
                Object parameterValue = parameterService.getParameter().getField(workPlanParameter);
                field.setFieldValue(parameterValue);
            }
        }
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

}
