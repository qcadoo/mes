package com.qcadoo.mes.cmmsMachineParts.controller.dataProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.BasicException;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionDTO;
import com.qcadoo.mes.cmmsMachineParts.dto.ActionForPlannedEventDTO;
import com.qcadoo.mes.cmmsMachineParts.dto.WorkerDTO;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;

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

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    public GridResponse<ActionForPlannedEventDTO> findAll(final Long plannedEventId, final String _sidx, final String _sord,
            int page, int perPage, ActionForPlannedEventDTO actionForPlannedEventDto) {
        String query = "SELECT %s FROM ( SELECT afpe.id AS id, a.name AS action, afpe.plannedevent_id AS plannedEvent, "
                + "s.name  || ' ' || s.surname || ' - ' || s.number AS responsibleWorker, "
                + "afpe.description AS description, afpe.state AS state, afpe.reason AS reason "
                + "FROM cmmsmachineparts_actionforplannedevent afpe JOIN cmmsmachineparts_action a ON a.id = afpe.action_id "
                + "LEFT JOIN basic_staff s ON s.id = afpe.responsibleworker_id "
                + "WHERE afpe.plannedevent_id = :plannedEventId %s) q ";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("plannedEventId", plannedEventId);

        GridResponse<ActionForPlannedEventDTO> gridResponse = lookupUtils.getGridResponse(query, _sidx, _sord, page, perPage,
                actionForPlannedEventDto, parameters);
        translateActionStates(gridResponse);
        return gridResponse;
    }

    private void translateActionStates(GridResponse<ActionForPlannedEventDTO> gridResponse) {
        List<ActionForPlannedEventDTO> rows = gridResponse.getRows();
        for (ActionForPlannedEventDTO action : rows) {
            String state = action.getState();
            if (state != null) {
                state = translationService.translate("cmmsMachineParts.actionForPlannedEvent.state.value." + state,
                        LocaleContextHolder.getLocale());
                action.setState(state);
            }
        }
    }

    public List<Map<String, String>> getActionStates() {
        return Lists.newArrayList("01correct", "02incorrect").stream().map(state -> {
            Map<String, String> states = new HashMap<>();
            states.put("key", state);
            states.put("value", translationService.translate("cmmsMachineParts.actionForPlannedEvent.state.value." + state,
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
            paramMap.put("query", '%' + q + '%');
            String query = generateQuery(plannedEventId) + " LIMIT 20";
            return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(ActionDTO.class));
        }
    }

    private String generateQuery(Long plannedEventId) {
        Entity plannedEvent = dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT)
                .get(plannedEventId);

        String query = "SELECT id, name AS code FROM cmmsmachineparts_action WHERE name ilike :query";

        Entity subassembly = plannedEvent.getBelongsToField(PlannedEventFields.SUBASSEMBLY);
        if (subassembly != null) {
            query = "SELECT * FROM ( SELECT id, name AS code FROM cmmsmachineparts_action "
                    + "WHERE id IN (SELECT action_id FROM jointable_action_subassembly WHERE subassembly_id = "
                    + "(SELECT subassembly_id FROM cmmsmachineparts_plannedevent WHERE id = :id)) "
                    + "OR id IN (SELECT action_id FROM jointable_action_workstationtype WHERE workstationtype_id = "
                    + "(SELECT workstationtype_id FROM basic_subassembly WHERE id IN("
                    + "SELECT subassembly_id FROM cmmsmachineparts_plannedevent WHERE id = :id)))"
                    + "OR appliesto IS NULL) q WHERE code ilike :query";
        } else {
            Entity workstation = plannedEvent.getBelongsToField(PlannedEventFields.WORKSTATION);
            if (workstation != null) {
                query = "SELECT * FROM ( SELECT id, name AS code FROM cmmsmachineparts_action "
                        + "WHERE id IN (SELECT action_id FROM jointable_action_workstation WHERE workstation_id = "
                        + "(SELECT workstation_id FROM cmmsmachineparts_plannedevent WHERE id = :id)) "
                        + "OR id IN (SELECT action_id FROM jointable_action_workstationtype WHERE workstationtype_id = "
                        + "(SELECT workstationtype_id FROM basic_workstation WHERE id IN("
                        + "SELECT workstation_id FROM cmmsmachineparts_plannedevent WHERE id = :id)))"
                        + "OR appliesto IS NULL) q WHERE code ilike :query";
            }
        }

        return query;
    }

    public DataResponse getActionsForObject(String q, Long plannedEventId) {
        List<AbstractDTO> entities = getActions(q, plannedEventId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", plannedEventId);
        return dataProvider.getDataResponse(q, generateQuery(plannedEventId), entities, paramMap);

    }

    public List<AbstractDTO> getWorkers(String q) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", '%' + q + '%');
        String query = "SELECT id, name  || ' ' || surname || ' - ' || number AS code FROM basic_staff WHERE name  || ' ' || surname || ' - ' || number ilike :q LIMIT 20;";
        return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(WorkerDTO.class));
    }

    public DataResponse getAllWorkers(String query) {
        List<AbstractDTO> entities = getWorkers(query);
        Map<String, Object> paramMap = new HashMap<>();
        return dataProvider.getDataResponse(query,
                "SELECT id, name  || ' ' || surname || ' - ' || number AS code FROM basic_staff WHERE name  || ' ' || surname || ' - ' || number ilike :query;",
                entities, paramMap);
    }

    private Long getWorkerId(String q) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", q);
        String query = "SELECT id FROM basic_staff WHERE name  || ' ' || surname || ' - ' || number = :q LIMIT 1;";
        return jdbcTemplate.query(query, paramMap, new ResultSetExtractor<Long>() {

            @Override
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getLong("id") : null;
            }
        });
    }

    private Long getActionId(String q) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", q);
        String query = "SELECT id FROM cmmsmachineparts_action WHERE name = :q LIMIT 1;";
        return jdbcTemplate.query(query, paramMap, new ResultSetExtractor<Long>() {

            @Override
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getLong("id") : null;
            }
        });
    }

    private DataDefinition getActionForPlannedEventDD() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_ACTION_PLANNED_EVENT);

    }

    public void create(ActionForPlannedEventDTO actionForPlannedEventDTO) {
        DataDefinition dataDefinition = getActionForPlannedEventDD();
        Entity actionForPlannedEvent = dataDefinition.create();
        updateActionForPlannedEvent(dataDefinition, actionForPlannedEvent, actionForPlannedEventDTO);
    }

    public void delete(Long id) {
        DataDefinition dataDefinition = getActionForPlannedEventDD();
        try {
            dataDefinition.delete(id);
        } catch (IllegalStateException exc) {
            throw new BasicException(
                    translationService.translate("actionsGrid.notification.entityInUse", LocaleContextHolder.getLocale()));
        }
    }

    public void update(Long id, ActionForPlannedEventDTO actionForPlannedEventDTO) {
        DataDefinition dataDefinition = getActionForPlannedEventDD();
        Entity actionForPlannedEvent = dataDefinition.get(id);
        updateActionForPlannedEvent(dataDefinition, actionForPlannedEvent, actionForPlannedEventDTO);
    }

    private void updateActionForPlannedEvent(DataDefinition dataDefinition, Entity actionForPlannedEvent,
            ActionForPlannedEventDTO actionForPlannedEventDTO) {
        actionForPlannedEvent.setField(ActionForPlannedEventFields.RESPONSIBLE_WORKER,
                getWorkerId(actionForPlannedEventDTO.getResponsibleWorker()));
        actionForPlannedEvent.setField(ActionForPlannedEventFields.ACTION, getActionId(actionForPlannedEventDTO.getAction()));
        actionForPlannedEvent.setField(ActionForPlannedEventFields.DESCRIPTION, actionForPlannedEventDTO.getDescription());
        actionForPlannedEvent.setField(ActionForPlannedEventFields.PLANNED_EVENT, actionForPlannedEventDTO.getPlannedEvent());
        actionForPlannedEvent.setField(ActionForPlannedEventFields.REASON, actionForPlannedEventDTO.getReason());
        actionForPlannedEvent.setField(ActionForPlannedEventFields.STATE, actionForPlannedEventDTO.getState());
        actionForPlannedEvent = dataDefinition.save(actionForPlannedEvent);
        if (!actionForPlannedEvent.isValid()) {
            StringBuilder errors = new StringBuilder();
            errors.append(actionForPlannedEvent.getGlobalErrors().stream()
                    .map(error -> translationService.translate(error.getMessage(), LocaleContextHolder.getLocale()))
                    .collect(Collectors.joining("\n")));
            if (errors.length() > 0) {
                errors.append("\n");
            }
            errors.append(actionForPlannedEvent.getErrors().entrySet().stream()
                    .map(entry -> translationService.translate(
                            "cmmsMachineParts.actionForPlannedEvent." + entry.getKey() + ".label",
                            LocaleContextHolder.getLocale()) + " - "
                    + translationService.translate(entry.getValue().getMessage(), LocaleContextHolder.getLocale()))
                    .collect(Collectors.joining("\n")));
            throw new BasicException(errors.toString());

        }
    }

    public boolean canEditActions(Long id) {
        Entity user = userService.getCurrentUserEntity();
        boolean hasProperRole = securityService.hasRole(user, "ROLE_PLANNED_EVENTS_BASIC_EDIT");

        Entity plannedEvent = dataDefinitionService
                .get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).get(id);
        String state = plannedEvent.getStringField(PlannedEventFields.STATE);
        boolean eventIsInProperState = !(PlannedEventStateStringValues.REALIZED.equals(state)
                || PlannedEventStateStringValues.CANCELED.equals(state));

        return hasProperRole && eventIsInProperState;
    }
}
