/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans.pdf.document.operation.product.column;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("productNameOperationProductColumn")
public class ProductNameOperationProductColumn extends AbstractOperationProductColumn {

    @Autowired
    public ProductNameOperationProductColumn(TranslationService translationService) {
        super(translationService);
    }

    @Autowired
    public DataDefinitionService dataDefinitionService;

    @Override
    public String getIdentifier() {
        return "productNameOperationProductColumn";
    }

    @Override
    public String getColumnValue(Entity operationProduct) {
            return buildValue(operationProduct);
    }

    @Override
    public String getColumnValueForOrder(Entity order, Entity operationProduct) {
        return "";
    }

    @Override
    public ProductDirection[] getDirection() {
        return ProductDirection.values();
    }

    private String number(Entity product) {
        return product.getStringField(ProductFields.NUMBER);
    }

    private String name(Entity product) {
        return product.getStringField(ProductFields.NAME);
    }

    private String buildValue(Entity operationProduct) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                operationProduct.getIntegerField("productId").longValue());

        Entity pcq = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.eq("order.id", operationProduct.getIntegerField("orderId").longValue()))
                .add(SearchRestrictions.eq("technologyOperationComponent.id", operationProduct.getIntegerField("technologyOperationComponentId").longValue()))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .setMaxResults(1)
                .uniqueResult();

        StringBuilder builder = new StringBuilder();
        builder.append(name(product)).append(" (").append(number(product)).append(")");
        appendAttributes(builder, product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES));
        if(Objects.nonNull(pcq)) {
            appendAttributes(builder, pcq.getHasManyField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES));
        }
        return builder.toString();
    }

    private void appendAttributes(StringBuilder builder, List<Entity> attrValues) {
        Map<String, List<String>> valuesByAttribute = Maps.newHashMap();
        attrValues.forEach(prodAttrVal -> {
            if (prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).isActive()) {
                String number = prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getStringField(
                        AttributeFields.NUMBER);
                if (valuesByAttribute.containsKey(number)) {
                    valuesByAttribute.get(number).add(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE));
                } else {
                    valuesByAttribute.put(number,
                            Lists.newArrayList(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE)));
                }
            }
        });
        for (Map.Entry<String, List<String>> entry : valuesByAttribute.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey()).append(": ");
            builder.append(String.join(", ", entry.getValue()));
        }
    }

}
