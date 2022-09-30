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
public class OperationDurationAnalysisDataProvider implements AnalysisDataProvider {

    private static final String NUMERIC_DATA_TYPE = "02numeric";

    private static final String L_OPERATION_NUMBER = "operationNumber";

    private static final String L_PRODUCT_NUMBER = "productNumber";

    private static final String L_PRODUCT_NAME = "productName";

    private static final String L_TJ = "tj";

    private static final String L_TPZ = "tpz";

    private static final String MACHINE_UTILIZATION = "machineUtilization";

    private static final String LABOR_UTILIZATION = "laborUtilization";

    private static final String L_OUT_PRODUCT_NUMBER = "outProductNumber";

    private static final String L_QUANTITY = "quantity";

    private static final String L_PRODUCT_UNIT = "productUnit";

    private static final String WORKERS_WORKING_TIME_SUM = "workersWorkingTimeSum";

    private static final String WORKER_UNIT_TIME = "workerUnitTime";

    private static final String MACHINES_WORKING_TIME_SUM = "machinesWorkingTimeSum";

    private static final String MACHINE_UNIT_TIME = "machineUnitTime";

    private static final String L_TIME_PART = " 23:59:59";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(final Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(L_OPERATION_NUMBER, translationService
                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.operationNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCT_NUMBER, translationService
                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.productNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCT_NAME, translationService
                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.productName", locale)));
        columns.add(
                new ColumnDTO(
                        L_TJ, translationService
                        .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.tj", locale)));
        columns.add(
                new ColumnDTO(L_TPZ,
                        translationService
                                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.tpz", locale)));
        columns.add(new ColumnDTO(LABOR_UTILIZATION,
                translationService.translate(
                        "productionCounting.operationDurationAnalysis.window.mainTab.grid.column.laborUtilization", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(MACHINE_UTILIZATION,
                translationService.translate(
                        "productionCounting.operationDurationAnalysis.window.mainTab.grid.column.machineUtilization", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_OUT_PRODUCT_NUMBER, translationService
                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.outProductNumber", locale)));
        columns.add(new ColumnDTO(
                L_QUANTITY, translationService
                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.quantity", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_PRODUCT_UNIT, translationService
                .translate("productionCounting.operationDurationAnalysis.window.mainTab.grid.column.productUnit", locale)));
        columns.add(new ColumnDTO(WORKERS_WORKING_TIME_SUM,
                translationService.translate(
                        "productionCounting.operationDurationAnalysis.window.mainTab.grid.column.workersWorkingTimeSum", locale)));
        columns.add(new ColumnDTO(WORKER_UNIT_TIME,
                translationService.translate(
                        "productionCounting.operationDurationAnalysis.window.mainTab.grid.column.workerUnitTime", locale)));
        columns.add(new ColumnDTO(MACHINES_WORKING_TIME_SUM,
                translationService.translate(
                        "productionCounting.operationDurationAnalysis.window.mainTab.grid.column.machinesWorkingTimeSum", locale)));
        columns.add(new ColumnDTO(MACHINE_UNIT_TIME,
                translationService.translate(
                        "productionCounting.operationDurationAnalysis.window.mainTab.grid.column.machineUnitTime", locale)));

        return columns;
    }

    public String validate(final String dateFrom, final String dateTo) throws ParseException {
        if (dateFrom.isEmpty() || dateTo.isEmpty()) {
            return "productionCounting.validate.global.error.operationDurationAnalysis.datesCannotBeEmpty";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (formatter.parse(dateTo).compareTo(formatter.parse(dateFrom)) < 0) {
            return "productionCounting.validate.global.error.operationDurationAnalysis.dateFromCantBeGreaterThanDateTo";
        }

        return "";
    }

    public List<Map<String, Object>> getRecords(final String dateFrom, final String dateTo, final JSONObject filters,
                                                final String sortColumn, final boolean sortAsc) throws JSONException {
        StringBuilder query = new StringBuilder();

        appendBaseQuery(query);

        query.append("WHERE o.dateFrom BETWEEN '").append(dateFrom).append("' AND '").append(dateTo).append(L_TIME_PART)
                .append("' ");
        query.append("AND o.typeofproductionrecording = '03forEach' AND o.state IN ('04completed','03inProgress','06interrupted') ");

        appendFilters(filters, query);

        query.append("GROUP BY op.number, p.number, p.name, toc.tj, toc.tpz, toc.machineUtilization, ");
        query.append("toc.laborUtilization, toc.productionInOneCycle, opocp.number, opocp.unit ");

        appendGroupFilters(filters, query);

        appendSort(sortColumn, sortAsc, query);

        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
    }

    private void appendBaseQuery(final StringBuilder query) {
        query.append("SELECT ");
        query.append("ROW_NUMBER() OVER(ORDER BY op.number) AS \"id\", ");
        query.append("op.number AS \"operationNumber\", ");
        query.append("p.number AS \"productNumber\", ");
        query.append("p.name AS \"productName\", ");
        query.append("TO_CHAR((toc.tj || ' second')::interval, 'HH24:MI:SS') AS \"tj\", ");
        query.append("TO_CHAR((toc.tpz || ' second')::interval, 'HH24:MI:SS') AS \"tpz\", ");
        query.append("toc.machineUtilization AS \"machineUtilization\", ");
        query.append("toc.laborUtilization AS \"laborUtilization\", ");
        query.append("opocp.number AS \"outProductNumber\", ");
        query.append("SUM(topoc.usedquantity) AS \"quantity\", ");
        query.append("opocp.unit AS \"productUnit\", ");
        query.append("TO_CHAR((SUM(pt.laborTime)  || ' second')::interval, 'HH24:MI:SS') AS \"workersWorkingTimeSum\", ");
        query.append("SUM(pt.laborTime) AS \"workersWorkingTimeSumInSeconds\", ");
        query.append("TO_CHAR((SUM(pt.laborTime)/(SUM(topoc.usedquantity) * toc.productionInOneCycle) || ' second')::interval, 'HH24:MI:SS') AS \"workerUnitTime\", ");
        query.append("TO_CHAR((SUM(pt.machineTime)  || ' second')::interval, 'HH24:MI:SS') AS \"machinesWorkingTimeSum\", ");
        query.append("SUM(pt.machineTime) AS \"machinesWorkingTimeSumInSeconds\", ");
        query.append("TO_CHAR((SUM(pt.machineTime)/(SUM(topoc.usedquantity) * toc.productionInOneCycle) || ' second')::interval, 'HH24:MI:SS') AS \"machineUnitTime\" ");
        query.append("FROM orders_order o ");
        query.append("JOIN technologies_technology t ON o.technology_id = t.id ");
        query.append("JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id ");
        query.append("JOIN technologies_operation op ON toc.operation_id = op.id ");
        query.append("JOIN technologies_operationproductoutcomponent opoc ON opoc.operationcomponent_id = toc.id AND waste = FALSE ");
        query.append("JOIN basic_product p ON o.product_id = p.id ");
        query.append("JOIN basic_product opocp ON opoc.product_id = opocp.id ");
        query.append("LEFT JOIN productioncounting_productiontracking pt ON pt.order_id = o.id ");
        query.append("AND pt.technologyoperationcomponent_id = toc.id AND pt.state = '02accepted' ");
        query.append("LEFT JOIN productioncounting_trackingoperationproductoutcomponent topoc ON topoc.productiontracking_id = pt.id ");
        query.append("AND topoc.typeOfMaterial IN ('02intermediate','03finalProduct') ");
    }

    private void appendFilters(final JSONObject filters, final StringBuilder query) throws JSONException {
        if (filters.length() > 0) {
            for (int i = 0; i < filters.names().length(); i++) {
                String key = filters.names().getString(i);
                String value = filters.getString(key).toUpperCase();

                if (value.isEmpty()) {
                    continue;
                }

                switch (key) {
                    case L_OPERATION_NUMBER:
                        query.append("AND UPPER(op.number) LIKE '%").append(value).append("%' ");
                        break;

                    case L_PRODUCT_NUMBER:
                        query.append("AND UPPER(p.number) LIKE '%").append(value).append("%' ");
                        break;

                    case L_PRODUCT_NAME:
                        query.append("AND UPPER(p.name) LIKE '%").append(value).append("%' ");
                        break;

                    case L_TJ:
                        query.append("AND TO_CHAR((toc.tj || ' second')::interval, 'HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case L_TPZ:
                        query.append("AND TO_CHAR((toc.tpz || ' second')::interval, 'HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case LABOR_UTILIZATION:
                        query.append("AND CAST(toc.machineUtilization AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case MACHINE_UTILIZATION:
                        query.append("AND CAST(toc.laborUtilization AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case L_OUT_PRODUCT_NUMBER:
                        query.append("AND UPPER(opocp.number) LIKE '%").append(value).append("%' ");
                        break;

                    case L_PRODUCT_UNIT:
                        query.append("AND UPPER(opocp.unit) LIKE '%").append(value).append("%' ");
                        break;
                }
            }
        }
    }

    private void appendGroupFilters(final JSONObject filters, final StringBuilder query) throws JSONException {
        if (filters.length() > 0) {
            boolean addHaving = true;
            for (int i = 0; i < filters.names().length(); i++) {
                String key = filters.names().getString(i);
                String value = filters.getString(key).toUpperCase();

                if (value.isEmpty()) {
                    continue;
                }

                if (addHaving) {
                    query.append("HAVING 1=1 ");
                    addHaving = false;
                }

                switch (key) {
                    case L_QUANTITY:
                        query.append("AND CAST(SUM(topoc.usedquantity) AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case WORKERS_WORKING_TIME_SUM:
                        query.append("AND TO_CHAR((SUM(pt.laborTime) || ' second')::interval, 'HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case WORKER_UNIT_TIME:
                        query.append("AND TO_CHAR((SUM(pt.laborTime)/(SUM(topoc.usedquantity) * toc.productionInOneCycle) || ' second')::interval, 'HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case MACHINES_WORKING_TIME_SUM:
                        query.append("AND TO_CHAR((SUM(pt.machineTime) || ' second')::interval, 'HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case MACHINE_UNIT_TIME:
                        query.append("AND TO_CHAR((SUM(pt.machineTime)/(SUM(topoc.usedquantity) * toc.productionInOneCycle) || ' second')::interval, 'HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;
                }
            }
        }
    }

    private void appendSort(final String sortColumn, final boolean sortAsc, final StringBuilder query) {
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
