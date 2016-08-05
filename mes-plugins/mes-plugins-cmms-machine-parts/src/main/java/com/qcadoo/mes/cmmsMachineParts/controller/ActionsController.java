package com.qcadoo.mes.cmmsMachineParts.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.cmmsMachineParts.controller.dataProvider.ActionsForPlannedEventDataProvider;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionForPlannedEventDto;

@Controller
@RequestMapping("/integration/rest/actions")
public class ActionsController {

    @Autowired
    private ActionsForPlannedEventDataProvider dataProvider;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable Long id) {
        Map<String, Object> config = new HashMap<>();
        config.put("readOnly", false);
        return config;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<ActionForPlannedEventDto> findAll(@PathVariable Long id, @RequestParam String sidx,
            @RequestParam String sord, @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage, ActionForPlannedEventDto actionForPlannedEventDto) {

        ActionForPlannedEventDto action = new ActionForPlannedEventDto("czynność", "Jan Kowalski", "jakiś opis", "correct",
                "bo tak");
        GridResponse<ActionForPlannedEventDto> response = new GridResponse<>();
        response.setRows(Lists.newArrayList(action));
        response.setPage(1);
        response.setRecords(1);
        response.setTotal(1);
        return response;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "states")
    public List<Map<String, String>> getTypeOfPallets() {
        return dataProvider.getActionStates();
    }
}
