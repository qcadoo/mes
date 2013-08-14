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
package com.qcadoo.mes.operationalTasksForOrders.listeners;

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.DESCRIPTION;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.NAME;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCTION_LINE;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.TYPE_TASK;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksFieldsOTFOF.ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.COMMENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.hooks.OperationalTasksDetailsHooksOTFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksDetailsListenersOTFO {

    private static final String L_FORM = "form";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    @Autowired
    private OperationalTasksDetailsHooksOTFO detailsHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void disabledFieldWhenOrderTypeIsSelected(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        detailsHooks.disabledFieldWhenOrderTypeIsSelected(viewDefinitionState);
    }

    public void setProductionLineFromOrderAndClearOperation(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        Entity order = ((LookupComponent) viewDefinitionState.getComponentByReference(ORDER)).getEntity();
        LookupComponent tocLookup = (LookupComponent) viewDefinitionState
                .getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);
        tocLookup.setFieldValue(null);
        tocLookup.requestComponentUpdateState();
        FieldComponent productionLine = (FieldComponent) viewDefinitionState.getComponentByReference(PRODUCTION_LINE);
        if (order == null) {
            productionLine.setFieldValue(null);
        } else {
            productionLine.setFieldValue(order.getBelongsToField(PRODUCTION_LINE).getId());
        }
        productionLine.requestComponentUpdateState();
    }

    public void setOperationalNameAndDescription(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Entity techOperComp = ((LookupComponent) viewDefinitionState.getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT))
                .getEntity();
        FieldComponent description = (FieldComponent) viewDefinitionState.getComponentByReference(DESCRIPTION);
        FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference(NAME);
        if (techOperComp == null) {
            description.setFieldValue(null);
            name.setFieldValue(null);
        } else {
            description.setFieldValue(techOperComp.getStringField(COMMENT));
            name.setFieldValue(techOperComp.getBelongsToField(OPERATION).getStringField(NAME));
        }
        description.requestComponentUpdateState();
        name.requestComponentUpdateState();
    }

    public final void showOperationalTasksWithOrder(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent operationTaskForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity operationTask = operationTaskForm.getEntity();

        if (operationTask.getId() == null) {
            return;
        }

        Entity order = operationTask.getBelongsToField(ORDER);

        if (order == null) {
            return;
        }

        String orderNumber = order.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderNumber", orderNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "operationalTask.operationalTasks");

        String url = "../page/operationalTasks/operationalTasksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOrder(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        Entity order = ((LookupComponent) viewState.getComponentByReference(ORDER)).getEntity();
        if (order == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", order.getId());

        String url = "../page/orders/orderDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public final void showOperationParameter(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Entity techOperComp = ((LookupComponent) viewState.getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT)).getEntity();
        if (techOperComp == null) {
            return;
        }
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", techOperComp.getId());

        String url = "../page/technologies/technologyOperationComponentDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public void disabledButtons(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooks.disabledButtons(view);
    }

    public void setTOCOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long operationalTaskId = (Long) state.getFieldValue();
        if (operationalTaskId != null) {
            FieldComponent type = (FieldComponent) view.getComponentByReference(TYPE_TASK);
            if (type.getFieldValue().equals("02executionOperationInOrder")) {
                LookupComponent tocLookup = (LookupComponent) view.getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);
                Entity toc = tocLookup.getEntity();
                Entity operationalTask = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                        OperationalTasksConstants.MODEL_OPERATIONAL_TASK).get(operationalTaskId);

                Entity tOCOperationalTask = operationalTask.getBelongsToField("techOperCompOperationalTasks");

                if (tOCOperationalTask == null) {
                    DataDefinition techOperCompOperationalTaskDD = dataDefinitionService.get(
                            OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                            OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS);
                    Entity techOperCompOperationalTask = techOperCompOperationalTaskDD.create();
                    techOperCompOperationalTask.setField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT, toc);
                    techOperCompOperationalTask = techOperCompOperationalTask.getDataDefinition().save(
                            techOperCompOperationalTask);
                    operationalTask.setField("techOperCompOperationalTasks", techOperCompOperationalTask);
                    operationalTask = operationalTask.getDataDefinition().save(operationalTask);
                } else {
                    Entity techOperCompOperationalTask = dataDefinitionService.get(
                            OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                            OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS).get(
                            tOCOperationalTask.getId());
                    techOperCompOperationalTask.setField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT, toc);
                    techOperCompOperationalTask = techOperCompOperationalTask.getDataDefinition().save(
                            techOperCompOperationalTask);
                }

            }
        }

    }

}
