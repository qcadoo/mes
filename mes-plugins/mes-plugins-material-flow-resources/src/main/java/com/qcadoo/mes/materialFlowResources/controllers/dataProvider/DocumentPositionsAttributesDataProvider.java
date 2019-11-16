package com.qcadoo.mes.materialFlowResources.controllers.dataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.materialFlowResources.constants.PositionDtoFields;

@Service
public class DocumentPositionsAttributesDataProvider {

    public static final String ORDER_NUMBER = "orderNumber";

    public static final String DELIVERY_NUMBER = "deliveryNumber";

    public static final String ATTRIBUTE_NUMBER = "attributeNumber";

    public static final String ATTRIBUTE_VALUE = "attributeValue";

    public static final String NUMERIC_DATA_TYPE = "02numeric";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();
        columns.add(new ColumnDTO(PositionDtoFields.LOCATION_FROM, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.locationFrom", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.LOCATION_TO, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.locationTo", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.PRODUCT_NUMBER, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.productNumber", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.PRODUCT_NAME, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.productName", locale)));
        columns.add(new ColumnDTO(
                PositionDtoFields.QUANTITY, translationService
                        .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.quantity", locale),
                NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(PositionDtoFields.PRODUCT_UNIT, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.unit", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.PRICE,
                translationService.translate("materialFlowResources.positionDto.price.label", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(PositionDtoFields.VALUE,
                translationService.translate("materialFlowResources.positionDto.value.label", locale), NUMERIC_DATA_TYPE));
        columns.add(new ColumnDTO(PositionDtoFields.DOCUMENT_NUMBER, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.documentNumber", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.DOCUMENT_DATE, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.documentName", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.DOCUMENT_TYPE, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.documentType", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.STATE, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.state", locale)));
        columns.add(new ColumnDTO(ORDER_NUMBER,
                translationService.translate("materialFlowResources.positionDto.orderNumber.label", locale)));
        columns.add(new ColumnDTO(DELIVERY_NUMBER, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.delivery", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.RESOURCE_NUMBER,
                translationService.translate("materialFlowResources.positionDto.resourceNumber.label", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.EXPIRATION_DATE, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.expirationDate", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.PRODUCTION_DATE, translationService
                .translate("materialFlowResources.documentPositionsList.window.mainTab.grid.column.productionDate", locale)));
        columns.add(new ColumnDTO(PositionDtoFields.BATCH,
                translationService.translate("materialFlowResources.positionDto.batch.label", locale)));
        String query = "SELECT 'resource' || a.number AS id, a.name, a.unit, a.valuetype AS dataType "
                + "FROM basic_attribute a WHERE a.forresource = TRUE "
                + "UNION ALL SELECT 'product' || a.number AS id, a.name, a.unit, a.valuetype AS dataType "
                + "FROM basic_attribute a WHERE a.forProduct = TRUE ORDER BY id";
        columns.addAll(jdbcTemplate.query(query, Collections.emptyMap(), new BeanPropertyRowMapper(ColumnDTO.class)));
        return columns;
    }

    public List<Map<String, Object>> getRecords() {
        String query = "SELECT pos.id, locationfrom.number AS locationFrom, locationto.number AS locationTo, "
                + "p.number AS productNumber, p.name AS productName, pos.quantity, p.unit AS productUnit, pos.price, "
                + "(pos.price * pos.quantity) AS value, CASE WHEN (pos.externaldocumentnumber IS NULL) THEN doc.number "
                + "ELSE pos.externaldocumentnumber END AS documentNumber, doc.\"time\" AS documentDate, CASE WHEN "
                + "(pos.externaldocumentnumber IS NULL) THEN doc.type ELSE '03internalOutbound'::character varying(255) "
                + "END AS documentType, doc.state, o.number AS orderNumber, d.number AS deliveryNumber, "
                + "pos.resourcenumber AS resourceNumber, (pos.expirationdate)::timestamp without time zone AS expirationDate, "
                + "(pos.productiondate)::timestamp without time zone AS productionDate, pos.batch, "
                + "'product' || a.number AS attributeNumber, pav.value AS attributeValue "
                + "FROM materialflowresources_position pos LEFT JOIN materialflowresources_document doc ON doc.id = pos.document_id "
                + "LEFT JOIN materialflow_location locationfrom ON locationfrom.id = doc.locationfrom_id "
                + "LEFT JOIN materialflow_location locationto ON locationto.id = doc.locationto_id "
                + "JOIN basic_product p ON p.id = pos.product_id "
                + "LEFT JOIN deliveries_delivery d ON d.id = doc.delivery_id "
                + "LEFT JOIN orders_order o ON (o.id = doc.order_id OR o.id = pos.orderid) "
                + "LEFT JOIN basic_productattributevalue pav ON p.id = pav.product_id "
                + "LEFT JOIN basic_attribute a ON a.id = pav.attribute_id "
                + "WHERE (a.forproduct = TRUE OR a.id IS NULL) ORDER BY pos.id, a.id";

        List<Map<String, Object>> attributes = jdbcTemplate.queryForList(query, Collections.emptyMap());
        Map<Long, Map<String, Object>> results = Maps.newHashMap();
        for (Map<String, Object> attribute : attributes) {
            Long documentPositionId = (Long) attribute.get("id");
            Map<String, Object> row;
            if (results.containsKey(documentPositionId)) {
                row = results.get(documentPositionId);
            } else {
                row = Maps.newHashMap();
                row.put("id", documentPositionId);
                row.put(PositionDtoFields.LOCATION_FROM, attribute.get(PositionDtoFields.LOCATION_FROM));
                row.put(PositionDtoFields.LOCATION_TO, attribute.get(PositionDtoFields.LOCATION_TO));
                row.put(PositionDtoFields.PRODUCT_NUMBER, attribute.get(PositionDtoFields.PRODUCT_NUMBER));
                row.put(PositionDtoFields.PRODUCT_NAME, attribute.get(PositionDtoFields.PRODUCT_NAME));
                row.put(PositionDtoFields.QUANTITY, attribute.get(PositionDtoFields.QUANTITY));
                row.put(PositionDtoFields.PRODUCT_UNIT, attribute.get(PositionDtoFields.PRODUCT_UNIT));
                row.put(PositionDtoFields.PRICE, attribute.get(PositionDtoFields.PRICE));
                row.put(PositionDtoFields.VALUE, attribute.get(PositionDtoFields.VALUE));
                row.put(PositionDtoFields.DOCUMENT_NUMBER, attribute.get(PositionDtoFields.DOCUMENT_NUMBER));
                row.put(PositionDtoFields.DOCUMENT_DATE, attribute.get(PositionDtoFields.DOCUMENT_DATE));
                row.put(PositionDtoFields.DOCUMENT_TYPE, attribute.get(PositionDtoFields.DOCUMENT_TYPE));
                row.put(PositionDtoFields.STATE, attribute.get(PositionDtoFields.STATE));
                row.put(ORDER_NUMBER, attribute.get(ORDER_NUMBER));
                row.put(DELIVERY_NUMBER, attribute.get(DELIVERY_NUMBER));
                row.put(PositionDtoFields.RESOURCE_NUMBER, attribute.get(PositionDtoFields.RESOURCE_NUMBER));
                row.put(PositionDtoFields.EXPIRATION_DATE, attribute.get(PositionDtoFields.EXPIRATION_DATE));
                row.put(PositionDtoFields.PRODUCTION_DATE, attribute.get(PositionDtoFields.PRODUCTION_DATE));
                row.put(PositionDtoFields.BATCH, attribute.get(PositionDtoFields.BATCH));
            }
            if (!Objects.isNull(attribute.get(ATTRIBUTE_NUMBER))) {
                String attributeValue = (String) row.get(attribute.get(ATTRIBUTE_NUMBER));
                if (Objects.isNull(attributeValue)) {
                    row.put((String) attribute.get(ATTRIBUTE_NUMBER), attribute.get(ATTRIBUTE_VALUE));
                } else {
                    row.put((String) attribute.get(ATTRIBUTE_NUMBER), attributeValue + ", " + attribute.get(ATTRIBUTE_VALUE));
                }
            }
            results.put(documentPositionId, row);
        }
        return new ArrayList<>(results.values());
    }
}
