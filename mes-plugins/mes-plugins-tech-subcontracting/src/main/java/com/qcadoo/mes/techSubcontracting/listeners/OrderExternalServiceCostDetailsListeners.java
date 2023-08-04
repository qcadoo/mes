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
package com.qcadoo.mes.techSubcontracting.listeners;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.techSubcontracting.constants.OrderExternalServiceCostFields;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class OrderExternalServiceCostDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TechnologyService technologyService;

    public final void onTechnologyOperationComponentChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderExternalServiceCostForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view.getComponentByReference(OrderExternalServiceCostFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderExternalServiceCostFields.PRODUCT);
        FieldComponent unitCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.UNIT_COST);
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.QUANTITY);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.QUANTITY_UNIT);

        Entity orderExternalServiceCost = orderExternalServiceCostForm.getEntity();
        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        Long productId = null;
        BigDecimal unitCost = null;
        BigDecimal quantity = null;
        String unit = null;

        if (Objects.nonNull(orderExternalServiceCost) && Objects.nonNull(technologyOperationComponent)) {
            Entity order = orderExternalServiceCost.getBelongsToField(OrderExternalServiceCostFields.ORDER);
            Entity operationProductOutComponent = technologyService.getMainOutputProductComponent(technologyOperationComponent);
            Entity product = null;

            if (Objects.nonNull(operationProductOutComponent)) {
                product = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
            }

            if (Objects.nonNull(order) && Objects.nonNull(product)) {
                Entity productionCountingQuantity = getProductionCountingQuantity(order, technologyOperationComponent, product);

                productId = product.getId();
                unitCost = technologyOperationComponent.getDecimalField(TechnologyOperationComponentFieldsTS.UNIT_COST);
                unit = product.getStringField(ProductFields.UNIT);

                if (Objects.nonNull(productionCountingQuantity)) {
                    quantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
                }
            }
        }

        productLookup.setFieldValue(productId);
        productLookup.requestComponentUpdateState();
        unitCostField.setFieldValue(numberService.formatWithMinimumFractionDigits(unitCost, 0));
        unitCostField.requestComponentUpdateState();
        quantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(quantity, 0));
        quantityField.requestComponentUpdateState();
        unitField.setFieldValue(unit);
        unitField.requestComponentUpdateState();

        calculateTotalCost(view, state, args);
    }

    private Entity getProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent, final Entity product) {
        SearchCriteriaBuilder searchCriteriaBuilder = getProductionCountingQuantityDD().find();

        searchCriteriaBuilder.add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.PRODUCED.getStringValue()));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent));
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product));

        return searchCriteriaBuilder.setMaxResults(1).uniqueResult();
    }

    public final void calculateCosts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.QUANTITY);
        FieldComponent unitCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.UNIT_COST);
        FieldComponent totalCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.TOTAL_COST);

        if (isValidDecimalField(view, Lists.newArrayList(OrderExternalServiceCostFields.QUANTITY, OrderExternalServiceCostFields.UNIT_COST, OrderExternalServiceCostFields.TOTAL_COST))) {
            if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())) {
                if (StringUtils.isNotEmpty((String) unitCostField.getFieldValue())) {
                    calculateTotalCost(view, state, args);
                } else if (StringUtils.isNotEmpty((String) totalCostField.getFieldValue())) {
                    calculateUnitCost(view, state, args);
                }
            }
        }
    }

    public final void calculateTotalCost(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.QUANTITY);
        FieldComponent unitCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.UNIT_COST);
        FieldComponent totalCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.TOTAL_COST);

        if (isValidDecimalField(view, Lists.newArrayList(OrderExternalServiceCostFields.QUANTITY, OrderExternalServiceCostFields.UNIT_COST, OrderExternalServiceCostFields.TOTAL_COST))) {
            if (StringUtils.isNotEmpty((String) quantityField.getFieldValue()) && StringUtils.isNotEmpty((String) unitCostField.getFieldValue())) {
                BigDecimal quantity = getBigDecimalFromField(quantityField, LocaleContextHolder.getLocale());
                BigDecimal unitCost = getBigDecimalFromField(unitCostField, LocaleContextHolder.getLocale());

                BigDecimal totalCost = quantity.multiply(unitCost, numberService.getMathContext());

                totalCostField.setFieldValue(numberService.formatWithMinimumFractionDigits(totalCost, 0));
                totalCostField.requestComponentUpdateState();
            }
        }
    }

    public final void calculateUnitCost(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.QUANTITY);
        FieldComponent unitCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.UNIT_COST);
        FieldComponent totalCostField = (FieldComponent) view.getComponentByReference(OrderExternalServiceCostFields.TOTAL_COST);

        if (isValidDecimalField(view, Lists.newArrayList(OrderExternalServiceCostFields.QUANTITY, OrderExternalServiceCostFields.UNIT_COST, OrderExternalServiceCostFields.TOTAL_COST))) {
            if (StringUtils.isNotEmpty((String) quantityField.getFieldValue()) && StringUtils.isNotEmpty((String) totalCostField.getFieldValue())) {
                BigDecimal quantity = getBigDecimalFromField(quantityField, LocaleContextHolder.getLocale());
                BigDecimal totalCost = getBigDecimalFromField(totalCostField, LocaleContextHolder.getLocale());

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    view.addMessage("techSubcontracting.orderExternalServiceCost.message.quantityIsZero", ComponentState.MessageType.INFO);

                    totalCostField.setFieldValue(numberService.formatWithMinimumFractionDigits(BigDecimal.ZERO, 0));
                    totalCostField.requestComponentUpdateState();
                } else {
                    BigDecimal unitCost = totalCost.divide(quantity, numberService.getMathContext());

                    unitCostField.setFieldValue(numberService.formatWithMinimumFractionDigits(unitCost, 0));
                    unitCostField.requestComponentUpdateState();
                }
            }
        }
    }

    private boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fieldNames) {
        boolean isValid = true;

        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity entity = formComponent.getEntity();

        for (String fieldName : fieldNames) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                formComponent.findFieldComponentByName(fieldName).addMessage("qcadooView.validate.field.error.invalidNumericFormat",
                        ComponentState.MessageType.FAILURE);

                isValid = false;
            }
        }

        return isValid;
    }

    public BigDecimal getBigDecimalFromField(final FieldComponent fieldComponent, final Locale locale) {
        Object value = fieldComponent.getFieldValue();

        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);

            return BigDecimal.valueOf(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            return null;
        }
    }

    private DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

}
