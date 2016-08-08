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

import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.cmmsMachineParts.controller.dataProvider.ActionsForPlannedEventService;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionForPlannedEventDTO;

@Controller
@RequestMapping("/integration/rest/actions")
public class ActionsController {

    @Autowired
    private ActionsForPlannedEventService dataProvider;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable Long id) {
        Map<String, Object> config = new HashMap<>();
        config.put("readOnly", false);
        return config;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<ActionForPlannedEventDTO> findAll(@PathVariable Long id, @RequestParam String sidx,
            @RequestParam String sord, @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
            @RequestParam(value = "rows") int perPage, ActionForPlannedEventDTO actionForPlannedEventDto) {

        return dataProvider.findAll(id, sidx, sord, page, perPage, actionForPlannedEventDto);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "states")
    public List<Map<String, String>> getStates() {
        return dataProvider.getActionStates();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "actions")
    public DataResponse getActionsForObject(@RequestParam("query") String query, @RequestParam("context") Long plannedEventId) {
        return dataProvider.getActionsForObject(query, plannedEventId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "workers")
    public DataResponse getWorkers(@RequestParam("query") String query) {
        return dataProvider.getAllWorkers(query);
    }

}
