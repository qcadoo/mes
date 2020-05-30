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
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("productNameOperationProductColumn")
public class ProductNameOperationProductColumn extends AbstractOperationProductColumn {

    @Autowired
    public ProductNameOperationProductColumn(TranslationService translationService) {
        super(translationService);
    }

    @Override
    public String getIdentifier() {
        return "productNameOperationProductColumn";
    }

    @Override
    public String getColumnValue(Entity operationProduct) {
        Entity product = product(operationProduct);
        return buildValue(product);
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

    private Entity product(Entity operationProduct) {
        return operationProduct.getBelongsToField(OperationProductInComponentFields.PRODUCT);
    }

    private String buildValue(Entity product) {
        StringBuilder builder = new StringBuilder();
        builder.append(name(product)).append(" (").append(number(product)).append(")");
        List<Entity> attrValues = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);
        Map<String, List<String>> valuesByAttribute = Maps.newHashMap();
        attrValues.forEach(prodAttrVal -> {
            String number = prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getStringField(
                    AttributeFields.NUMBER);
            if (valuesByAttribute.containsKey(number)) {
                valuesByAttribute.get(number).add(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE));
            } else {
                valuesByAttribute.put(number, Lists.newArrayList(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE)));
            }
        });
        for (Map.Entry<String, List<String>> entry : valuesByAttribute.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey()).append(": ");
            builder.append(String.join(", ", entry.getValue()));
        }
        return builder.toString();
    }

}
