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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.hooks.OrderedProductDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderedProductDetailsListeners {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private OrderedProductDetailsHooks orderedProductDetailsHooks;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private NumberService numberService;

    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillUnitFields(view);
        fillAdditionalUnit(view);
        clearAdditionalCode(view);
    }

    private void fillAdditionalUnit(ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        if (product != null) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            FieldComponent conversionField = (FieldComponent) view.getComponentByReference("conversion");

            if (!StringUtils.isEmpty(additionalUnit)) {
                String conversion = numberService
                        .formatWithMinimumFractionDigits(getConversion(product, unit, additionalUnit), 0);
                conversionField.setFieldValue(conversion);
                conversionField.setEnabled(true);
                conversionField.requestComponentUpdateState();
            }
            quantityChange(view, null, null);
        }
    }
    
    private void clearAdditionalCode(ViewDefinitionState view) {
        LookupComponent additionalCodeLookup = (LookupComponent) view
                .getComponentByReference(OrderedProductFields.ADDITIONAL_CODE);

        additionalCodeLookup.setFieldValue(null);
        additionalCodeLookup.requestComponentUpdateState();
    }

    private BigDecimal getConversion(Entity product, String unit, String additionalUnit) {
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));
        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public void fillCurrencyFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillCurrencyFields(view);
    }

    public void calculatePriceFromTotalPrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromTotalPrice(view, OrderedProductFields.ORDERED_QUANTITY);
    }

    public void calculatePriceFromPricePerUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromPricePerUnit(view, OrderedProductFields.ORDERED_QUANTITY);
    }

    public void quantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePrice(view, OrderedProductFields.ORDERED_QUANTITY);

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity orderedProduct = form.getEntity();
        if (decimalFieldsInvalid(form)) {
            return;
        }
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);
        BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        if (conversion != null && orderedQuantity != null) {
            FieldComponent additionalQuantity = (FieldComponent) view.getComponentByReference("additionalQuantity");
            BigDecimal newAdditionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());
            newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                    RoundingMode.HALF_UP);
            additionalQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
            additionalQuantity.requestComponentUpdateState();
        }

    }

    private boolean decimalFieldsInvalid(FormComponent form) {
        String[] names = { OrderedProductFields.ADDITIONAL_QUANTITY, OrderedProductFields.CONVERSION,
                OrderedProductFields.ORDERED_QUANTITY };
        boolean valid = false;
        Entity entity = form.getEntity();
        for (String fieldName : names) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                if (!OrderedProductFields.ORDERED_QUANTITY.equals(fieldName)) {
                    form.findFieldComponentByName(fieldName).addMessage("qcadooView.validate.field.error.invalidNumericFormat",
                        MessageType.FAILURE);
                }
                valid = true;
            }
        }
        return valid;
    }

    public void additionalQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity orderedProduct = form.getEntity();
        if (decimalFieldsInvalid(form)) {
            return;
        }
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);
        BigDecimal additionalQuantity = orderedProduct.getDecimalField(OrderedProductFields.ADDITIONAL_QUANTITY);
        if (conversion != null && additionalQuantity != null) {
            FieldComponent orderedQuantity = (FieldComponent) view.getComponentByReference("orderedQuantity");
            BigDecimal newOrderedQuantity = additionalQuantity.divide(conversion, numberService.getMathContext());
            newOrderedQuantity = newOrderedQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                    RoundingMode.HALF_UP);
            orderedQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newOrderedQuantity, 0));
            orderedQuantity.requestComponentUpdateState();
            deliveriesService.recalculatePrice(view, OrderedProductFields.ORDERED_QUANTITY);
        }

    }

}
