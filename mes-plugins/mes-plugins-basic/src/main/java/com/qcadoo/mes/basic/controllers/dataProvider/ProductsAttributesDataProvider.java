package com.qcadoo.mes.basic.controllers.dataProvider;

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

@Service
public class ProductsAttributesDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();
        columns.add(new ColumnDTO("productNumber", translationService.translate("basic.product.number.label", locale),
                "productNumber"));
        columns.add(
                new ColumnDTO("productName", translationService.translate("basic.product.name.label", locale), "productName"));
        String query = "SELECT a.number AS id, a.name, a.number AS field FROM basic_attribute a WHERE a.forproduct = TRUE ORDER BY a.id";
        columns.addAll(jdbcTemplate.query(query, Collections.emptyMap(), new BeanPropertyRowMapper(ColumnDTO.class)));
        return columns;
    }

    public List<Map<String, Object>> getRecords() {
        String query = "SELECT p.id, p.number AS productNumber, p.name AS productName, a.number AS attributeNumber, "
                + "pav.value AS attributeValue FROM basic_product p LEFT JOIN basic_productattributevalue pav ON "
                + "p.id = pav.product_id LEFT JOIN basic_attribute a ON a.id = pav.attribute_id WHERE "
                + "(a.forproduct = TRUE OR a.id IS NULL) ORDER BY p.number, a.id";
        List<Map<String, Object>> attributes = jdbcTemplate.queryForList(query, Collections.emptyMap());
        Map<Long, Map<String, Object>> results = Maps.newHashMap();
        for (Map<String, Object> attribute : attributes) {
            Long productId = (Long) attribute.get("id");
            Map<String, Object> row;
            if (results.containsKey(productId)) {
                row = results.get(productId);
            } else {
                row = Maps.newHashMap();
                row.put("id", productId);
                row.put("productNumber", attribute.get("productNumber"));
                row.put("productName", attribute.get("productName"));
            }
            if (!Objects.isNull(attribute.get("attributeNumber"))) {
                String attributeValue = (String) row.get(attribute.get("attributeNumber"));
                if (Objects.isNull(attributeValue)) {
                    row.put((String) attribute.get("attributeNumber"), attribute.get("attributeValue"));
                } else {
                    row.put((String) attribute.get("attributeNumber"), attributeValue + ", " + attribute.get("attributeValue"));
                }
            }
            results.put(productId, row);
        }
        return new ArrayList<>(results.values());
    }
}
