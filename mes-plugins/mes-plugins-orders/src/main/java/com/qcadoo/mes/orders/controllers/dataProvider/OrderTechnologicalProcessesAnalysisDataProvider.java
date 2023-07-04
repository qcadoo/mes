package com.qcadoo.mes.orders.controllers.dataProvider;

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
public class OrderTechnologicalProcessesAnalysisDataProvider implements AnalysisDataProvider {

    private static final String NUMERIC_DATA_TYPE = "02numeric";

    private static final String L_NUMBER = "number";

    private static final String L_ORDER_PACK_NUMBER = "orderPackNumber";

    private static final String L_ORDER_NUMBER = "orderNumber";

    private static final String L_PRODUCT_NUMBER = "productNumber";

    private static final String L_MODEL_NAME = "modelName";

    private static final String L_SIZE_NUMBER = "sizeNumber";

    private static final String L_QUANTITY = "quantity";

    private static final String L_WASTES_QUANTITY = "wastesQuantity";

    private static final String L_PRODUCT_UNIT = "productUnit";

    private static final String L_OPERATION_NUMBER = "operationNumber";

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    private static final String L_WORKER = "worker";

    private static final String L_DATE = "date";

    private static final String L_TJ = "tj";

    private static final String L_WORK_TIME = "workTime";

    private static final String L_CURRENT_RATE = "currentRate";

    private static final String L_VALUE = "value";

    private static final String L_TIME_PART = " 23:59:59";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(final Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(L_NUMBER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.number", locale)));
        columns.add(new ColumnDTO(L_ORDER_PACK_NUMBER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.orderPackNumber", locale)));
        columns.add(new ColumnDTO(L_ORDER_NUMBER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.orderNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCT_NUMBER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.productNumber", locale)));
        columns.add(new ColumnDTO(L_MODEL_NAME, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.modelName", locale)));
        columns.add(new ColumnDTO(L_SIZE_NUMBER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.sizeNumber", locale)));
        columns.add(new ColumnDTO(
                L_QUANTITY, translationService
                        .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.quantity", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_WASTES_QUANTITY,
                translationService.translate(
                        "orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.wastesQuantity", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_PRODUCT_UNIT, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.productUnit", locale)));
        columns.add(new ColumnDTO(L_OPERATION_NUMBER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.operationNumber", locale)));
        columns.add(new ColumnDTO(L_TECHNOLOGICAL_PROCESS_NAME, translationService.translate(
                "orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.technologicalProcessName", locale)));
        columns.add(new ColumnDTO(L_WORKER, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.worker", locale)));
        columns.add(new ColumnDTO(L_DATE, translationService
                .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.date", locale)));
        columns.add(new ColumnDTO(L_TJ,
                translationService.translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.tj", locale)));
        columns.add(new ColumnDTO(
                L_WORK_TIME, translationService
                        .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.workTime", locale)));
        columns.add(new ColumnDTO(
                L_CURRENT_RATE, translationService
                        .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.currentRate", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(
                L_VALUE, translationService
                        .translate("orders.orderTechnologicalProcessesAnalysis.window.mainTab.grid.column.value", locale),
                NUMERIC_DATA_TYPE));

        return columns;
    }

    public String validate(final String dateFrom, final String dateTo) throws ParseException {
        if (dateFrom.isEmpty() || dateTo.isEmpty()) {
            return "orderTechnologicalProcessesAnalysis.validate.global.error.orderTechnologicalProcessesAnalysis.datesCannotBeEmpty";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (formatter.parse(dateTo).compareTo(formatter.parse(dateFrom)) < 0) {
            return "orderTechnologicalProcessesAnalysis.validate.global.error.orderTechnologicalProcessesAnalysis.dateFromCantBeGreaterThanDateTo";

        }
        return "";
    }

    public List<Map<String, Object>> getRecords(final String dateFrom, final String dateTo, final JSONObject filters,
            final String sortColumn, final boolean sortAsc) throws JSONException {
        StringBuilder query = new StringBuilder();

        appendBaseQuery(query);

        query.append("WHERE ((ordertechnologicalprocessdto.date IS NOT NULL AND ordertechnologicalprocessdto.date BETWEEN '").append(dateFrom).append("' AND '").append(dateTo).append(L_TIME_PART).append("') ");
        query.append("OR (ordertechnologicalprocessdto.date IS NULL AND ordertechnologicalprocessdto.orderstartdate BETWEEN '").append(dateFrom).append("' AND '").append(dateTo).append(L_TIME_PART).append("')) ");

        appendFilters(filters, query);

        appendSort(sortColumn, sortAsc, query);

        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
    }

    private void appendBaseQuery(final StringBuilder query) {
        query.append("SELECT ");
        query.append("    ordertechnologicalprocessdto.id AS \"id\", ");
        query.append("    ordertechnologicalprocessdto.number AS \"number\", ");
        query.append("    ordertechnologicalprocessdto.orderPackNumber AS \"orderPackNumber\", ");
        query.append("    ordertechnologicalprocessdto.orderNumber AS \"orderNumber\", ");
        query.append("    ordertechnologicalprocessdto.productNumber AS \"productNumber\", ");
        query.append("    ordertechnologicalprocessdto.modelName AS \"modelName\", ");
        query.append("    ordertechnologicalprocessdto.sizeNumber AS \"sizeNumber\", ");
        query.append("    ordertechnologicalprocessdto.quantity AS \"quantity\", ");
        query.append("    ordertechnologicalprocessdto.wastesQuantity AS \"wastesQuantity\", ");
        query.append("    ordertechnologicalprocessdto.productUnit AS \"productUnit\", ");
        query.append("    ordertechnologicalprocessdto.operationNumber AS \"operationNumber\", ");
        query.append("    ordertechnologicalprocessdto.technologicalProcessName AS \"technologicalProcessName\", ");
        query.append("    ordertechnologicalprocessdto.worker AS \"worker\", ");
        query.append("    TO_CHAR(ordertechnologicalprocessdto.date, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    TO_CHAR((COALESCE(technologicalprocesscomponent.tj, 0) || ' second')::interval, 'HH24:MI:SS') AS \"tj\", ");
        query.append("    TO_CHAR((COALESCE(technologicalprocesscomponent.tj, 0) * ordertechnologicalprocessdto.quantity  || ' second')::interval, 'HH24:MI:SS') AS \"workTime\", ");
        query.append("    COALESCE(technologicalprocesscomponent.tj, 0) * ordertechnologicalprocessdto.quantity  AS \"workTimeInSeconds\", ");
        query.append("    ROUND(COALESCE(get_currentrate(ordertechnologicalprocessdto.technologicalprocessrateid, ordertechnologicalprocessdto.date::date, ordertechnologicalprocessdto.orderstartdate::date), 0), 2) AS \"currentRate\", ");
        query.append("    ROUND(COALESCE(technologicalprocesscomponent.tj, 0) * ordertechnologicalprocessdto.quantity / 3600 * COALESCE(get_currentrate(ordertechnologicalprocessdto.technologicalprocessrateid, ordertechnologicalprocessdto.date::date, ordertechnologicalprocessdto.orderstartdate::date), 0), 2) AS \"value\" ");
        query.append("FROM orders_ordertechnologicalprocessdto ordertechnologicalprocessdto ");
        query.append("LEFT JOIN technologies_technologicalprocesscomponent technologicalprocesscomponent ");
        query.append("    ON technologicalprocesscomponent.technologicalprocesslist_id = ordertechnologicalprocessdto.technologicalprocesslistId ");
        query.append("    AND technologicalprocesscomponent.technologicalprocess_id = ordertechnologicalprocessdto.technologicalProcessId ");
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
                    case L_NUMBER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.number) LIKE '%").append(value).append("%' ");
                        break;

                    case L_ORDER_PACK_NUMBER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.orderPackNumber) LIKE '%").append(value)
                                .append("%' ");
                        break;

                    case L_ORDER_NUMBER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.orderNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case L_PRODUCT_NUMBER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.productNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case L_MODEL_NAME:
                        query.append("AND UPPER(ordertechnologicalprocessdto.modelName) LIKE '%").append(value).append("%' ");
                        break;

                    case L_SIZE_NUMBER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.sizeNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case L_QUANTITY:
                        query.append("AND ordertechnologicalprocessdto.quantity = ").append(value).append(" ");
                        break;

                    case L_WASTES_QUANTITY:
                        query.append("AND ordertechnologicalprocessdto.wastesQuantity = ").append(value).append(" ");
                        break;

                    case L_PRODUCT_UNIT:
                        query.append("AND UPPER(ordertechnologicalprocessdto.productUnit) LIKE '%").append(value).append("%' ");
                        break;

                    case L_OPERATION_NUMBER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.operationNumber) LIKE '%").append(value)
                                .append("%' ");
                        break;

                    case L_TECHNOLOGICAL_PROCESS_NAME:
                        query.append("AND UPPER(ordertechnologicalprocessdto.technologicalProcessName) LIKE '%").append(value)
                                .append("%' ");
                        break;

                    case L_WORKER:
                        query.append("AND UPPER(ordertechnologicalprocessdto.worker) LIKE '%").append(value).append("%' ");
                        break;

                    case L_DATE:
                        query.append("AND TO_CHAR(ordertechnologicalprocessdto.date, 'YYYY-MM-DD HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case L_TJ:
                        query.append("AND TO_CHAR((COALESCE(technologicalprocesscomponent.tj, 0) || ' second')::interval, 'HH24:MI:SS') LIKE '%").append(value).append("%' ");
                        break;

                    case L_WORK_TIME:
                        query.append("AND TO_CHAR((COALESCE(technologicalprocesscomponent.tj, 0) * ordertechnologicalprocessdto.quantity || ' second')::interval, 'HH24:MI:SS') LIKE '%").append(value).append("%' ");
                        break;

                    case L_CURRENT_RATE:
                        query.append("AND ROUND(COALESCE(get_currentrate(ordertechnologicalprocessdto.technologicalprocessrateid, ordertechnologicalprocessdto.date::date, ordertechnologicalprocessdto.orderstartdate::date), 0), 2) = ").append(value).append(" ");
                        break;

                    case L_VALUE:
                        query.append("AND ROUND(COALESCE(technologicalprocesscomponent.tj, 0) * ordertechnologicalprocessdto.quantity / 3600 * COALESCE(get_currentrate(ordertechnologicalprocessdto.technologicalprocessrateid, ordertechnologicalprocessdto.date::date, ordertechnologicalprocessdto.orderstartdate::date), 0), 2) = ").append(value).append(" ");
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
