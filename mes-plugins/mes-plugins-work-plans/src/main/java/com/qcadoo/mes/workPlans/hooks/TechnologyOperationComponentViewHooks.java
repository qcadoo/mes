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

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TechnologyOperationComponentViewHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void setTechnologyOperationComponentDefaultValues(final ViewDefinitionState view,
            final ComponentState component, final String[] args) {
        setTechnologyOperationComponentDefaultValues(view);
    }

    public final void setTechnologyOperationComponentDefaultValues(final ViewDefinitionState view) {
        FieldComponent operation = getFieldComponent(view, "operation");

        if (operation.getFieldValue() != null) {
            Long operationId = (Long) operation.getFieldValue();

            for (String workPlanParameter : WorkPlansConstants.WORKPLAN_PARAMETERS) {
                FieldComponent field = getFieldComponent(view, workPlanParameter);
                field.setFieldValue(getOperationParameter(operationId, workPlanParameter));
            }
        }
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    public Object getOperationParameter(Long operationId, String parameterName) {
        checkArgument(operationId != null, "Operation Id is null");

        Entity parameter = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get(operationId);

        if ((parameter == null) || (parameter.getField(parameterName) == null)) {
            return null;
        } else {
            return parameter.getField(parameterName);
        }
    }

}
