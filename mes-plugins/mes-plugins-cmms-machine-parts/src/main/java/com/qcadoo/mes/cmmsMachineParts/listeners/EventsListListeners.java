/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventContextService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventContextFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.internal.components.grid.GridComponentFilterException;
import com.qcadoo.view.internal.components.grid.GridComponentFilterSQLUtils;

@Service
public class EventsListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MaintenanceEventContextService maintenanceEventContextService;

    @Autowired
    private MaintenanceEventService maintenanceEventService;

    public void newEventAction(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String args[]) {
        viewDefinitionState.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/addNewEvent.html", false,
                true);
    }

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];
        long maintenanceContextId = ((FormComponent) viewDefinitionState.getComponentByReference("form")).getEntityId();

        String url = "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html";
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("eventType", eventType);
        parameters.put("maintenanceEventContext", maintenanceContextId);
        viewDefinitionState.redirectTo(url, false, true, parameters);
    }

    public void confirmContext(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String args[]) {
        maintenanceEventContextService.confirmOrChangeContext(viewDefinitionState, triggerState, args);
    }

    public void onSelectedEventChange(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String args[]) {
        maintenanceEventContextService.onSelectedEventChange(viewDefinitionState);
    }

    public void printXlsReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {

        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        Map<String, String> filter = grid.getFilters();
        String filterQ = "";
        try {
            filterQ = GridComponentFilterSQLUtils.addFilters(filter, grid.getColumns(), "event", dataDefinitionService
                    .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT));
        } catch (GridComponentFilterException e) {
            filterQ = "";
        }

        String contextFilter = "";
        Entity context = maintenanceEventContextService.getCurrentContext(view, state, args);
        if (context.getBooleanField(MaintenanceEventContextFields.CONFIRMED)) {
            Entity factory = context.getBelongsToField(MaintenanceEventContextFields.FACTORY);
            if (factory != null) {
                contextFilter += " factory.id = " + factory.getId();
            }
            Entity division = context.getBelongsToField(MaintenanceEventContextFields.DIVISION);
            if (division != null) {
                if (contextFilter.length() > 1) {
                    contextFilter += " AND";
                }
                contextFilter += " division.id = " + division.getId();

            }
            if (filterQ.length() > 1) {
                if (contextFilter.length() > 1) {
                    filterQ = contextFilter + " AND " + filterQ;
                }
            } else {
                filterQ = contextFilter;
            }
        }

        Entity plannedEventXLSHelper = dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, "plannedEventXLSHelper").create();
        plannedEventXLSHelper.setField("query", filterQ);
        plannedEventXLSHelper = plannedEventXLSHelper.getDataDefinition().save(plannedEventXLSHelper);

        Map<String, Long> filters = Maps.newHashMap();
        filters.put("PLANED_EVENT_FILTER", plannedEventXLSHelper.getId());

        String filtersInJson = new JSONObject(filters).toString();
        StringBuffer redirectUrl = new StringBuffer();
        redirectUrl.append("/cmmsMachineParts/plannedEvents.xls");
        redirectUrl.append("?");
        redirectUrl.append("filters=");
        redirectUrl.append(filtersInJson);
        view.redirectTo(redirectUrl.toString(), true, false);
    }

    private void generate() {

        List<Entity> factories = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_FACTORY).find()
                .list().getEntities();
        List<Entity> divisions = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DIVISION).find()
                .list().getEntities();
        for (int i = 0; i < 10000; i++) {
            Entity afterReviewEvent = dataDefinitionService
                    .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).create();
            String number = numberGeneratorService.generateNumber(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                    CmmsMachinePartsConstants.MODEL_PLANNED_EVENT);
            afterReviewEvent.setField(PlannedEventFields.NUMBER, number);
            afterReviewEvent.setField(PlannedEventFields.TYPE, PlannedEventType.AFTER_REVIEW.getStringValue());
            afterReviewEvent.setField(PlannedEventFields.FACTORY, factories.get(0));
            afterReviewEvent.setField(PlannedEventFields.DIVISION, divisions.get(0));

            afterReviewEvent.setField(PlannedEventFields.BASED_ON, PlannedEventBasedOn.DATE.getStringValue());
            afterReviewEvent.setField(PlannedEventFields.AFTER_REVIEW, true);
            afterReviewEvent.setField(PlannedEventFields.REQUIRES_SHUTDOWN, false);
            afterReviewEvent.setField(PlannedEventFields.PLANNED_SEPARATELY, false);
            afterReviewEvent.getDataDefinition().save(afterReviewEvent);
        }
    }

    public void addPlannedEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String args[]) {
        long plannedContextId = ((FormComponent) viewDefinitionState.getComponentByReference("form")).getEntityId();

        String url = "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/plannedEventDetails.html";
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("plannedEventContext", plannedContextId);
        viewDefinitionState.redirectTo(url, false, true, parameters);
    }

    public void showPlannedEvent(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        List<Entity> selectedEntities = grid.getSelectedEntities();
        if (selectedEntities.isEmpty()) {
            return;
        }
        Entity maintenanceEvent = dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT)
                .get(selectedEntities.get(0).getId());

        Optional<Entity> plannedEvent = maintenanceEventService.getPlannedEventForMaintenanceEvent(maintenanceEvent);
        if (plannedEvent.isPresent()) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", plannedEvent.get().getId());
            view.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/plannedEventDetails.html", false, true,
                    parameters);
        }

    }

    public void showMaintenanceEvent(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        List<Entity> selectedEntities = grid.getSelectedEntities();
        if (selectedEntities.isEmpty()) {
            return;
        }
        Entity plannedEvent = dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT)
                .get(selectedEntities.get(0).getId());

        Entity maintenanceEvent = plannedEvent.getBelongsToField(PlannedEventFields.MAINTENANCE_EVENT);
        if (maintenanceEvent != null) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", maintenanceEvent.getId());
            view.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false,
                    true, parameters);
        } else {
            view.addMessage("cmmsMachineParts.plannedEventsList.eventWithoutMaintenanceEvent", ComponentState.MessageType.INFO);
        }

    }

}
