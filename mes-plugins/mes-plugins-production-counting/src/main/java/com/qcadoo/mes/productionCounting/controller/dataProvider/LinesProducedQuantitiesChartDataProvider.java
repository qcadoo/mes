package com.qcadoo.mes.productionCounting.controller.dataProvider;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.BigDecimalUtils;

@Service
public class LinesProducedQuantitiesChartDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public String validate(final String dateFrom, final String dateTo) throws ParseException {
        if (dateFrom.isEmpty() || dateTo.isEmpty()) {
            return "productionCounting.linesProducedQuantitiesChart.validate.global.error.datesCannotBeEmpty";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date dFrom = formatter.parse(dateFrom);
        Date dTo = formatter.parse(dateTo);

        if (dTo.compareTo(dFrom) < 0) {
            return "productionCounting.linesProducedQuantitiesChart.validate.global.error.dateFromCantBeGreaterThanDateTo";
        }
        long diff = TimeUnit.DAYS.convert(Math.abs(dTo.getTime() - dFrom.getTime()), TimeUnit.MILLISECONDS);
        if (diff > 6) {
            return "productionCounting.linesProducedQuantitiesChart.validate.global.error.dateDifferenceToBig";
        }

        return "";
    }

    public Map<String, Object> getData(final String dateFrom, final String dateTo, final Locale locale) throws ParseException {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = getLabels(dateFrom, dateTo);
        data.put("labels", labels);

        Map<String, List<BigDecimal>> datasets = getDatasets(dateFrom, dateTo, labels, locale);

        data.put("datasets", datasets);
        return data;
    }

    private Map<String, List<BigDecimal>> getDatasets(String dateFrom, String dateTo, List<String> labels, Locale locale) {
        Map<String, List<BigDecimal>> datasets = new HashMap<>();
        Map<String, BigDecimal> factoryQuantities = new HashMap<>();
        List<Map<String, Object>> producedQuantities = getProducedQuantities(dateFrom, dateTo);

        Set<String> productionLines = getProductionLines(producedQuantities);
        for (String productionLine : productionLines) {
            List<BigDecimal> quantities = new ArrayList<>();
            for (String label : labels) {
                BigDecimal quantity = getQuantity(productionLine, label, producedQuantities);
                quantities.add(quantity);
                factoryQuantities.merge(label, quantity, BigDecimal::add);
            }
            datasets.put(productionLine, quantities);
        }
        addFactoryQuantity(labels, locale, datasets, factoryQuantities);
        return datasets;
    }

    private void addFactoryQuantity(List<String> labels, Locale locale, Map<String, List<BigDecimal>> datasets, Map<String, BigDecimal> factoryQuantities) {
        List<BigDecimal> factoryQuantitiesList = new ArrayList<>();
        for (String label : labels) {
            factoryQuantitiesList.add(factoryQuantities.get(label));
        }
        datasets.put(translationService.translate("productionCounting.linesProducedQuantitiesChart.chart.factory.label", locale), factoryQuantitiesList);
    }

    private List<Map<String, Object>> getProducedQuantities(String dateFrom, String dateTo) {
        StringBuilder query = getProducedQuantitiesQuery();

        query.append("AND COALESCE(pt.shiftstartday, date(pt.timerangefrom), date(pt.createdate)) BETWEEN '")
                .append(dateFrom).append("' AND '").append(dateTo).append("' ")
                .append("GROUP BY productionline, chartdate ORDER BY productionline, chartdate ");
        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
    }

    private Set<String> getProductionLines(List<Map<String, Object>> producedQuantities) {
        Set<String> productionLines = new HashSet<>();
        for (Map<String, Object> producedQuantity : producedQuantities) {
            productionLines.add((String) producedQuantity.get("productionline"));
        }
        return productionLines;
    }

    private BigDecimal getQuantity(String productionLine, String label, List<Map<String, Object>> producedQuantities) {
        for (Map<String, Object> producedQuantity : producedQuantities) {
            if (productionLine.equals(producedQuantity.get("productionline")) && label.equals(producedQuantity.get("chartdate"))) {
                return BigDecimalUtils.convertNullToZero(producedQuantity.get("quantity"));
            }
        }
        return BigDecimal.ZERO;
    }

    private List<String> getLabels(String dateFrom, String dateTo) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dFrom = formatter.parse(dateFrom);
        Date dTo = formatter.parse(dateTo);
        long diff = TimeUnit.DAYS.convert(Math.abs(dTo.getTime() - dFrom.getTime()), TimeUnit.MILLISECONDS);
        LocalDate localDate = LocalDate.parse(dateFrom);
        List<String> labels = new ArrayList<>();
        for (int i = 0; i <= diff; i++) {
            labels.add(localDate.toString());
            localDate = localDate.plusDays(1);
        }

        return labels;
    }

    private StringBuilder getProducedQuantitiesQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("pl.number AS productionline, ");
        query.append("to_char(COALESCE(pt.shiftstartday, date(pt.timerangefrom), date(pt.createdate)), 'YYYY-MM-DD') AS chartdate, ");
        query.append("SUM(topoc.usedquantity) AS quantity ");
        query.append("FROM productioncounting_productiontracking pt ");
        query.append("JOIN productioncounting_trackingoperationproductoutcomponent topoc ");
        query.append("ON topoc.productiontracking_id = pt.id ");
        query.append("JOIN orders_order o ON o.id = pt.order_id ");
        query.append("JOIN productionlines_productionline pl ON pl.id = o.productionline_id ");
        query.append("WHERE pt.state = '02accepted' ");
        query.append("AND topoc.product_id = o.product_id ");
        return query;
    }

}
