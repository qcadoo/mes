package com.qcadoo.mes.basic.controllers.dataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;

@Service
public class ProductsAttributesDataProvider {

    private static final String PRODUCT_NUMBER = "productNumber";

    private static final String PRODUCT_NAME = "productName";

    private static final String ATTRIBUTE_NUMBER = "attributeNumber";

    private static final String ATTRIBUTE_VALUE = "attributeValue";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public List<ColumnDTO> getColumns(Locale locale) {
        List<ColumnDTO> columns = Lists.newArrayList();

        columns.add(new ColumnDTO(PRODUCT_NUMBER, translationService.translate("basic.product.number.label", locale)));
        columns.add(new ColumnDTO(PRODUCT_NAME, translationService.translate("basic.product.name.label", locale)));
        columns.add(new ColumnDTO(ProductFields.GLOBAL_TYPE_OF_MATERIAL,
                translationService.translate("basic.product.globalTypeOfMaterial.label", locale)));
        columns.add(new ColumnDTO(ProductFields.UNIT, translationService.translate("basic.product.unit.label", locale)));
        columns.add(new ColumnDTO(ProductFields.ADDITIONAL_UNIT,
                translationService.translate("basic.product.additionalUnit.label", locale)));
        columns.add(new ColumnDTO(ProductFields.CATEGORY, translationService.translate("basic.product.category.label", locale)));
        columns.add(new ColumnDTO(ProductFields.SIZE, translationService.translate("basic.product.size.label", locale)));
        String query = "SELECT a.number AS id, a.name, a.unit, a.valuetype AS dataType "
                + "FROM basic_attribute a WHERE a.active = TRUE AND a.forproduct = TRUE ORDER BY a.number";

        columns.addAll(jdbcTemplate.query(query, Collections.emptyMap(), new BeanPropertyRowMapper(ColumnDTO.class)));

        return columns;
    }

    public List<Map<String, Object>> getRecords() {
        String query = "SELECT p.id, p.number AS productNumber, p.name AS productName, p.globaltypeofmaterial, p.unit, "
                + "p.additionalunit, p.category, s.number AS size, a.number AS attributeNumber, pav.value AS attributeValue "
                + "FROM basic_product p LEFT JOIN basic_size s ON s.id = p.size_id "
                + "LEFT JOIN basic_productattributevalue pav ON p.id = pav.product_id "
                + "LEFT JOIN basic_attribute a ON a.id = pav.attribute_id WHERE p.active = TRUE ORDER BY p.number, a.number";

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
                row.put(PRODUCT_NUMBER, attribute.get(PRODUCT_NUMBER));
                row.put(PRODUCT_NAME, attribute.get(PRODUCT_NAME));
                row.put(ProductFields.GLOBAL_TYPE_OF_MATERIAL, translationService.translate(
                        "basic.product.globalTypeOfMaterial.value." + attribute.get(ProductFields.GLOBAL_TYPE_OF_MATERIAL),
                        LocaleContextHolder.getLocale()));
                row.put(ProductFields.UNIT, attribute.get(ProductFields.UNIT));
                row.put(ProductFields.ADDITIONAL_UNIT, attribute.get(ProductFields.ADDITIONAL_UNIT));
                row.put(ProductFields.CATEGORY, attribute.get(ProductFields.CATEGORY));
                row.put(ProductFields.SIZE, attribute.get(ProductFields.SIZE));
            }

            if (!Objects.isNull(attribute.get(ATTRIBUTE_NUMBER))) {
                String attributeValue = (String) row.get(attribute.get(ATTRIBUTE_NUMBER));

                if (Objects.isNull(attributeValue)) {
                    row.put((String) attribute.get(ATTRIBUTE_NUMBER), attribute.get(ATTRIBUTE_VALUE));
                } else {
                    row.put((String) attribute.get(ATTRIBUTE_NUMBER), attributeValue + ", " + attribute.get(ATTRIBUTE_VALUE));
                }
            }

            results.put(productId, row);
        }

        return new ArrayList<>(results.values());
    }

}
