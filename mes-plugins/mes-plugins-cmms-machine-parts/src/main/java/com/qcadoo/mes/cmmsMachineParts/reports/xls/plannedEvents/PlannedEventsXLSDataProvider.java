package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.google.common.collect.*;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.MachinePartForEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventRealizationDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents.dto.PlannedEventStateChangeDTO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PlannedEventsXLSDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private final static String ORDER_BY = " ORDER BY event.id";

    private final static String QUERY = "SELECT event.id as id, event.\"number\" as number, event.type as type, event.description as description, event.state as state, event.ownername as ownerName,\n"
            + "event.plannedseparately as plannedSeparately, event.requiresshutdown as requiresShutdown, event.basedon as basedOn, event.solutiondescription as solutionDescription, \n"
            + "event.date as date, event.counter as counter, event.countertolerance as counterTolerance, event.effectivecounter as effectiveCounter, \n"
            + "event.duration as duration, event.effectiveduration as effectiveDuration, event.isdeadline as isDeadline, event.createdate as createdate, \n"
            + "event.startdate as startDate, event.finishdate as finishDate, event.countertolerance as countertolerance, event.createuser as createuser, \n"
            + "factory.number as factoryNumber, division.number as divisionNumber, productionline.number as productionLineNumber, workstation.number as workstatioNnumber, subassembly.number as subassemblynumber, \n"
            + "company.number as companyNumber, sourcecost.number as sourceCostNumber,\n"
            + "docpos.id as machinePartId, product.name as machinePartName, product.number as machinePartNumber, product.unit as machinePartUnit, docpos.quantity as machinePartPlannedQuantity, \n"
            + "product.lastpurchasecost * docpos.quantity as lastPurchaseCost,\n"
            + "docpos.price * docpos.quantity as priceFromDocumentPosition,\n"
            + "docpos.price as priceFromPosition,\n"
            + "docpos.quantity as quantityFromPosition,"
            + "realization.id as realizationId, worker.name as realizationWorkerName, worker.surname as realizationWorkerSurname, realization.duration as realizationDuration, \n"
            + "hist.id as stateChangeId, hist.dateandtime as stateChangeDateAndTime, hist.sourcestate as stateChangeSourceState, hist.targetstate as stateChangeTargetState , "
            + "hist.status as stateStatus, hist.worker as stateWorker \n"
            + "  FROM cmmsmachineparts_plannedevent event\n"
            + "   LEFT JOIN cmmsmachineparts_plannedeventstatechange hist ON event.id = hist.plannedevent_id\n"
            + "   LEFT JOIN cmmsmachineparts_plannedeventrealization realization ON event.id = realization.plannedevent_id\n"
            + "   LEFT JOIN basic_factory factory ON event.factory_id = factory.id\n"
            + "   LEFT JOIN basic_division division ON event.division_id = division.id\n"
            + "   LEFT JOIN productionlines_productionline productionline ON event.productionline_id = productionline.id\n"
            + "   LEFT JOIN basic_workstation workstation ON event.workstation_id = workstation.id\n"
            + "   LEFT JOIN basic_subassembly subassembly ON event.subassembly_id = subassembly.id\n"
            + "   LEFT JOIN basic_company company ON event.company_id = company.id\n"
            + "   LEFT JOIN cmmsmachineparts_sourcecost sourcecost ON event.sourcecost_id = sourcecost.id\n"
            + "   LEFT JOIN basic_staff worker ON realization.worker_id = worker.id\n"
            + "LEFT JOIN materialflowresources_document doc ON event.id = doc.plannedevent_id and doc.state = '02accepted'\n"
            + "LEFT JOIN materialflowresources_position docpos ON docpos.document_id = doc.id\n"
            + "LEFT JOIN basic_product product ON docpos.product_id = product.id\n";

    public List<PlannedEventDTO> getEvents(final Map<String, Object> filters) {
        List<PlannedEventDTO> events;
        Map<String, String> _filter = (Map<String, String>) filters.get("filtersMap");
        Long helperModelId = Long.valueOf(_filter.get("PLANED_EVENT_FILTER"));
        Entity helperEntity = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, "plannedEventXLSHelper")
                .get(helperModelId);
        String _query = QUERY;
        if (StringUtils.isNoneBlank(helperEntity.getStringField("query")) && helperEntity.getStringField("query").length() > 1) {
            _query = QUERY + " where " + PlannedEventsFilterUtils.processFilter(helperEntity.getStringField("query"));
        }
        _query = _query + ORDER_BY;
        events = jdbcTemplate.query(_query, new BeanPropertyRowMapper(PlannedEventDTO.class));

        Multimap<Long, PlannedEventDTO> eventsMap = Multimaps.index(events, PlannedEventDTO::getId);

        Map<Long, PlannedEventDTO> _events = Maps.newHashMap();
        for (Long eID : eventsMap.keySet()) {
            _events.put(eID, eventsMap.get(eID).iterator().next());
        }
        List<PlannedEventDTO> finalEvents = Lists.newArrayList(_events.values());
        for (PlannedEventDTO plannedEventDTO : finalEvents) {
            List<PlannedEventDTO> _subEvents = Lists.newArrayList(eventsMap.get(plannedEventDTO.getId()));

            List<PlannedEventRealizationDTO> realizationDTOs = fillRealizations(plannedEventDTO, _subEvents);
            plannedEventDTO.setRealizations(realizationDTOs);

            List<MachinePartForEventDTO> partsDTOs = fillParts(plannedEventDTO, _subEvents);
            plannedEventDTO.setParts(partsDTOs);

            List<PlannedEventStateChangeDTO> hists = fillHist(plannedEventDTO, _subEvents);
            plannedEventDTO.setStateChanges(hists);

        }
        finalEvents.sort((e1, e2) -> e1.getNumber().compareTo(e2.getNumber()));
        return finalEvents;
    }

    private List<PlannedEventStateChangeDTO> fillHist(Object plannedEventDTO, List<PlannedEventDTO> subEvents) {
        return subEvents.stream().map(e -> toHist(e)).filter(Objects::nonNull)
                .filter(e -> "03successful".equals(e.getStateStatus())).distinct().collect(Collectors.toList());

    }

    private PlannedEventStateChangeDTO toHist(PlannedEventDTO e) {
        if (e.getStateChangeId() == null) {
            return null;
        }
        PlannedEventStateChangeDTO state = new PlannedEventStateChangeDTO();
        state.setStateChangeDateAndTime(e.getStateChangeDateAndTime());
        state.setStateChangeId(e.getStateChangeId());
        state.setStateChangeSourceState(e.getStateChangeSourceState());
        state.setStateChangeTargetState(e.getStateChangeTargetState());
        state.setStateStatus(e.getStateStatus());
        state.setStateWorker(e.getStateWorker());
        return state;
    }

    private List<MachinePartForEventDTO> fillParts(PlannedEventDTO plannedEventDTO, List<PlannedEventDTO> subEvents) {
        return subEvents.stream().map(e -> toPart(e)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private MachinePartForEventDTO toPart(PlannedEventDTO e) {
        if (e.getMachinePartId() == null) {
            return null;
        }
        MachinePartForEventDTO part = new MachinePartForEventDTO();
        part.setMachinePartId(e.getMachinePartId());
        part.setMachinePartName(e.getMachinePartName());
        part.setMachinePartNumber(e.getMachinePartNumber());
        part.setMachinePartPlannedQuantity(e.getMachinePartPlannedQuantity());
        part.setMachinePartUnit(e.getMachinePartUnit());
        if(e.getPriceFromDocumentPosition() != null){
            part.setValue(e.getPriceFromDocumentPosition());
        } else {
            part.setValue(e.getLastPurchaseCost());
        }
        return part;
    }

    private List<PlannedEventRealizationDTO> fillRealizations(PlannedEventDTO plannedEventDTO, List<PlannedEventDTO> subEvents) {
        return subEvents.stream().map(e -> toRealization(e)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private PlannedEventRealizationDTO toRealization(PlannedEventDTO e) {
        if (e.getRealizationId() == null) {
            return null;
        }
        PlannedEventRealizationDTO plannedEventRealizationDTO = new PlannedEventRealizationDTO();
        plannedEventRealizationDTO.setRealizationDuration(e.getRealizationDuration());
        plannedEventRealizationDTO.setRealizationId(e.getRealizationId());
        plannedEventRealizationDTO.setRealizationWorkerName(e.getRealizationWorkerName());
        plannedEventRealizationDTO.setRealizationWorkerSurname(e.getRealizationWorkerSurname());
        return plannedEventRealizationDTO;
    }
}
