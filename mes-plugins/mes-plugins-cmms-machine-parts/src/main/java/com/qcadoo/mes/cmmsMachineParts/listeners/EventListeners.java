package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.hooks.MachinePartDetailsHooks;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.internal.components.tree.TreeComponentState;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class EventListeners {

    private Long factoryStructureId;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];

        DataDefinition dataDefinition = dataDefinitionService.
                get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT);
        Entity maintenanceEvent = dataDefinition.create();

        maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
//        maintenanceEvent.setField(MaintenanceEventFields.FACTORY_STRUCTURE, factoryStructureId);
        maintenanceEvent.setField(MaintenanceEventFields.NUMBER, numberGeneratorService.generateNumber(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT));
        maintenanceEvent = dataDefinition.save(maintenanceEvent);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", maintenanceEvent.getId());
        viewDefinitionState.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true, parameters);
    }

    public final void selectOnTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        TreeComponentState treeComponentState = (TreeComponentState) state;
        factoryStructureId = treeComponentState.getSelectedEntityId();
    }
}
