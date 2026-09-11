package com.qcadoo.mes.materialFlowResources.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.basic.services.AnalysisDataProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
public class ResourceHistoryDataProvider implements AnalysisDataProvider {

    private static final String NUMERIC_DATA_TYPE = "02numeric";
    public static final String DATE = "date";
    public static final String WAREHOUSE = "warehouse";
    public static final String TYPE = "type";
    public static final String QUANTITY = "quantity";
    public static final String STOCK = "stock";
    public static final String RESOURCE_NUMBER = "resourceNumber";
    public static final String CREATED_RESOURCE = "createdResource";
    public static final String DOCUMENT_NUMBER = "documentNumber";
    public static final String CORRECTION_NUMBER = "correctionNumber";
    public static final String REPACKING_NUMBER = "repackingNumber";
    public static final String COMPANY = "company";


    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(final Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(DATE, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.date", locale)));
        columns.add(new ColumnDTO(WAREHOUSE, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.warehouse", locale)));
        columns.add(new ColumnDTO(TYPE, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.type", locale)));
        columns.add(new ColumnDTO(QUANTITY, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.quantity", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(STOCK, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.stock", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(RESOURCE_NUMBER, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.resourceNumber", locale)));
        columns.add(new ColumnDTO(CREATED_RESOURCE, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.createdResource", locale)));
        columns.add(new ColumnDTO(DOCUMENT_NUMBER,
                translationService.translate(
                        "materialFlowResources.resourceHistory.window.mainTab.grid.column.documentNumber", locale)));
        columns.add(new ColumnDTO(CORRECTION_NUMBER, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.correctionNumber", locale)));
        columns.add(new ColumnDTO(REPACKING_NUMBER, translationService
                .translate("materialFlowResources.resourceHistory.window.mainTab.grid.column.repackingNumber", locale)));
        columns.add(new ColumnDTO(COMPANY, translationService.translate(
                "materialFlowResources.resourceHistory.window.mainTab.grid.column.company", locale)));

        return columns;
    }

    public String validate(final String number) throws ParseException {
        if (number.isEmpty()) {
            return "resourceHistory.validate.global.error.resourceHistory.numberCannotBeEmpty";
        }

        return "";
    }

    public List<Map<String, Object>> getRecords(final String number, final String other, final JSONObject filters,
                                                final String sortColumn, final boolean sortAsc) throws JSONException {
        StringBuilder query = new StringBuilder();

        Set<String> numbers = new HashSet<>();
        numbers.add(number);

        getCreatedResourceForNumber(numbers, number);

        String numbersString = String.join("','", numbers);
        appendBaseQuery(query, numbersString);

        appendFilters(filters, query);

        appendSort(sortColumn, sortAsc, query);

        BigDecimal stock = BigDecimal.ZERO;
        List<Map<String, Object>> records = jdbcTemplate.queryForList(query.toString(), Maps.newHashMap());
        for (Map<String, Object> record : records) {
            stock = stock.add((BigDecimal) record.get(QUANTITY));
            record.put(STOCK, stock);
        }
        return records;
    }

    private void getCreatedResourceForNumber(Set<String> numbers, String number) {
        List<String> createdResources = getCreatedResourceNumbers(number);
        for (String createdResource : createdResources) {
            numbers.add(createdResource);
            getCreatedResourceForNumber(numbers, createdResource);
        }
    }

    private List<String> getCreatedResourceNumbers(String number) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append("    position.transferresourcenumber AS \"createdResource\" ");
        query.append("FROM materialflowresources_position position ");
        query.append("JOIN materialflowresources_document document ");
        query.append("    ON position.document_id = document.id ");
        query.append("    WHERE document.type = '05transfer' ");
        query.append("    AND document.state = '02accepted' ");
        query.append("    AND position.transferresourcenumber IS NOT NULL ");
        query.append("    AND position.resourcenumber = '").append(number).append("' ");
        query.append("UNION SELECT ");
        query.append("    position.createdresourcenumber AS \"createdResource\" ");
        query.append("FROM materialflowresources_repackingposition position ");
        query.append("JOIN materialflowresources_repacking repacking ");
        query.append("    ON position.repacking_id = repacking.id ");
        query.append("    WHERE repacking.state = '02accepted' ");
        query.append("    AND position.resourcenumber ='").append(number).append("' ");
        return jdbcTemplate.queryForList(query.toString(), Maps.newHashMap(), String.class);
    }

    private void appendBaseQuery(final StringBuilder query, String numbers) {
        String inboundDocument = translationService.translate(
                "materialFlowResources.resourceHistory.type.value.inboundDocument",
                LocaleContextHolder.getLocale());
        String outboundDocument = translationService.translate(
                "materialFlowResources.resourceHistory.type.value.outboundDocument",
                LocaleContextHolder.getLocale());
        String correction = translationService.translate(
                "materialFlowResources.resourceHistory.type.value.correction",
                LocaleContextHolder.getLocale());
        String repacking = translationService.translate(
                "materialFlowResources.resourceHistory.type.value.repacking",
                LocaleContextHolder.getLocale());
        query.append("SELECT id, date, warehouse, type, quantity, stock, \"resourceNumber\", \"createdResource\", \"documentNumber\", \"documentId\", \"correctionNumber\", \"correctionId\", \"repackingNumber\", \"repackingId\", company ");
        query.append("FROM (SELECT ");
        query.append("    position.id::text AS \"id\", ");
        query.append("    TO_CHAR(document.time, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    location.number AS \"warehouse\", ");
        query.append("    '").append(outboundDocument).append("' AS \"type\", ");
        query.append("    -position.quantity AS \"quantity\", ");
        query.append("    0 AS \"stock\", ");
        query.append("    position.resourcenumber AS \"resourceNumber\", ");
        query.append("    position.transferresourcenumber AS \"createdResource\", ");
        query.append("    document.number AS \"documentNumber\", ");
        query.append("    document.id AS \"documentId\", ");
        query.append("    null AS \"correctionNumber\", ");
        query.append("    null::bigint AS \"correctionId\", ");
        query.append("    null AS \"repackingNumber\", ");
        query.append("    null::bigint AS \"repackingId\", ");
        query.append("    company.number AS \"company\" ");
        query.append("FROM materialflowresources_position position ");
        query.append("JOIN materialflowresources_document document ");
        query.append("    ON position.document_id = document.id ");
        query.append("JOIN materialflow_location location ");
        query.append("    ON document.locationfrom_id = location.id ");
        query.append("LEFT JOIN basic_company company ");
        query.append("    ON document.company_id = company.id ");
        query.append("    WHERE document.type in ('03internalOutbound','04release','05transfer') ");
        query.append("    AND document.state = '02accepted' ");
        query.append("    AND position.resourcenumber IN ('").append(numbers).append("') ");
        query.append("UNION SELECT ");
        query.append("    position.id::text AS \"id\", ");
        query.append("    TO_CHAR(document.time, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    location.number AS \"warehouse\", ");
        query.append("    '").append(inboundDocument).append("' AS \"type\", ");
        query.append("    position.quantity AS \"quantity\", ");
        query.append("    0 AS \"stock\", ");
        query.append("    position.resourcenumber AS \"resourceNumber\", ");
        query.append("    position.transferresourcenumber AS \"createdResource\", ");
        query.append("    document.number AS \"documentNumber\", ");
        query.append("    document.id AS \"documentId\", ");
        query.append("    null AS \"correctionNumber\", ");
        query.append("    null::bigint AS \"correctionId\", ");
        query.append("    null AS \"repackingNumber\", ");
        query.append("    null::bigint AS \"repackingId\", ");
        query.append("    company.number AS \"company\" ");
        query.append("FROM materialflowresources_position position ");
        query.append("JOIN materialflowresources_document document ");
        query.append("    ON position.document_id = document.id ");
        query.append("JOIN materialflow_location location ");
        query.append("    ON document.locationto_id = location.id ");
        query.append("LEFT JOIN basic_company company ");
        query.append("    ON document.company_id = company.id ");
        query.append("    WHERE document.type in ('01receipt','02internalInbound') ");
        query.append("    AND document.state = '02accepted' ");
        query.append("    AND position.resourcenumber IN ('").append(numbers).append("') ");
        query.append("UNION SELECT ");
        query.append("    'transfer' || position.id AS \"id\", ");
        query.append("    TO_CHAR(document.time, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    location.number AS \"warehouse\", ");
        query.append("    '").append(inboundDocument).append("' AS \"type\", ");
        query.append("    position.quantity AS \"quantity\", ");
        query.append("    0 AS \"stock\", ");
        query.append("    position.transferresourcenumber AS \"resourceNumber\", ");
        query.append("    null AS \"createdResource\", ");
        query.append("    document.number AS \"documentNumber\", ");
        query.append("    document.id AS \"documentId\", ");
        query.append("    null AS \"correctionNumber\", ");
        query.append("    null::bigint AS \"correctionId\", ");
        query.append("    null AS \"repackingNumber\", ");
        query.append("    null::bigint AS \"repackingId\", ");
        query.append("    company.number AS \"company\" ");
        query.append("FROM materialflowresources_position position ");
        query.append("JOIN materialflowresources_document document ");
        query.append("    ON position.document_id = document.id ");
        query.append("JOIN materialflow_location location ");
        query.append("    ON document.locationfrom_id = location.id ");
        query.append("LEFT JOIN basic_company company ");
        query.append("    ON document.company_id = company.id ");
        query.append("    WHERE document.type in ('05transfer') ");
        query.append("    AND document.state = '02accepted' ");
        query.append("    AND position.transferresourcenumber IN ('").append(numbers).append("') ");
        query.append("UNION SELECT ");
        query.append("    'correction' || correction.id AS \"id\", ");
        query.append("    TO_CHAR(correction.createdate, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    location.number AS \"warehouse\", ");
        query.append("    '").append(correction).append("' AS \"type\", ");
        query.append("    correction.newquantity - correction.oldquantity AS \"quantity\", ");
        query.append("    0 AS \"stock\", ");
        query.append("    correction.resourcenumber AS \"resourceNumber\", ");
        query.append("    null AS \"createdResource\", ");
        query.append("    null AS \"documentNumber\", ");
        query.append("    null::bigint AS \"documentId\", ");
        query.append("    correction.number AS \"correctionNumber\", ");
        query.append("    correction.id AS \"correctionId\", ");
        query.append("    null AS \"repackingNumber\", ");
        query.append("    null::bigint AS \"repackingId\", ");
        query.append("    null AS \"company\" ");
        query.append("FROM materialflowresources_resourcecorrection correction ");
        query.append("JOIN materialflow_location location ");
        query.append("    ON correction.location_id = location.id ");
        query.append("    WHERE correction.oldquantity <> correction.newquantity ");
        query.append("    AND correction.resourcenumber IN ('").append(numbers).append("') ");
        query.append("UNION SELECT ");
        query.append("    'repacking' || position.id::text AS \"id\", ");
        query.append("    TO_CHAR(repackingstatechange.dateandtime, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    location.number AS \"warehouse\", ");
        query.append("    '").append(repacking).append("' AS \"type\", ");
        query.append("    -position.quantity AS \"quantity\", ");
        query.append("    0 AS \"stock\", ");
        query.append("    position.resourcenumber AS \"resourceNumber\", ");
        query.append("    position.createdresourcenumber AS \"createdResource\", ");
        query.append("    null AS \"documentNumber\", ");
        query.append("    null::bigint AS \"documentId\", ");
        query.append("    null AS \"correctionNumber\", ");
        query.append("    null::bigint AS \"correctionId\", ");
        query.append("    repacking.number AS \"repackingNumber\", ");
        query.append("    repacking.id AS \"repackingId\", ");
        query.append("    null AS \"company\" ");
        query.append("FROM materialflowresources_repackingposition position ");
        query.append("JOIN materialflowresources_repacking repacking ");
        query.append("    ON position.repacking_id = repacking.id ");
        query.append("JOIN materialflow_location location ");
        query.append("    ON repacking.location_id = location.id ");
        query.append("JOIN materialflowresources_repackingstatechange repackingstatechange ");
        query.append("    ON repackingstatechange.repacking_id = repacking.id AND repackingstatechange.status = '03successful' AND repackingstatechange.targetstate = '02accepted' ");
        query.append("    WHERE repacking.state = '02accepted' ");
        query.append("    AND position.resourcenumber IN ('").append(numbers).append("') ");
        query.append("UNION SELECT ");
        query.append("    'repackingNew' || position.id::text AS \"id\", ");
        query.append("    TO_CHAR(repackingstatechange.dateandtime, 'YYYY-MM-DD HH24:MI:SS') AS \"date\", ");
        query.append("    location.number AS \"warehouse\", ");
        query.append("    '").append(repacking).append("' AS \"type\", ");
        query.append("    position.quantity AS \"quantity\", ");
        query.append("    0 AS \"stock\", ");
        query.append("    position.createdresourcenumber AS \"resourceNumber\", ");
        query.append("    null AS \"createdResource\", ");
        query.append("    null AS \"documentNumber\", ");
        query.append("    null::bigint AS \"documentId\", ");
        query.append("    null AS \"correctionNumber\", ");
        query.append("    null::bigint AS \"correctionId\", ");
        query.append("    repacking.number AS \"repackingNumber\", ");
        query.append("    repacking.id AS \"repackingId\", ");
        query.append("    null AS \"company\" ");
        query.append("FROM materialflowresources_repackingposition position ");
        query.append("JOIN materialflowresources_repacking repacking ");
        query.append("    ON position.repacking_id = repacking.id ");
        query.append("JOIN materialflow_location location ");
        query.append("    ON repacking.location_id = location.id ");
        query.append("JOIN materialflowresources_repackingstatechange repackingstatechange ");
        query.append("    ON repackingstatechange.repacking_id = repacking.id AND repackingstatechange.status = '03successful' AND repackingstatechange.targetstate = '02accepted' ");
        query.append("    WHERE repacking.state = '02accepted' ");
        query.append("    AND position.createdresourcenumber IN ('").append(numbers).append("')) AS resource_history ");
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
                    case DATE:
                        query.append("AND TO_CHAR(date, 'YYYY-MM-DD HH24:MI:SS') LIKE '%")
                                .append(value).append("%' ");
                        break;

                    case WAREHOUSE:
                        query.append("AND UPPER(warehouse) LIKE '%").append(value)
                                .append("%' ");
                        break;

                    case TYPE:
                        query.append("AND UPPER(type) LIKE '%").append(value).append("%' ");
                        break;

                    case QUANTITY:
                        query.append("AND quantity = ").append(value).append(" ");
                        break;

                    case STOCK:
                        query.append("AND stock = ").append(value).append(" ");
                        break;

                    case RESOURCE_NUMBER:
                        query.append("AND UPPER(resourceNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case CREATED_RESOURCE:
                        query.append("AND UPPER(createdResource) LIKE '%").append(value).append("%' ");
                        break;

                    case DOCUMENT_NUMBER:
                        query.append("AND UPPER(documentNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case CORRECTION_NUMBER:
                        query.append("AND UPPER(correctionNumber) LIKE '%").append(value).append("%' ");
                        break;

                    case REPACKING_NUMBER:
                        query.append("AND UPPER(repackingNumber) LIKE '%").append(value)
                                .append("%' ");
                        break;

                    case COMPANY:
                        query.append("AND UPPER(company) LIKE '%").append(value)
                                .append("%' ");
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
        } else {
            query.append("ORDER BY \"").append(DATE).append("\" ASC");
        }
    }

}
