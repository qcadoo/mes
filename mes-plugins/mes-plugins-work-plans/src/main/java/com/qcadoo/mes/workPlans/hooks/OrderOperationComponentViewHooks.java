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

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderOperationComponentViewHooks {

    private Set<String> WORKPLAN_PARAMETERS = Sets.newHashSet("hideDescriptionInWorkPlans", "hideDetailsInWorkPlans",
            "hideTechnologyAndOrderInWorkPlans", "imageUrlInWorkPlan", "dontPrintInputProductsInWorkPlans",
            "dontPrintOutputProductsInWorkPlans");

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void setOrderOperationComponentDefaultValues(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        setOrderOperationComponentDefaultValues(view);
    }

    public final void setOrderOperationComponentDefaultValues(final ViewDefinitionState view) {
        FormComponent form = getForm(view);

        if (form.getEntityId() == null) {
            Long orderOperationComponentId = (Long) form.getEntityId();

            for (String workPlanParameter : WORKPLAN_PARAMETERS) {
                FieldComponent field = getFieldComponent(view, workPlanParameter);
                field.setFieldValue(getParameter(orderOperationComponentId, workPlanParameter));
            }
        }
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    public Object getParameter(Long orderOperationComponentId, String parameterName) {
        checkArgument(orderOperationComponentId != null, "Order Operation Component Id is null");

        Entity parameter = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.OPERATION_COMPONENTS).get(orderOperationComponentId);

        if ((parameter == null) || (parameter.getField(parameterName) == null)) {
            return null;
        } else {
            return parameter.getField(parameterName);
        }
    }
}
