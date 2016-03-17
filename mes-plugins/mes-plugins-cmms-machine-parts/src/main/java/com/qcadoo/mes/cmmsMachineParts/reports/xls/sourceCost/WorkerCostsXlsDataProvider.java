package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost.dto.WorkerCostsDTO;

@Service
public class WorkerCostsXlsDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final static String query = "SELECT cost.name as sourcecost, worker.name || ' ' || worker.surname as worker, event.number, event.type, worktime.labortime as worktime\n"
            + "FROM cmmsmachineparts_staffworktime worktime\n"
            + "JOIN cmmsmachineparts_maintenanceevent event on event.id = worktime.maintenanceevent_id\n"
            + "JOIN cmmsmachineparts_sourcecost cost on event.sourcecost_id = cost.id\n"
            + "JOIN basic_staff worker on worktime.worker_id = worker.id\n"
            + "UNION\n"
            + "\n"
            + "SELECT cost.name as sourcecost, worker.name || ' ' || worker.surname as worker, event.number, event.type, realization.duration as worktime\n"
            + "FROM cmmsmachineparts_plannedeventrealization realization\n"
            + "JOIN cmmsmachineparts_plannedevent event ON event.id = realization.plannedevent_id\n"
            + "JOIN cmmsmachineparts_sourcecost cost on event.sourcecost_id = cost.id\n"
            + "JOIN basic_staff worker on realization.worker_id = worker.id";

    public List<WorkerCostsDTO> getCosts(Map<String, Object> filters) {
        // String query = prepareQuery(filters, plannedEventQuery, query);
        return jdbcTemplate.query(query, filters, new WorkerCostsRowMapper());
    }

    private String prepareQuery(Map<String, Object> filters, String plannedEventQueryPart, String maintenanceEventQueryPart) {
        // StringBuilder builder = new StringBuilder("SELECT * FROM ( " + plannedEventQueryPart + "UNION ALL "
        // + maintenanceEventQueryPart + " ) AS events");
        // if (!filters.isEmpty()) {
        // List<String> whereFilters = Lists.newLinkedList();
        // if (filters.containsKey(TimeUsageReportFilterFields.FROM_DATE)) {
        // whereFilters.add("startDate >= :fromDate");
        // }
        // if (filters.containsKey(TimeUsageReportFilterFields.TO_DATE)) {
        // whereFilters.add("startDate <= :toDate");
        // }
        // if (filters.containsKey(TimeUsageReportFilterFields.WORKERS)) {
        // whereFilters.add("worker_id in (:workers)");
        // }
        // builder.append(" WHERE ").append(StringUtils.collectionToDelimitedString(whereFilters, " AND "));
        // }
        // return builder.toString();
        return query;
    }
}
