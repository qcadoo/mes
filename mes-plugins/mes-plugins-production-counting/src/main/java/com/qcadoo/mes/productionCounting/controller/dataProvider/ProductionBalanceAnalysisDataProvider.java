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
public class ProductionBalanceAnalysisDataProvider implements AnalysisDataProvider {

    private static final String NUMERIC_DATA_TYPE = "02numeric";

    private static final String PRODUCTION_BALANCE_NUMBER = "productionBalanceNumber";

    private static final String ORDER_NUMBER = "orderNumber";

    private static final String PRODUCT_NUMBER = "productNumber";

    private static final String PLANNED_QUANTITY = "plannedQuantity";

    private static final String PRODUCED_QUANTITY = "producedQuantity";

    private static final String DEVIATION = "deviation";

    private static final String PRODUCT_UNIT = "productUnit";
    private static final String PLANNED_COST = "plannedCost";

    private static final String REAL_COST = "realCost";

    private static final String VALUE_DEVIATION = "valueDeviation";

    private static final String PLANNED_COSTS_SUM = "plannedCostsSum";

    private static final String REAL_COSTS_SUM = "realCostsSum";

    private static final String SUM_COSTS_DEVIATION = "sumCostsDeviation";

    private static final String TECHNICAL_PRODUCTION_COSTS = "technicalProductionCosts";
    private static final String TOTAL_COSTS = "totalCosts";
    private static final String REGISTRATION_PRICE = "registrationPrice";
    private static final String REAL_PRODUCTION_COSTS = "realProductionCosts";
    private static final String TOTAL_MANUFACTURING_COST = "totalManufacturingCost";
    private static final String SELL_PRICE = "sellPrice";

    private static final String L_TIME_PART = " 23:59:59";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;


    public List<ColumnDTO> getColumns(final Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(PRODUCTION_BALANCE_NUMBER, translationService
                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.productionBalanceNumber", locale)));
        columns.add(new ColumnDTO(ORDER_NUMBER, translationService
                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.orderNumber", locale)));
        columns.add(new ColumnDTO(PRODUCT_NUMBER, translationService
                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.productNumber", locale)));
        columns.add(
                new ColumnDTO(
                        PLANNED_QUANTITY, translationService
                        .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.plannedQuantity", locale),
                        NUMERIC_DATA_TYPE));
        columns.add(
                new ColumnDTO(PRODUCED_QUANTITY,
                        translationService
                                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.producedQuantity", locale),
                        NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(DEVIATION,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.deviation", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(PRODUCT_UNIT, translationService
                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.productUnit", locale)));
        columns.add(new ColumnDTO(PLANNED_COST,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.plannedCost", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(REAL_COST, translationService
                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.realCost", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(
                VALUE_DEVIATION, translationService
                .translate("productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.valueDeviation", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(PLANNED_COSTS_SUM,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.plannedCostsSum", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(REAL_COSTS_SUM,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.realCostsSum", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(SUM_COSTS_DEVIATION,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.sumCostsDeviation", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(TECHNICAL_PRODUCTION_COSTS,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.technicalProductionCosts", locale),
                NUMERIC_DATA_TYPE));

        columns.add(new ColumnDTO(TOTAL_COSTS,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.totalCosts", locale),
                NUMERIC_DATA_TYPE));

        columns.add(new ColumnDTO(REGISTRATION_PRICE,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.registrationPrice", locale),
                NUMERIC_DATA_TYPE));


        columns.add(new ColumnDTO(REAL_PRODUCTION_COSTS,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.realProductionCosts", locale),
                NUMERIC_DATA_TYPE));

        columns.add(new ColumnDTO(TOTAL_MANUFACTURING_COST,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.totalManufacturingCost", locale),
                NUMERIC_DATA_TYPE));

        columns.add(new ColumnDTO(SELL_PRICE,
                translationService.translate(
                        "productionCounting.productionBalanceAnalysis.window.mainTab.grid.column.sellPrice", locale),
                NUMERIC_DATA_TYPE));
        return columns;
    }

    public String validate(final String dateFrom, final String dateTo) throws ParseException {
        if (dateFrom.isEmpty() || dateTo.isEmpty()) {
            return "productionCounting.validate.global.error.productionBalanceAnalysis.datesCannotBeEmpty";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (formatter.parse(dateTo).compareTo(formatter.parse(dateFrom)) < 0) {
            return "productionCounting.validate.global.error.productionBalanceAnalysis.dateFromCantBeGreaterThanDateTo";
        }

        return "";
    }

    public List<Map<String, Object>> getRecords(final String dateFrom, final String dateTo, final JSONObject filters,
                                                final String sortColumn, final boolean sortAsc) throws JSONException {
        StringBuilder query = new StringBuilder();

        appendBaseQuery(query);

        query.append("WHERE pb.date BETWEEN '").append(dateFrom).append("' AND '").append(dateTo).append(L_TIME_PART)
                .append("' ");

        appendFilters(filters, query);

        appendSort(sortColumn, sortAsc, query);

        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
    }

    private void appendBaseQuery(final StringBuilder query) {
        query.append("SELECT ");
        query.append("ob.id, ");
        query.append("pb.number AS \"productionBalanceNumber\", ");
        query.append("ob.orderNumber AS \"orderNumber\", ");
        query.append("ob.productNumber AS \"productNumber\", ");
        query.append("ob.plannedQuantity AS \"plannedQuantity\", ");
        query.append("ob.producedQuantity AS \"producedQuantity\", ");
        query.append("ob.deviation AS deviation, ");
        query.append("ob.productUnit AS \"productUnit\", ");
        query.append("ob.plannedMaterialCosts AS \"plannedCost\", ");
        query.append("ob.materialCosts AS \"realCost\", ");
        query.append("ob.materialCostsDeviation AS \"valueDeviation\", ");
        query.append("ob.plannedProductionCosts AS \"plannedCostsSum\", ");
        query.append("ob.productionCosts AS \"realCostsSum\", ");
        query.append("ob.productionCostsDeviation AS \"sumCostsDeviation\", ");
        query.append("ob.technicalProductionCosts AS \"technicalProductionCosts\", ");
        query.append("ob.totalCosts AS \"totalCosts\", ");
        query.append("ob.registrationPrice AS \"registrationPrice\", ");
        query.append("ob.realProductionCosts AS \"realProductionCosts\", ");
        query.append("ob.totalManufacturingCost AS \"totalManufacturingCost\", ");
        query.append("ob.sellPrice AS \"sellPrice\" ");

        query.append("FROM productioncounting_orderbalance ob ");
        query.append("JOIN productioncounting_productionbalance pb ON pb.id = ob.productionbalance_id ");
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
                    case PRODUCTION_BALANCE_NUMBER:
                        query.append("AND UPPER(pb.number) LIKE '%").append(value).append("%' ");
                        break;

                    case ORDER_NUMBER:
                        query.append("AND UPPER(ob.orderNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case PRODUCT_NUMBER:
                        query.append("AND UPPER(ob.productNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case PLANNED_QUANTITY:
                        query.append("AND CAST(ob.plannedQuantity AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case PRODUCED_QUANTITY:
                        query.append("AND CAST(ob.producedQuantity AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case DEVIATION:
                        query.append("AND CAST(ob.deviation AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case PRODUCT_UNIT:
                        query.append("AND UPPER(ob.productUnit) LIKE '%").append(value).append("%' ");
                        break;

                    case PLANNED_COST:
                        query.append("AND CAST(ob.plannedMaterialCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case REAL_COST:
                        query.append("AND CAST(ob.materialCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case VALUE_DEVIATION:
                        query.append("AND CAST(ob.materialCostsDeviation AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case PLANNED_COSTS_SUM:
                        query.append("AND CAST(ob.plannedProductionCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case REAL_COSTS_SUM:
                        query.append("AND CAST(ob.productionCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case SUM_COSTS_DEVIATION:
                        query.append("AND CAST(ob.productionCostsDeviation AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case TECHNICAL_PRODUCTION_COSTS:
                        query.append("AND CAST(ob.technicalProductionCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case TOTAL_COSTS:
                        query.append("AND CAST(ob.totalCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case REGISTRATION_PRICE:
                        query.append("AND CAST(ob.registrationPrice AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case REAL_PRODUCTION_COSTS:
                        query.append("AND CAST(ob.realProductionCosts AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case TOTAL_MANUFACTURING_COST:
                        query.append("AND CAST(ob.totalManufacturingCost AS TEXT) LIKE '%").append(value).append("%' ");
                        break;

                    case SELL_PRICE:
                        query.append("AND CAST(ob.sellPrice AS TEXT) LIKE '%").append(value).append("%' ");
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
