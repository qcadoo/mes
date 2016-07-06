package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.MachinePartDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.MaintenanceEventDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.StateChangeDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents.dto.WorkTimeDTO;
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

@Service public class MaintenanceEventsXLSDataProvider {

    @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired private DataDefinitionService dataDefinitionService;

    private final static String query =
            "SELECT maintenanceevent.id as id, \n" + "maintenanceevent.number as number, \n" + "maintenanceevent.type as type, \n"
                    + "factory.number as factoryNumber, \n" + "division.number as divisionNumber,\n"
                    + "productionline.number as productionLineNumber,\n" + "workstation.number as workstationNumber,\n"
                    + "subassembly.number as subassemblyNumber,\n" + "faulttype.name as faultTypeName,\n"
                    + "maintenanceevent.description as description,\n"
                    + "(personreceiving.surname || ' ' || personreceiving.name ) as personReceiving,\n"
                    + "sourcecost.number as sourceCost,\n" + "staffworktime.id as staffworkTimeId,\n"
                    + "(staffworktimestaff.surname || ' ' || staffworktimestaff.name ) as staffWorkTimeWorker,\n"
                    + "staffworktime.laborTime as staffWorkTimeLaborTime,\n" + "docpos.id as machinePartId,\n"
                    + "product.number as partNumber,\n" + "product.name as partName,\n" + "warehouse.number as warehouseNumber,\n"
                    + "product.lastpurchasecost * docpos.quantity as lastPurchaseCost,\n"
                    + "docpos.price * docpos.quantity as priceFromDocumentPosition,\n" + "docpos.price as priceFromPosition,\n"
                    + "docpos.quantity as quantityFromPosition,\n"
                    + "docpos.quantity as partPlannedQuantity,\n" + "product.unit as partUnit,\n"
                    + "maintenanceevent.createdate as createDate,\n" + "maintenanceevent.createuser as createUser,\n"
                    + "statechange.id as stateChangeId, \n" + "statechange.dateandtime as stateChangeDateAndTime, \n"
                    + "statechange.sourcestate as stateChangeSourceState, \n"
                    + "statechange.targetstate as stateChangeTargetState , \n" + "statechange.status as stateStatus,\n"
                    + "statechange.worker as stateWorker,\n" + "maintenanceevent.state as state,\n"
                    + "maintenanceevent.solutiondescription as solutionDescription\n"
                    + "FROM cmmsmachineparts_maintenanceevent maintenanceevent\n"
                    + "LEFT JOIN cmmsmachineparts_staffworktime staffworktime ON maintenanceevent.id = staffworktime.maintenanceevent_id\n"
                    + "LEFT JOIN basic_staff staffworktimestaff ON staffworktime.worker_id = staffworktimestaff.id\n"
                    + "LEFT JOIN materialflowresources_document doc ON maintenanceevent.id = doc.maintenanceevent_id and doc.state = '02accepted'\n"
                    + "LEFT JOIN materialflowresources_position docpos ON docpos.document_id = doc.id\n"
                    + "LEFT JOIN basic_product product ON docpos.product_id = product.id\n"
                    + "LEFT JOIN materialflow_location warehouse ON doc.locationfrom_id = warehouse.id\n"
                    + "LEFT JOIN cmmsmachineparts_maintenanceeventstatechange statechange ON maintenanceevent.id = statechange.maintenanceevent_id\n"
                    + "LEFT JOIN basic_factory factory ON maintenanceevent.factory_id = factory.id\n"
                    + "LEFT JOIN basic_division division ON maintenanceevent.division_id = division.id\n"
                    + "LEFT JOIN productionlines_productionline productionline ON maintenanceevent.productionline_id = productionline.id\n"
                    + "LEFT JOIN basic_workstation workstation ON maintenanceevent.workstation_id = workstation.id\n"
                    + "LEFT JOIN basic_subassembly subassembly ON maintenanceevent.subassembly_id = subassembly.id\n"
                    + "LEFT JOIN basic_staff personreceiving ON maintenanceevent.personreceiving_id = personreceiving.id\n"
                    + "LEFT JOIN basic_faulttype faulttype ON maintenanceevent.faulttype_id = faulttype.id\n"
                    + "LEFT JOIN cmmsmachineparts_sourcecost sourcecost ON maintenanceevent.sourcecost_id = sourcecost.id\n";

    private final static String ORDER_BY = " ORDER BY maintenanceevent.number";

    public List<MaintenanceEventDTO> getEvents(final Map<String, Object> filters) {
        List<MaintenanceEventDTO> events = Lists.newArrayList();
        String _query = buildQuery(filters);
        events = jdbcTemplate.query(_query, new BeanPropertyRowMapper(MaintenanceEventDTO.class));
        List<MaintenanceEventDTO> finalEvents = processResults(events);
        finalEvents.sort((e1, e2) -> e1.getNumber().compareTo(e2.getNumber()));
        return finalEvents;
    }

    private List<MaintenanceEventDTO> processResults(List<MaintenanceEventDTO> events) {
        Multimap<Long, MaintenanceEventDTO> eventsMap = ArrayListMultimap.create();
        eventsMap = Multimaps.index(events, new Function<MaintenanceEventDTO, Long>() {

            @Override public Long apply(MaintenanceEventDTO maintenanceEventDTO) {
                return maintenanceEventDTO.getId();
            }
        });

        Map<Long, MaintenanceEventDTO> _events = Maps.newHashMap();
        for (Long eID : eventsMap.keySet()) {
            _events.put(eID, eventsMap.get(eID).iterator().next());
        }
        List<MaintenanceEventDTO> finalEvents = Lists.newArrayList(_events.values());
        for (MaintenanceEventDTO maintenanceEventDTO : finalEvents) {
            List<MaintenanceEventDTO> _subEvents = Lists.newArrayList(eventsMap.get(maintenanceEventDTO.getId()));

            List<WorkTimeDTO> workTimes = fillWorkTimes(_subEvents);
            maintenanceEventDTO.setWorkTimes(workTimes);

            List<MachinePartDTO> parts = fillParts(_subEvents);
            maintenanceEventDTO.setMachineParts(parts);

            List<StateChangeDTO> hists = fillHist(_subEvents);
            maintenanceEventDTO.setStateChange(hists);

        }
        return finalEvents;
    }

    private List<StateChangeDTO> fillHist(List<MaintenanceEventDTO> subEvents) {
        return subEvents.stream().map(e -> toHist(e)).filter(Objects::nonNull)
                .filter(e -> "03successful".equals(e.getStateStatus())).distinct().collect(Collectors.toList());

    }

    private StateChangeDTO toHist(MaintenanceEventDTO e) {
        if (e.getStateChangeId() == null) {
            return null;
        }
        StateChangeDTO state = new StateChangeDTO();
        state.setStateChangeDateAndTime(e.getStateChangeDateAndTime());
        state.setStateChangeId(e.getStateChangeId());
        state.setStateChangeSourceState(e.getStateChangeSourceState());
        state.setStateChangeTargetState(e.getStateChangeTargetState());
        state.setStateStatus(e.getStateStatus());
        state.setStateWorker(e.getStateWorker());

        return state;
    }

    private List<MachinePartDTO> fillParts(final List<MaintenanceEventDTO> subEvents) {
        return subEvents.stream().map(e -> toPart(e)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private MachinePartDTO toPart(MaintenanceEventDTO e) {
        if (e.getMachinePartId() == null) {
            return null;
        }
        MachinePartDTO part = new MachinePartDTO();
        part.setMachinePartId(e.getMachinePartId());
        part.setPartName(e.getPartName());
        part.setPartNumber(e.getPartNumber());
        part.setPartPlannedQuantity(e.getPartPlannedQuantity());
        part.setPartUnit(e.getPartUnit());
        part.setWarehouseNumber(e.getWarehouseNumber());
        if(e.getPriceFromDocumentPosition() != null){
            part.setValue(e.getPriceFromDocumentPosition());
        } else {
            part.setValue(e.getLastPurchaseCost());
        }
        return part;
    }

    private List<WorkTimeDTO> fillWorkTimes(final List<MaintenanceEventDTO> subEvents) {
        return subEvents.stream().map(e -> toWorkTime(e)).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private WorkTimeDTO toWorkTime(MaintenanceEventDTO e) {
        if (e.getStaffworkTimeId() == null) {
            return null;
        }
        WorkTimeDTO workTime = new WorkTimeDTO();
        workTime.setStaffworkTimeId(e.getStaffworkTimeId());
        workTime.setStaffWorkTimeLaborTime(e.getStaffWorkTimeLaborTime());
        workTime.setStaffWorkTimeWorker(e.getStaffWorkTimeWorker());
        return workTime;
    }

    private String buildQuery(final Map<String, Object> filters) {
        Map<String, String> _filter = (Map<String, String>) filters.get("filtersMap");
        Long helperModelId = Long.valueOf(_filter.get("MAINTENANCE_EVENT_FILTER"));
        Entity helperEntity = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, "maintenanceEventXLSHelper")
                .get(helperModelId);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(query);
        if (StringUtils.isNoneBlank(helperEntity.getStringField("query")) && helperEntity.getStringField("query").length() > 1) {
            queryBuilder.append(" where ").append(MaintenanceEventsFilterUtils.processFilter(helperEntity.getStringField("query")));
        }
        queryBuilder.append(ORDER_BY);
        return queryBuilder.toString();
    }
}