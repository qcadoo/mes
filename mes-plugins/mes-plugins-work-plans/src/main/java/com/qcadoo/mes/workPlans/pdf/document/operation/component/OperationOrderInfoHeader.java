/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.pdf.PdfHelper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationOrderInfoHeader {

    private PdfHelper pdfHelper;

    private TranslationService translationService;

    private ParameterService parameterService;

    private DataDefinitionService dataDefinitionService;

    @Autowired
    public OperationOrderInfoHeader(final PdfHelper pdfHelper, final TranslationService translationService,
                                    final ParameterService parameterService, final DataDefinitionService dataDefinitionService) {
        this.pdfHelper = pdfHelper;
        this.translationService = translationService;
        this.parameterService = parameterService;
        this.dataDefinitionService = dataDefinitionService;
    }

    public void print(final Entity order, final PdfPTable operationTable, final Locale locale) throws DocumentException {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        String technologyNumber = null;

        if (Objects.nonNull(technology)) {
            technologyNumber = technology.getStringField(TechnologyFields.NUMBER);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.technology", locale), technologyNumber);

        String orderName = order.getStringField(OrderFields.NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderName", locale), orderName);

        String orderNumber = order.getStringField(OrderFields.NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderNumber", locale), orderNumber);

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        String productionLineNumber = null;
        if (Objects.nonNull(productionLine)) {
            productionLineNumber = productionLine.getStringField(ProductionLineFields.NUMBER);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.productionLineNumber", locale),
                productionLineNumber);

        String orderCategory = order.getStringField(OrderFields.ORDER_CATEGORY);
        if (Objects.nonNull(orderCategory)) {
            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.orderCategory", locale), orderCategory);
        }

        Entity company = order.getBelongsToField(OrderFields.COMPANY);
        String companyNumber = null;
        if (Objects.nonNull(company)) {
            companyNumber = company.getStringField(CompanyFields.NUMBER);
        }
        if (Objects.nonNull(companyNumber)) {
            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.companyNumber", locale), companyNumber);
        }

        String orderDescription = order.getStringField(OrderFields.DESCRIPTION);
        if (Objects.nonNull(orderDescription)) {
            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.orderDescription", locale),
                    orderDescription);
        }

        if (!parameterService.getParameter().getBooleanField(ParameterFieldsWP.HIDE_ORDERED_PRODUCT_WORK_PLAN)) {
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);
            Entity pcq = dataDefinitionService
                    .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                    .find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                            ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                    .setMaxResults(1)
                    .uniqueResult();

            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.orderedProduct", locale),
                    buildOrderedProductValue(product, pcq));
        }

        operationTable.completeRow();
    }

    private String buildOrderedProductValue(Entity product, Entity pcq) {
        StringBuilder builder = new StringBuilder();
        builder.append(product.getStringField(ProductFields.NUMBER));
        appendAttribiutes(builder, product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES));
        appendAttribiutes(builder, pcq.getHasManyField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES));
        return builder.toString();
    }

    private void appendAttribiutes(StringBuilder builder, List<Entity> attrValues) {
        Map<String, List<String>> valuesByAttribute = Maps.newHashMap();
        attrValues.forEach(prodAttrVal -> {
            if (prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).isActive()) {
                String number = prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getStringField(AttributeFields.NUMBER);
                if (valuesByAttribute.containsKey(number)) {
                    valuesByAttribute.get(number).add(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE));
                } else {
                    valuesByAttribute.put(number, Lists.newArrayList(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE)));
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
