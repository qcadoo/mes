package com.qcadoo.mes.cmmsMachineParts.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.basic.BasicLookupController;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionDTO;

@Controller
@RequestMapping(value = "action")
public class ActionsLookupController extends BasicLookupController<ActionDTO> {

    @Override
    protected String getQueryForRecords(final Long context) {
        String query = "SELECT %s FROM ( SELECT id, name AS code FROM cmmsmachineparts_action %s) q";

        return query;
    }

    @Override
    protected Map<String, Object> getQueryParameters(Long context, ActionDTO actionDTO) {
        Map<String, Object> params = new HashMap<>();
        params.put("plannedEventId", actionDTO.getPlannedEventId());
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
