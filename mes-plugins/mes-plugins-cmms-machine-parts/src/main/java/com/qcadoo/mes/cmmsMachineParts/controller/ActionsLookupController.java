package com.qcadoo.mes.cmmsMachineParts.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionDTO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Controller
@RequestMapping(value = "action")
public class ActionsLookupController extends BasicLookupController<ActionDTO> {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Override
    protected String getQueryForRecords(final Long context) {
        
        Entity plannedEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(context);
        
        String query = "SELECT %s FROM ( SELECT id, name AS code FROM cmmsmachineparts_action %s) q";

        Entity subassembly = plannedEvent.getBelongsToField(PlannedEventFields.SUBASSEMBLY);
        if(subassembly != null) {
            query = "SELECT %s FROM ( SELECT id, name AS code FROM cmmsmachineparts_action "
                    + "WHERE id IN (SELECT action_id FROM jointable_action_subassembly WHERE subassembly_id = "
                    + "(SELECT subassembly_id FROM cmmsmachineparts_plannedevent WHERE id = :plannedEventId)) "
                    + "OR id IN (SELECT action_id FROM jointable_action_workstationtype WHERE workstationtype_id = "
                    + "(SELECT workstationtype_id FROM basic_subassembly WHERE id IN("
                    + "SELECT subassembly_id FROM cmmsmachineparts_plannedevent WHERE id = :plannedEventId)))"
                    + "OR appliesto IS NULL %s) q";
        } else {
            Entity workstation = plannedEvent.getBelongsToField(PlannedEventFields.WORKSTATION);
            if(workstation != null) {
                query = "SELECT %s FROM ( SELECT id, name AS code FROM cmmsmachineparts_action "
                        + "WHERE id IN (SELECT action_id FROM jointable_action_workstation WHERE workstation_id = "
                        + "(SELECT workstation_id FROM cmmsmachineparts_plannedevent WHERE id = :plannedEventId)) "
                        + "OR id IN (SELECT action_id FROM jointable_action_workstationtype WHERE workstationtype_id = "
                        + "(SELECT workstationtype_id FROM basic_workstation WHERE id IN("
                        + "SELECT workstation_id FROM cmmsmachineparts_plannedevent WHERE id = :plannedEventId)))"
                        + "OR appliesto IS NULL %s) q";
            }
        }
        

        return query;
    }

    @Override
    protected Map<String, Object> getQueryParameters(Long context, ActionDTO actionDTO) {
        Map<String, Object> params = new HashMap<>();
        // params.put("plannedEventId", actionDTO.getPlannedEventId());
        params.put("plannedEventId", context);
        return params;
    }

    @Override
    protected List<String> getGridFields() {
        return Arrays.asList("code");
    }

    @Override
    protected String getRecordName() {
        return "action";
    }
}
