package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class EventsListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private EventListeners eventListeners;

    public void newEventAction(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String args[]) {
        viewDefinitionState.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/addNewEvent.html", false,
                true);
    }

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];

        // DataDefinition dataDefinition = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
        // CmmsMachinePartsConstants.MAINTENANCE_EVENT);
        // Long faultTypeId = null;
        // Entity maintenanceEvent = dataDefinition.create();
        // if (MaintenanceEventType.parseString(eventType).compareTo(MaintenanceEventType.PROPOSAL) == 0) {
        // maintenanceEvent.setField(MaintenanceEventFields.FAULT_TYPE, eventListeners.getDefaultFaultType());
        // faultTypeId = eventListeners.getDefaultFaultType().getId();
        // }
        // maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
        // maintenanceEvent.setField(MaintenanceEventFields.NUMBER, numberGeneratorService.generateNumber(
        // CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT));
        // maintenanceEvent = dataDefinition.save(maintenanceEvent);

        // Map<String, Object> parameters = Maps.newHashMap();
        // parameters.put("form.id", maintenanceEvent.getId());
        // viewDefinitionState.redirectTo(
        // "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true,
        // parameters);

        String url = "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER
                + "/maintenanceEventDetails.html?context={\"eventType\":\"" + eventType + "\"}";
        viewDefinitionState.redirectTo(url, false, true);
    }
}
