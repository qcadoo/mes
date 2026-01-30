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
package com.qcadoo.mes.deliveries.listeners;

import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.QualityCardFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.hooks.OrderedProductDetailsHooks;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderedProductDetailsListeners {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private OrderedProductDetailsHooks orderedProductDetailsHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    public void onProductChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        fillUnitFields(view, state, args);
        fillCurrencyFields(view, state, args);
        setBatchLookupProductFilterValue(view, state, args);
        fillQualityCard(view);
        setBatchLookup(view);
    }

    private void setBatchLookup(ViewDefinitionState view) {
        LookupComponent batchLookup = (LookupComponent) view
                .getComponentByReference(OrderedProductFields.BATCH);
        batchLookup.setFieldValue(null);
        batchLookup.requestComponentUpdateState();
    }


    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillUnitFields(view);

        fillAdditionalUnit(view);
    }

    private void fillAdditionalUnit(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

            FieldComponent conversionField = (FieldComponent) view.getComponentByReference(OrderedProductFields.CONVERSION);

            if (!StringUtils.isEmpty(additionalUnit)) {
                String conversion = numberService
                        .formatWithMinimumFractionDigits(deliveriesService.getConversion(product, unit, additionalUnit, null), 0);

                conversionField.setFieldValue(conversion);
                conversionField.setEnabled(true);
                conversionField.requestComponentUpdateState();
            }

            quantityChange(view, null, null);
        }
    }

    public void fillCurrencyFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillCurrencyFields(view);
    }

    public void calculatePriceFromTotalPrice(final ViewDefinitionState view, final ComponentState state,
                                             final String[] args) {
        deliveriesService.recalculatePriceFromTotalPrice(view, OrderedProductFields.ORDERED_QUANTITY);
    }

    public void calculatePriceFromPricePerUnit(final ViewDefinitionState view, final ComponentState state,
                                               final String[] args) {
        deliveriesService.recalculatePriceFromPricePerUnit(view, OrderedProductFields.ORDERED_QUANTITY);
    }

    public void quantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePrice(view, OrderedProductFields.ORDERED_QUANTITY);

        FormComponent orderedProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity orderedProduct = orderedProductForm.getEntity();

        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);

        if (decimalFieldsInvalid(orderedProductForm) || Objects.isNull(product)) {
            return;
        }

        BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);

        if (Objects.nonNull(orderedQuantity)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);

            BigDecimal conversion = deliveriesService.getConversion(product, unit, additionalUnit, orderedProduct.getDecimalField(OrderedProductFields.CONVERSION));
            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(orderedQuantity,
                    conversion, additionalUnit);

            FieldComponent additionalQuantityField = (FieldComponent) view
                    .getComponentByReference(OrderedProductFields.ADDITIONAL_QUANTITY);

            additionalQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
            additionalQuantityField.requestComponentUpdateState();
        }
    }

    private boolean decimalFieldsInvalid(final FormComponent formComponent) {
        String[] fieldNames = {OrderedProductFields.ADDITIONAL_QUANTITY, OrderedProductFields.CONVERSION,
                OrderedProductFields.ORDERED_QUANTITY};

        boolean valid = false;

        Entity entity = formComponent.getEntity();

        for (String fieldName : fieldNames) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                if (!OrderedProductFields.ORDERED_QUANTITY.equals(fieldName)) {
                    formComponent.findFieldComponentByName(fieldName).addMessage(
                            "qcadooView.validate.field.error.invalidNumericFormat", MessageType.FAILURE);
                }

                valid = true;
            }
        }

        return valid;
    }

    public void additionalQuantityChange(final ViewDefinitionState view, final ComponentState state,
                                         final String[] args) {
        FormComponent orderedProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity orderedProduct = orderedProductForm.getEntity();

        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);

        if (decimalFieldsInvalid(orderedProductForm) || Objects.isNull(product)) {
            return;
        }

        BigDecimal additionalQuantity = orderedProduct.getDecimalField(OrderedProductFields.ADDITIONAL_QUANTITY);

        if (Objects.nonNull(additionalQuantity)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);

            BigDecimal conversion = deliveriesService.getConversion(product, unit, additionalUnit, orderedProduct.getDecimalField(OrderedProductFields.CONVERSION));
            BigDecimal newOrderedQuantity = calculationQuantityService.calculateQuantity(additionalQuantity,
                    conversion, unit);

            FieldComponent orderedQuantityField = (FieldComponent) view
                    .getComponentByReference(OrderedProductFields.ORDERED_QUANTITY);

            orderedQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(newOrderedQuantity, 0));
            orderedQuantityField.requestComponentUpdateState();

            deliveriesService.recalculatePrice(view, OrderedProductFields.ORDERED_QUANTITY);
        }
    }

    public void setBatchLookupProductFilterValue(final ViewDefinitionState view, final ComponentState state,
                                                 final String[] args) {
        FormComponent orderedProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity orderedProduct = orderedProductForm.getPersistedEntityWithIncludedFormValues();

        orderedProductDetailsHooks.setBatchLookupProductFilterValue(view, orderedProduct);
    }

    private void fillQualityCard(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            LookupComponent qualityCardLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.QUALITY_CARD);
            List<Entity> entities = dataDefinitionService
                    .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_QUALITY_CARD).find()
                    .add(SearchRestrictions.eq(QualityCardFields.STATE, "02accepted"))
                    .createAlias(QualityCardFields.PRODUCTS, QualityCardFields.PRODUCTS, JoinType.INNER)
                    .add(SearchRestrictions.eq(QualityCardFields.PRODUCTS + ".id", product.getId())).list().getEntities();
            if (entities.size() == 1) {
                qualityCardLookup.setFieldValue(entities.get(0).getId());
            } else {
                qualityCardLookup.setFieldValue(null);
            }
        }
    }

}
