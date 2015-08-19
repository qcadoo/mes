package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventContextService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
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

}
