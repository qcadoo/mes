package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
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

        DataDefinition dataDefinition = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MAINTENANCE_EVENT);
        Entity maintenanceEvent = dataDefinition.create();
        if (MaintenanceEventType.parseString(eventType).compareTo(MaintenanceEventType.PROPOSAL) == 0) {
            maintenanceEvent.setField(MaintenanceEventFields.FAULT_TYPE, eventListeners.getDefaultFaultType());
        }
        maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
        maintenanceEvent.setField(MaintenanceEventFields.NUMBER, numberGeneratorService.generateNumber(
                CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT));
        maintenanceEvent = dataDefinition.save(maintenanceEvent);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", maintenanceEvent.getId());
        viewDefinitionState.redirectTo(
                "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true,
                parameters);
    }

}
