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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OperationalTaskDetailsHooksOTFO {

    private static final String L_WINDOW = "window";

    private static final String L_FORM = "form";

    private static final String L_ORDER = "order";

    private static final String L_TECH_INST_OPER_COMP = "techInstOperComp";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_SHOW_ORDER = "showOrder";

    private static final String L_SHOW_OPERATION_PARAMETER = "showOperationParameter";

    private static final String L_SHOW_OPERATIONAL_TASKS_WITH_ORDER = "showOperationalTasksWithOrder";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void disabledFieldWhenOrderTypeIsSelected(final ViewDefinitionState view) {
        FieldComponent typeTaskField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE_TASK);

        String typeTask = (String) typeTaskField.getFieldValue();

        List<String> referenceBasicFields = Lists.newArrayList(OperationalTaskFields.NAME, OperationalTaskFields.PRODUCTION_LINE,
                OperationalTaskFields.DESCRIPTION);
        List<String> extendFields = Lists.newArrayList(OperationalTaskFieldsOTFO.ORDER, L_TECHNOLOGY_OPERATION_COMPONENT);

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskOtherCase(typeTask)) {
            changedStateField(view, referenceBasicFields, true);
            changedStateField(view, extendFields, false);
            clearFieldValue(view, extendFields);
        } else {
            changedStateField(view, referenceBasicFields, false);
            changedStateField(view, extendFields, true);
        }
    }

    private void changedStateField(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
            fieldComponent.setEnabled(enabled);
        }
    }

    private void clearFieldValue(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
            fieldComponent.setFieldValue(null);
            fieldComponent.requestComponentUpdateState();
        }
    }

    public void disabledButtons(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        FieldComponent typeTaskField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE_TASK);

        String typeTask = (String) typeTaskField.getFieldValue();

        boolean isOperationalTaskTypeTaskExecutionOperationInOrder = operationalTasksForOrdersService
                .isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask);

        RibbonGroup order = window.getRibbon().getGroupByName(L_ORDER);
        RibbonGroup techInstOperComp = window.getRibbon().getGroupByName(L_TECH_INST_OPER_COMP);
        RibbonGroup operationalTasks = window.getRibbon().getGroupByName(L_OPERATIONAL_TASKS);

        RibbonActionItem showOrder = order.getItemByName(L_SHOW_ORDER);
        RibbonActionItem showOperationParameter = techInstOperComp.getItemByName(L_SHOW_OPERATION_PARAMETER);
        RibbonActionItem showOperationalTasksWithOrder = operationalTasks.getItemByName(L_SHOW_OPERATIONAL_TASKS_WITH_ORDER);

        showOrder.setEnabled(isOperationalTaskTypeTaskExecutionOperationInOrder);
        showOrder.requestUpdate(true);

        showOperationParameter.setEnabled(isOperationalTaskTypeTaskExecutionOperationInOrder);
        showOperationParameter.requestUpdate(true);

        showOperationalTasksWithOrder.setEnabled(isOperationalTaskTypeTaskExecutionOperationInOrder);
        showOperationalTasksWithOrder.requestUpdate(true);
    }

    public void setTechnology(final ViewDefinitionState view) {
        FieldComponent technologyField = (FieldComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology != null) {
            technologyField.setFieldValue(technology.getId());
            technologyField.requestComponentUpdateState();
        }
    }

    public void setTechnologyOperationComponent(final ViewDefinitionState view) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent typeTaskField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE_TASK);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);

        Long operationalTaskId = operationalTaskForm.getEntityId();

        if (operationalTaskId == null) {
            return;
        }

        String typeTask = (String) typeTaskField.getFieldValue();

        if (operationalTasksForOrdersService.isOperationalTaskTypeTaskExecutionOperationInOrder(typeTask)) {
            Entity operationalTask = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                    OperationalTasksConstants.MODEL_OPERATIONAL_TASK).get(operationalTaskId);

            Entity techOperCompOperationalTasks = operationalTask
                    .getBelongsToField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASKS);

            Entity technologyOperationComponent = techOperCompOperationalTasks
                    .getBelongsToField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT);

            if ((technologyOperationComponentLookup.getEntity() == null) && (orderLookup.getEntity() != null)) {
                technologyOperationComponentLookup.setFieldValue(technologyOperationComponent.getId());
                technologyOperationComponentLookup.requestComponentUpdateState();
            }
        } else {
            technologyOperationComponentLookup.setFieldValue(null);
            technologyOperationComponentLookup.requestComponentUpdateState();
        }
    }

}
