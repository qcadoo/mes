package com.qcadoo.mes.productionCounting.controller.dataProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.basic.services.AnalysisDataProvider;

@Service
public class EmployeeWorkingTimeSettlementDataProvider implements AnalysisDataProvider {

    private static final String NUMERIC_DATA_TYPE = "02numeric";

    private static final String L_WORKER = "worker";

    private static final String L_START_DATE = "startDate";

    private static final String L_FINISH_DATE = "finishDate";

    private static final String L_WORK_TIME = "workTime";

    private static final String L_SHIFT_NUMBER = "shiftNumber";

    private static final String L_SHIFT_START_DATE = "shiftStartDate";

    private static final String L_ORDER_NUMBER = "orderNumber";

    private static final String L_OPERATION_NUMBER = "operationNumber";

    private static final String L_PRODUCT_NUMBER = "productNumber";

    private static final String L_DIVISION_NUMBER = "divisionNumber";

    private static final String L_PRODUCTION_LINE_NUMBER = "productionLineNumber";

    private static final String L_WORKSTATION_NUMBER = "workstationNumber";

    private static final String L_TIME_PART = " 23:59:59";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(Locale locale) {

        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(L_WORKER, translationService
                .translate("productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.worker", locale)));
        columns.add(new ColumnDTO(L_START_DATE, translationService
                .translate("productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.startDate", locale)));
        columns.add(new ColumnDTO(L_FINISH_DATE, translationService
                .translate("productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.finishDate", locale)));
        columns.add(new ColumnDTO(L_WORK_TIME,
                translationService.translate(
                        "productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.workTime", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_SHIFT_NUMBER, translationService
                .translate("productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.shiftNumber", locale)));
        columns.add(new ColumnDTO(L_SHIFT_START_DATE, translationService.translate(
                "productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.shiftStartDate", locale)));
        columns.add(new ColumnDTO(L_ORDER_NUMBER, translationService
                .translate("productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.orderNumber", locale)));
        columns.add(new ColumnDTO(L_OPERATION_NUMBER, translationService.translate(
                "productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.operationNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCT_NUMBER, translationService
                .translate("productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.productNumber", locale)));
        columns.add(new ColumnDTO(L_DIVISION_NUMBER, translationService.translate(
                "productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.divisionNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCTION_LINE_NUMBER, translationService.translate(
                "productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.productionLineNumber", locale)));
        columns.add(new ColumnDTO(L_WORKSTATION_NUMBER, translationService.translate(
                "productionCounting.employeeWorkingTimeSettlement.window.mainTab.grid.column.workstationNumber", locale)));

        return columns;
    }

    public String validate(String dateFrom, String dateTo) throws ParseException {
        if (dateFrom.isEmpty() || dateTo.isEmpty()) {
            return "productionCounting.validate.global.error.employeeWorkingTimeSettlement.datesCannotBeEmpty";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (formatter.parse(dateTo).compareTo(formatter.parse(dateFrom)) < 0) {
            return "productionCounting.validate.global.error.employeeWorkingTimeSettlement.dateFromCantBeGreaterThanDateTo";
        }
        return "";
    }

    public List<Map<String, Object>> getRecords(String dateFrom, String dateTo, JSONObject filters, String sortColumn,
                                                boolean sortAsc) throws JSONException {
        StringBuilder query = new StringBuilder();

        StringBuilder queryShiftPart = new StringBuilder();
        StringBuilder queryCreateDatePart = new StringBuilder();
        appendBaseQuery(queryShiftPart);
        appendBaseQuery(queryCreateDatePart);

        queryShiftPart.append("WHERE pt.shiftstartday IS NOT NULL AND pt.shiftstartday BETWEEN '").append(dateFrom)
                .append("' AND '").append(dateTo).append(L_TIME_PART).append("'  ");
        queryCreateDatePart.append("WHERE pt.shiftstartday IS NULL AND pt.createdate BETWEEN '").append(dateFrom)
                .append("' AND '").append(dateTo).append(L_TIME_PART).append("'  ");

        appendFilters(filters, queryShiftPart);
        appendFilters(filters, queryCreateDatePart);

        appendSort(sortColumn, sortAsc, queryShiftPart);
        appendSort(sortColumn, sortAsc, queryCreateDatePart);

        query.append("(").append(queryShiftPart).append(")");
        query.append(" UNION ALL ");
        query.append("(").append(queryCreateDatePart).append(")");

        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
    }

    private void appendBaseQuery(StringBuilder query) {
        query.append("SELECT ");
        query.append("swt.id as id, ");
        query.append("(((stf.surname)::text || ' '::text) || (stf.name)::text) AS worker, ");
        query.append("to_char(swt.effectiveexecutiontimestart, 'YYYY-MM-DD HH24:MI:SS') as \"startDate\", ");
        query.append("to_char(swt.effectiveexecutiontimeend, 'YYYY-MM-DD HH24:MI:SS') as \"finishDate\", ");
        query.append("TO_CHAR((swt.labortime || ' second')::interval, 'HH24:MI:SS') as \"workTime\", ");
        query.append("swt.labortime as \"workTimeInSeconds\", ");
        query.append("s.name as \"shiftNumber\", ");
        query.append("to_char(pt.shiftstartday, 'YYYY-MM-DD') AS \"shiftStartDate\", ");
        query.append("o.number AS \"orderNumber\", ");
        query.append("(((toc.nodenumber)::text || ' '::text) || (op.name)::text) AS \"operationNumber\", ");
        query.append("p.number AS \"productNumber\", ");
        query.append("d.number AS \"divisionNumber\", ");
        query.append("pl.number AS \"productionLineNumber\", ");
        query.append("w.number AS \"workstationNumber\" ");
        query.append("FROM productioncounting_staffworktime swt ");
        query.append(
                "JOIN productioncounting_productiontracking pt ON swt.productionrecord_id = pt.id AND pt.state = '02accepted' ");
        query.append("JOIN orders_order o ON o.id = pt.order_id ");
        query.append("JOIN basic_product p ON o.product_id = p.id ");
        query.append("LEFT JOIN productionlines_productionline pl ON o.productionline_id = pl.id ");
        query.append("LEFT JOIN basic_staff stf ON swt.worker_id = stf.id ");
        query.append("LEFT JOIN technologies_technologyoperationcomponent toc ON pt.technologyoperationcomponent_id = toc.id ");
        query.append("LEFT JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("LEFT JOIN basic_workstation w ON pt.workstation_id = w.id ");
        query.append("LEFT JOIN basic_shift s ON pt.shift_id = s.id ");
        query.append("LEFT JOIN basic_division d ON pt.division_id = d.id ");
    }

    private void appendFilters(JSONObject filters, StringBuilder query) throws JSONException {
        if (filters.length() > 0) {
            for (int i = 0; i < filters.names().length(); i++) {
                String key = filters.names().getString(i);
                String value = filters.getString(key).toUpperCase();
                if (value.isEmpty()) {
                    continue;
                }
                switch (key) {
                    case L_WORKER:
                        query.append("AND UPPER(stf.surname || ' ' || stf.name) LIKE '%").append(value).append("%' ");
                        break;
                    case L_START_DATE:
                        query.append("AND to_char(swt.effectiveexecutiontimestart, 'YYYY-MM-DD HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;
                    case L_FINISH_DATE:
                        query.append("AND to_char(swt.effectiveexecutiontimeend, 'YYYY-MM-DD HH24:MI:SS') LIKE '%").append(value)
                                .append("%' ");
                        break;
                    case L_WORK_TIME:
                        query.append("AND TO_CHAR((swt.labortime || ' second')::interval, 'HH24:MI:SS') LIKE '%").append(value)
                                .append("%' ");
                        break;
                    case L_SHIFT_NUMBER:
                        query.append("AND UPPER(s.name) LIKE '%").append(value).append("%' ");
                        break;
                    case L_SHIFT_START_DATE:
                        query.append("AND to_char(pt.shiftstartday, 'YYYY-MM-DD') LIKE '%").append(value).append("%' ");
                        break;
                    case L_ORDER_NUMBER:
                        query.append("AND UPPER(o.number) LIKE '%").append(value).append("%' ");
                        break;
                    case L_OPERATION_NUMBER:
                        query.append("AND UPPER(toc.nodenumber || ' ' || op.name) LIKE '%").append(value).append("%' ");
                        break;
                    case L_PRODUCT_NUMBER:
                        query.append("AND UPPER(p.number) LIKE '%").append(value).append("%' ");
                        break;
                    case L_DIVISION_NUMBER:
                        query.append("AND UPPER(d.number) LIKE '%").append(value).append("%' ");
                        break;
                    case L_PRODUCTION_LINE_NUMBER:
                        query.append("AND UPPER(pl.number) LIKE '%").append(value).append("%' ");
                        break;
                    case L_WORKSTATION_NUMBER:
                        query.append("AND UPPER(w.number) LIKE '%").append(value).append("%' ");
                        break;
                }
            }
        }
    }

    private void appendSort(String sortColumn, boolean sortAsc, StringBuilder query) {
        if (!sortColumn.isEmpty()) {
            query.append("ORDER BY \"").append(sortColumn);
            if (sortAsc) {
                query.append("\" ASC");
            } else {
                query.append("\" DESC");
            }
        }
    }

}
