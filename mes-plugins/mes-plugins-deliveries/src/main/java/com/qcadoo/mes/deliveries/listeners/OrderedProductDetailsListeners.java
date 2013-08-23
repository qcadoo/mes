/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.hooks.OrderedProductDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderedProductDetailsListeners {

    private static final String PRICE_PER_UNIT = "pricePerUnit";

    private static final String ORDERED_QUANTITY = "orderedQuantity";

    private static final String TOTAL_PRICE = "totalPrice";

    private static final List<String> FIELDS = Arrays.asList(PRICE_PER_UNIT, ORDERED_QUANTITY, TOTAL_PRICE);

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private OrderedProductDetailsHooks orderedProductDetailsHooks;

    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillUnitFields(view);
    }

    public void fillCurrencyFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderedProductDetailsHooks.fillCurrencyFields(view);
    }

    public void calculatePriceFromTotalPrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        recalculatePriceFromTotalPrice(view, ORDERED_QUANTITY);
    }

    public void recalculatePriceFromTotalPrice(final ViewDefinitionState view, final String quantityFieldReference) {
        if (!isValidDecimalField(view, Arrays.asList(PRICE_PER_UNIT, quantityFieldReference, TOTAL_PRICE))) {
            return;
        }
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(quantityFieldReference);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(TOTAL_PRICE);
        if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) totalPriceField.getFieldValue())) {
            calculatePriceUsingTotalCost(view, quantityField, totalPriceField);
        }
    }

    private void calculatePriceUsingTotalCost(final ViewDefinitionState view, FieldComponent quantityField,
            FieldComponent totalPriceField) {

        Locale locale = view.getLocale();
        BigDecimal quantity = deliveriesService.getBigDecimalFromField(quantityField, locale);
        BigDecimal totalPrice = deliveriesService.getBigDecimalFromField(totalPriceField, locale);
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(PRICE_PER_UNIT);

        BigDecimal pricePerUnit = numberService.setScale(totalPrice.divide(quantity, numberService.getMathContext()));
        pricePerUnitField.setFieldValue(numberService.format(pricePerUnit));
        pricePerUnitField.requestComponentUpdateState();
    }

    public void calculatePriceFromPricePerUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        recalculatePriceFromPricePerUnit(view, ORDERED_QUANTITY);
    }

    public void recalculatePriceFromPricePerUnit(final ViewDefinitionState view, final String quantityFieldReference) {
        if (!isValidDecimalField(view, Arrays.asList(PRICE_PER_UNIT, quantityFieldReference, TOTAL_PRICE))) {
            return;
        }
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(quantityFieldReference);
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(PRICE_PER_UNIT);
        if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) pricePerUnitField.getFieldValue())) {
            calculatePriceUsingPricePerUnit(view, quantityField, pricePerUnitField);
        }
    }

    private void calculatePriceUsingPricePerUnit(final ViewDefinitionState view, FieldComponent quantityField,
            FieldComponent pricePerUnitField) {

        Locale locale = view.getLocale();
        BigDecimal pricePerUnit = deliveriesService.getBigDecimalFromField(pricePerUnitField, locale);
        BigDecimal quantity = deliveriesService.getBigDecimalFromField(quantityField, locale);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(TOTAL_PRICE);
        BigDecimal totalPrice = numberService.setScale(pricePerUnit.multiply(quantity, numberService.getMathContext()));

        totalPriceField.setFieldValue(numberService.format(totalPrice));
        totalPriceField.requestComponentUpdateState();
    }

    public void calculatePrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        recalculatePrice(view, ORDERED_QUANTITY);
    }

    public void recalculatePrice(final ViewDefinitionState view, final String quantityFieldReference) {
        if (!isValidDecimalField(view, Arrays.asList(PRICE_PER_UNIT, quantityFieldReference, TOTAL_PRICE))) {
            return;
        }
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(quantityFieldReference);
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(PRICE_PER_UNIT);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(TOTAL_PRICE);

        if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) pricePerUnitField.getFieldValue())) {
            calculatePriceUsingPricePerUnit(view, quantityField, pricePerUnitField);
        } else if (StringUtils.isNotEmpty((String) quantityField.getFieldValue())
                && StringUtils.isNotEmpty((String) totalPriceField.getFieldValue())) {
            calculatePriceUsingTotalCost(view, quantityField, totalPriceField);
        }

    }

    private boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fileds) {
        boolean isValid = true;
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity entity = formComponent.getEntity();
        for (String field : fileds) {
            try {
                BigDecimal decimalField = entity.getDecimalField(field);
            } catch (IllegalArgumentException e) {
                formComponent.findFieldComponentByName(field).addMessage("qcadooView.validate.field.error.invalidNumericFormat",
                        MessageType.FAILURE);
                isValid = false;
            }
        }

        return isValid;
    }
}
