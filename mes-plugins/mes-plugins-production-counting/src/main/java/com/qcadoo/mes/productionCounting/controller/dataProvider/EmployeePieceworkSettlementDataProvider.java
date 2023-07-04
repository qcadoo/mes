package com.qcadoo.mes.productionCounting.controller.dataProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.basic.services.AnalysisDataProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmployeePieceworkSettlementDataProvider implements AnalysisDataProvider {

    private static final String L_WORKER = "worker";

    private static final String L_DATE = "date";

    private static final String L_SHIFT_NAME = "shiftName";

    private static final String L_ORDER_NUMBER = "orderNumber";

    private static final String L_OPERATION_NUMBER = "operationNumber";

    private static final String L_PRODUCT_NUMBER = "productNumber";

    private static final String L_PRODUCT_NAME = "productName";

    private static final String L_PRODUCED_QUANTITY = "producedQuantity";

    private static final String L_RATE = "rate";

    private static final String L_COST = "cost";

    private static final String NUMERIC_DATA_TYPE = "02numeric";

    private static final String L_TIME_PART = " 23:59:59";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(final Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(L_WORKER, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.worker", locale)));
        columns.add(new ColumnDTO(L_DATE, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.date", locale)));
        columns.add(new ColumnDTO(L_SHIFT_NAME, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.shiftName", locale)));
        columns.add(new ColumnDTO(L_ORDER_NUMBER, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.orderNumber", locale)));
        columns.add(new ColumnDTO(L_OPERATION_NUMBER, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.operationNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCT_NUMBER, translationService.translate(
                "productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.productNumber", locale)));
        columns.add(new ColumnDTO(L_PRODUCT_NAME, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.productName", locale)));
        columns.add(new ColumnDTO(L_PRODUCED_QUANTITY, translationService.translate(
                "productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.producedQuantity", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_RATE, translationService
                .translate("productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.rate", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(L_COST, translationService.translate(
                "productionCounting.employeePieceworkSettlement.window.mainTab.grid.column.cost", locale), NUMERIC_DATA_TYPE));

        return columns;
    }

    public String validate(final String dateFrom, final String dateTo) throws ParseException {
        if (dateFrom.isEmpty() || dateTo.isEmpty()) {
            return "productionCounting.validate.global.error.employeePieceworkSettlement.datesCannotBeEmpty";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (formatter.parse(dateTo).compareTo(formatter.parse(dateFrom)) < 0) {
            return "productionCounting.validate.global.error.employeePieceworkSettlement.dateFromCantBeGreaterThanDateTo";
        }
        return "";
    }

    public List<Map<String, Object>> getRecords(final String dateFrom, final String dateTo,
                                                final JSONObject filters, final String sortColumn,
                                                final boolean sortAsc) throws JSONException {
        StringBuilder query = new StringBuilder();

        appendBaseQuery(query);

        appendDateBetween(query, dateFrom, dateTo);

        appendFilters(query, filters);

        appendSort(query, sortColumn, sortAsc);

        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
    }

    private static void appendBaseQuery(final StringBuilder query) {
        query.append("SELECT row_number() OVER () AS id, ");
        query.append("  worker, ");
        query.append("  date, ");
        query.append("  shiftname AS \"shiftName\", ");
        query.append("  ordernumber AS \"orderNumber\", ");
        query.append("  operationnumber AS \"operationNumber\", ");
        query.append("  productnumber AS \"productNumber\", ");
        query.append("  productname AS \"productName\", ");
        query.append("  producedquantity AS \"producedQuantity\", ");
        query.append("  rate, ");
        query.append("  cost ");
        query.append("FROM productioncounting_employeepieceworksettlementdto ");
    }

    private static void appendDateBetween(final StringBuilder query, final String dateFrom, final String dateTo) {
        query.append("WHERE date BETWEEN '").append(dateFrom).append("' AND '").append(dateTo).append(L_TIME_PART).append("'  ");
    }

    private void appendFilters(final StringBuilder query, final JSONObject filters) throws JSONException {
        if (filters.length() > 0) {
            for (int i = 0; i < filters.names().length(); i++) {
                String key = filters.names().getString(i);
                String value = filters.getString(key).toUpperCase();

                if (value.isEmpty()) {
                    continue;
                }

                switch (key) {
                    case L_WORKER:
                        query.append("AND UPPER(worker) LIKE '%").append(value).append("%' ");

                        break;

                    case L_DATE:
                        query.append("AND to_char(date, 'YYYY-MM-DD') LIKE '%").append(value).append("%' ");

                        break;

                    case L_SHIFT_NAME:
                        query.append("AND UPPER(shiftName) LIKE '%").append(value).append("%' ");

                        break;

                    case L_ORDER_NUMBER:
                        query.append("AND UPPER(orderNumber) LIKE '%").append(value).append("%' ");

                        break;

                    case L_OPERATION_NUMBER:
                        query.append("AND UPPER(operationNumber) LIKE '%").append(value).append("%' ");

                        break;

                    case L_PRODUCT_NUMBER:
                        query.append("AND UPPER(productNumber) LIKE '%").append(value).append("%' ");

                        break;

                    case L_PRODUCT_NAME:
                        query.append("AND UPPER(productName) LIKE '%").append(value).append("%' ");

                        break;

                    case L_PRODUCED_QUANTITY:
                        query.append("AND CAST(producedQuantity AS TEXT) LIKE '%").append(value).append("%' ");

                        break;

                    case L_RATE:
                        query.append("AND CAST(rate AS TEXT) LIKE '%").append(value).append("%' ");

                        break;

                    case L_COST:
                        query.append("AND CAST(cost AS TEXT) LIKE '%").append(value).append("%' ");

                        break;
                }
            }
        }
    }

    private void appendSort(final StringBuilder query, final String sortColumn, final boolean sortAsc) {
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
