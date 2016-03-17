package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.TimeUsageReportFilterFields;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto.TimeUsageDTO;

@Service
public class TimeUsageXlsDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final static String plannedEventQuery = "SELECT staff.surname || ' ' || staff.name AS worker, staff.id AS worker_id, 'planned' AS event_type,\n"
            + "realization.startdate,\n"
            + "event.number,\n"
            + "event.type,\n"
            + "event.state,\n"
            + "COALESCE(subassembly.number, workstation.number, line.number, division.number, factory.number) AS object,\n"
            + "(SELECT CASE WHEN EXISTS (SELECT id FROM cmmsmachineparts_machinepartforevent WHERE plannedevent_id=event.id) THEN 'tak' ELSE 'nie' END) as parts,\n"
            + "event.solutiondescription AS description,\n"
            + "realization.duration AS duration,\n"
            + "(SELECT MIN(dateandtime) FROM cmmsmachineparts_plannedeventstatechange WHERE plannedevent_id = event.id and targetstate='04inRealization') AS registeredStart,\n"
            + "(SELECT MAX(dateandtime) FROM cmmsmachineparts_plannedeventstatechange WHERE plannedevent_id = event.id and targetstate='08inEditing') AS registeredEnd\n"
            + "FROM (\n"
            + "SELECT worker_id, plannedevent_id, sum(duration) AS duration, (startdate - interval '6 hours')::date as startdate FROM cmmsmachineparts_plannedeventrealization WHERE confirmed='true' GROUP BY 1,2,4) AS realization\n"
            + "LEFT JOIN basic_staff staff ON realization.worker_id=staff.id\n"
            + "LEFT JOIN cmmsmachineparts_plannedevent event ON plannedevent_id=event.id\n"
            + "LEFT JOIN productionlines_productionline line ON event.productionline_id=line.id\n"
            + "LEFT JOIN basic_subassembly subassembly ON event.subassembly_id=subassembly.id\n"
            + "LEFT JOIN basic_workstation workstation ON event.workstation_id=workstation.id\n"
            + "LEFT JOIN basic_division division ON event.division_id=division.id\n"
            + "LEFT JOIN basic_factory factory ON event.factory_id=factory.id\n";

    private final static String maintenanceEventQuery = "SELECT staff.surname || ' ' || staff.name AS worker, staff.id AS worker_id, 'maintenance' AS event_type,\n"
            + "realization.startdate,\n"
            + "event.number,\n"
            + "event.type,\n"
            + "event.state,\n"
            + "COALESCE(subassembly.number, workstation.number, line.number, division.number, factory.number) AS object,\n"
            + "(SELECT COALESCE(ARRAY_TO_STRING(ARRAY_AGG(name),', '),'nie') FROM basic_product WHERE id IN(SELECT machinepart_id FROM cmmsmachineparts_machinepartforevent WHERE maintenanceevent_id=event.id)) as parts,\n"
            + "event.solutiondescription AS description,\n"
            + "realization.duration AS duration,\n"
            + "(SELECT MIN(dateandtime) FROM cmmsmachineparts_maintenanceeventstatechange WHERE maintenanceevent_id = event.id and targetstate='02inProgress') AS registeredStart,\n"
            + "(SELECT MAX(dateandtime) FROM cmmsmachineparts_maintenanceeventstatechange WHERE maintenanceevent_id = event.id and targetstate='03edited') AS registeredEnd\n"
            + "FROM (\n"
            + "SELECT worker_id, maintenanceevent_id, sum(labortime) AS duration, (effectiveexecutiontimestart - interval '6 hours')::date as startdate FROM cmmsmachineparts_staffworktime GROUP BY 1,2,4) AS realization\n"
            + "LEFT JOIN basic_staff staff ON realization.worker_id=staff.id\n"
            + "LEFT JOIN cmmsmachineparts_maintenanceevent event ON maintenanceevent_id=event.id\n"
            + "LEFT JOIN productionlines_productionline line ON event.productionline_id=line.id\n"
            + "LEFT JOIN basic_subassembly subassembly ON event.subassembly_id=subassembly.id\n"
            + "LEFT JOIN basic_workstation workstation ON event.workstation_id=workstation.id\n"
            + "LEFT JOIN basic_division division ON event.division_id=division.id\n"
            + "LEFT JOIN basic_factory factory ON event.factory_id=factory.id\n";

    public List<TimeUsageDTO> getUsages(Map<String, Object> filters) {
        String query = prepareQuery(filters, plannedEventQuery, maintenanceEventQuery);
        return jdbcTemplate.query(query, filters, new TimeUsageRowMapper());
    }

    private String prepareQuery(Map<String, Object> filters, String plannedEventQueryPart, String maintenanceEventQueryPart) {
        StringBuilder builder = new StringBuilder("SELECT * FROM ( " + plannedEventQueryPart + "UNION ALL "
                + maintenanceEventQueryPart + " ) AS events");
        if (!filters.isEmpty()) {
            List<String> whereFilters = Lists.newLinkedList();
            if (filters.containsKey(TimeUsageReportFilterFields.FROM_DATE)) {
                whereFilters.add("startDate >= :fromDate");
            }
            if (filters.containsKey(TimeUsageReportFilterFields.TO_DATE)) {
                whereFilters.add("startDate <= :toDate");
            }
            if (filters.containsKey(TimeUsageReportFilterFields.WORKERS)) {
                whereFilters.add("worker_id in (:workers)");
            }
            builder.append(" WHERE ").append(StringUtils.collectionToDelimitedString(whereFilters, " AND "));
        }
        return builder.toString();
    }
}
