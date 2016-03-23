package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostReportFilterFields;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost.dto.WorkerCostsDTO;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;

@Service
public class WorkerCostsXlsDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final static String plannedEventQuery = "SELECT cost.id as id, cost.number as sourcecost, worker.surname || ' ' || worker.name as worker,\n"
            + "event.number, event.type, realization.duration as worktime\n"
            + "FROM (SELECT plannedEvent_id, worker_id, sum(duration) as duration FROM cmmsmachineparts_plannedeventrealization\n"
            + "WHERE (startDate >= :fromDate AND startDate < :toDate)\n"
            + "OR (finishDate >= :fromDate AND finishDate < :toDate)\n"
            + "GROUP BY plannedevent_id, worker_id) AS realization\n"
            + "JOIN cmmsmachineparts_plannedevent event ON event.id = realization.plannedevent_id\n"
            + "JOIN cmmsmachineparts_sourcecost cost on event.sourcecost_id = cost.id\n"
            + "JOIN basic_staff worker on realization.worker_id = worker.id WHERE event.state IN(" + allowedPlannedStates() + ")";

    private final static String maintenanceEventQuery = "SELECT cost.id as id, cost.number as sourcecost, worker.surname || ' ' || worker.name as worker,\n"
            + "event.number, event.type, worktime.labortime as worktime\n"
            + "FROM (SELECT maintenanceevent_id, worker_id, sum(labortime) as labortime FROM cmmsmachineparts_staffworktime\n"
            + "WHERE (effectiveExecutionTimeStart >= :fromDate AND effectiveExecutionTimeStart < :toDate)\n"
            + "OR (effectiveExecutionTimeEnd >= :fromDate AND effectiveExecutionTimeEnd < :toDate)\n"
            + "GROUP BY maintenanceevent_id, worker_id) AS worktime\n"
            + "JOIN cmmsmachineparts_maintenanceevent event on event.id = worktime.maintenanceevent_id\n"
            + "JOIN cmmsmachineparts_sourcecost cost on event.sourcecost_id = cost.id\n"
            + "JOIN basic_staff worker on worktime.worker_id = worker.id WHERE event.state IN("
            + allowedMaintenanceStates()
            + ")";

    public List<WorkerCostsDTO> getCosts(Map<String, Object> filters) {
        String query = prepareQuery(filters, plannedEventQuery, maintenanceEventQuery);
        return jdbcTemplate.query(query, filters, new WorkerCostsRowMapper());
    }


    private String prepareQuery(Map<String, Object> filters, String plannedEventQueryPart, String maintenanceEventQueryPart) {
        DateTime toDate = new DateTime((Date) filters.get("toDate"));
        filters.put("toDate", toDate.plusDays(1).plusHours(6).toDate());

        DateTime fromDate = new DateTime((Date) filters.get("fromDate"));
        filters.put("fromDate", fromDate.plusHours(6).toDate());

        StringBuilder builder = new StringBuilder("SELECT * FROM ( " + plannedEventQueryPart + " UNION ALL "
                + maintenanceEventQueryPart + " ) AS events");
        if (filters.get(SourceCostReportFilterFields.SOURCE_COST) != null) {
            builder.append(" WHERE id = :sourceCost");
        }
        builder.append(" ORDER BY sourcecost, worker, number");
        return builder.toString();
    }

    private static String allowedMaintenanceStates() {
        List<String> allowed = Lists.newArrayList(MaintenanceEventStateStringValues.CLOSED,
                MaintenanceEventStateStringValues.EDITED, MaintenanceEventStateStringValues.IN_PROGRESS,
                MaintenanceEventStateStringValues.ACCEPTED, MaintenanceEventStateStringValues.PLANNED);
        return StringUtils.collectionToDelimitedString(allowed, ",", "'", "'");
    }

    private static String allowedPlannedStates() {
        List<String> allowed = Lists.newArrayList(PlannedEventStateStringValues.REALIZED,
                PlannedEventStateStringValues.IN_EDITING, PlannedEventStateStringValues.IN_REALIZATION,
                PlannedEventStateStringValues.ACCEPTED, PlannedEventStateStringValues.PLANNED,
                PlannedEventStateStringValues.IN_PLAN);
        return StringUtils.collectionToDelimitedString(allowed, ",", "'", "'");
    }
}
