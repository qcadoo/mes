package com.qcadoo.mes.cmmsMachineParts.controller.dataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionDTO;
import com.qcadoo.mes.cmmsMachineParts.dto.WorkerDTO;

@Repository
public class ActionsForPlannedEventService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private LookupUtils lookupUtils;

    @Autowired
    private DataProvider dataProvider;

    public List<Map<String, String>> getActionStates() {
        return Lists.newArrayList("01correct", "02incorrect").stream().map(state -> {
            Map<String, String> states = new HashMap<>();
            states.put("value", state);
            states.put("key", translationService.translate("cmmsMachineParts.actionForPlannedEvent.state.value." + state,
                    LocaleContextHolder.getLocale()));

            return states;
        }).collect(Collectors.toList());
    }

    public List<AbstractDTO> getActions(String q, Long plannedEventId) {

        if (plannedEventId == null) {
            return Lists.newArrayList();
        } else {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", plannedEventId);
            paramMap.put("q", '%' + q + '%');
            String query = "SELECT id, name AS code FROM cmmsmachineparts_action WHERE name ilike :q LIMIT 20;";
            return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(ActionDTO.class));
        }
    }

    public DataResponse getActionsForObject(String q, Long plannedEventId) {
        List<AbstractDTO> entities = getActions(q, plannedEventId);
        Map<String, Object> paramMap = new HashMap<>();
        return dataProvider.getDataResponse(q, "SELECT id, name AS code FROM cmmsmachineparts_action WHERE name ilike :query;",
                entities, paramMap);

    }

    public List<AbstractDTO> getWorkers(String q) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", '%' + q + '%');
        String query = "SELECT id, name  || ' ' || surname AS code FROM basic_staff WHERE name  || ' ' || surname ilike :q LIMIT 20;";
        return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(WorkerDTO.class));
    }

    public DataResponse getAllWorkers(String query) {
        List<AbstractDTO> entities = getWorkers(query);
        Map<String, Object> paramMap = new HashMap<>();
        return dataProvider.getDataResponse(query, "SELECT id, name  || ' ' || surname AS code FROM basic_staff WHERE name  || ' ' || surname ilike :query;",
                entities, paramMap);
    }
}
