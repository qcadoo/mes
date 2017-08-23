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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.helpers.DeliveryPositionCalculationHelper;
import com.qcadoo.mes.deliveries.hooks.DeliveredProductDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class DeliveredProductDetailsListeners {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveredProductDetailsHooks deliveredProductDetailsHooks;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveryPositionCalculationHelper deliveryPositionCalculationHelper;

    public void onSelectedEntityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.PRODUCT);
        Entity product = productLookup.getEntity();

        LookupComponent additionalCodeLookup = (LookupComponent) view
                .getComponentByReference(DeliveredProductFields.ADDITIONAL_CODE);
        LookupComponent storageLocationLookup = (LookupComponent) view
                .getComponentByReference(DeliveredProductFields.STORAGE_LOCATION);

        if (product != null) {
            filterByProduct(additionalCodeLookup, product.getId());
            filterByProduct(storageLocationLookup, product.getId());
        } else {
            clearAndDisable(additionalCodeLookup);
            clearAndDisable(storageLocationLookup);
        }
        fillConversion(view, state, args);
        quantityChange(view, state, args);
    }

    private void filterByProduct(LookupComponent component, Long id) {
        component.setFieldValue(null);
        component.setEnabled(true);
        FilterValueHolder filterValueHolder = component.getFilterValue();
        filterValueHolder.put(DeliveredProductFields.PRODUCT, id);
        component.setFilterValue(filterValueHolder);
        component.requestComponentUpdateState();
    }

    private void clearAndDisable(LookupComponent component) {
        component.setFieldValue(null);
        component.setEnabled(false);
        component.requestComponentUpdateState();
    }

    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillUnitFields(view);
    }

    public void fillCurrencyFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillCurrencyFields(view);
    }

    public void fillOrderedQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillOrderedQuantities(view);
    }

    public void fillConversion(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveredProductDetailsHooks.fillConversion(view);
    }

    public void calculatePriceFromTotalPrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromTotalPrice(view, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public void calculatePriceFromPricePerUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromPricePerUnit(view, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public void calculatePrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePrice(view, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public void quantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProduct = form.getEntity();
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        if (decimalFieldsInvalid(form) || product == null) {
            return;
        }
        BigDecimal conversion = deliveredProduct.getDecimalField(DeliveredProductFields.CONVERSION);
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
        if (conversion != null && deliveredQuantity != null) {
            String additionalQuantityUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT))
                    .orElse(product.getStringField(ProductFields.UNIT));
            FieldComponent additionalQuantity = (FieldComponent) view.getComponentByReference("additionalQuantity");
            BigDecimal newAdditionalQuantity = deliveryPositionCalculationHelper.calculateAdditionalQuantity(deliveredQuantity,
                    conversion, additionalQuantityUnit);
            additionalQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
            additionalQuantity.requestComponentUpdateState();
        }

    }

    private boolean decimalFieldsInvalid(FormComponent form) {
        String[] names = { DeliveredProductFields.ADDITIONAL_QUANTITY, DeliveredProductFields.CONVERSION,
                DeliveredProductFields.DELIVERED_QUANTITY };
        boolean valid = false;
        Entity entity = form.getEntity();
        for (String fieldName : names) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                form.findFieldComponentByName(fieldName).addMessage("qcadooView.validate.field.error.invalidNumericFormat",
                        MessageType.FAILURE);
                valid = true;
            }
        }
        return valid;
    }

    public void additionalQuantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProduct = form.getEntity();
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        if (decimalFieldsInvalid(form) || product == null) {
            return;
        }
        BigDecimal conversion = deliveredProduct.getDecimalField(DeliveredProductFields.CONVERSION);
        BigDecimal additionalQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY);
        if (conversion != null && additionalQuantity != null) {
            String deliveredQuantityUnit = product.getStringField(ProductFields.UNIT);
            FieldComponent deliveredQuantity = (FieldComponent) view.getComponentByReference("deliveredQuantity");
            BigDecimal newDeliveredQuantity = deliveryPositionCalculationHelper.calculateQuantity(additionalQuantity, conversion,
                    deliveredQuantityUnit);
            deliveredQuantity.setFieldValue(numberService.formatWithMinimumFractionDigits(newDeliveredQuantity, 0));
            deliveredQuantity.requestComponentUpdateState();
        }

    }
}
