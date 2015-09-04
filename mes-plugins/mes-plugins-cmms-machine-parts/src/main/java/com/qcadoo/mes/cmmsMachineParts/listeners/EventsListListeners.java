package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventContextService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class EventsListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private EventListeners eventListeners;

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
        Entity maintenanceEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT).get(selectedEntities.get(0).getId());

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
        Entity plannedEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(selectedEntities.get(0).getId());

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
