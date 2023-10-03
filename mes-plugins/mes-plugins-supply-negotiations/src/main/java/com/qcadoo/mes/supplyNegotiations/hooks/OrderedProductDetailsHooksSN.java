/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.supplyNegotiations.hooks;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.OrderedProductFieldsSN;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class OrderedProductDetailsHooksSN {

    @Autowired
    private NumberService numberService;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private DeliveriesService deliveriesService;

    public void fillPricePerUnit(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderedProductFields.PRODUCT);
        LookupComponent offerLookup = (LookupComponent) view.getComponentByReference(OrderedProductFieldsSN.OFFER);

        Entity product = productLookup.getEntity();
        Entity offer = offerLookup.getEntity();

        if (Objects.nonNull(product) && Objects.nonNull(offer)) {
            FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(OrderedProductFields.PRICE_PER_UNIT);
            FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(OrderedProductFields.TOTAL_PRICE);
            FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderedProductFields.ORDERED_QUANTITY);

            BigDecimal quantity = deliveriesService.getBigDecimalFromField(quantityField, view.getLocale());
            BigDecimal pricePerUnit = supplyNegotiationsService.getPricePerUnit(offer, product);

            if (Objects.nonNull(quantity) && Objects.nonNull(pricePerUnit)) {
                BigDecimal totalPrice = quantity.multiply(pricePerUnit, numberService.getMathContext());

                pricePerUnitField.setFieldValue(numberService.format(pricePerUnit));
                totalPriceField.setFieldValue(numberService.format(totalPrice));
            } else {
                if (Objects.isNull(pricePerUnit)) {
                    pricePerUnitField.setFieldValue(null);
                } else {
                    pricePerUnitField.setFieldValue(numberService.format(pricePerUnit));
                }

                totalPriceField.setFieldValue(null);
            }

            totalPriceField.requestComponentUpdateState();
            pricePerUnitField.requestComponentUpdateState();
        }
    }

    public void disabledFieldWhenOfferIsSelected(final ViewDefinitionState view) {
        LookupComponent offerLookup = (LookupComponent) view.getComponentByReference(OrderedProductFieldsSN.OFFER);
        Entity offer = offerLookup.getEntity();

        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(OrderedProductFields.PRICE_PER_UNIT);
        FieldComponent totalPriceField = (FieldComponent) view.getComponentByReference(OrderedProductFields.TOTAL_PRICE);

        if (Objects.isNull(offer)) {
            pricePerUnitField.setEnabled(true);
            totalPriceField.setEnabled(true);
        } else {
            pricePerUnitField.setEnabled(false);
            totalPriceField.setEnabled(false);
        }
    }

    public void fillPricePerUnitAndOffer(final ViewDefinitionState view) {
        FormComponent orderedProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OfferProductFields.PRODUCT);

        Entity orderedProduct = orderedProductForm.getEntity();
        Entity product = productLookup.getEntity();

        Entity delivery = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY);

        if (Objects.isNull(delivery) || Objects.isNull(product)) {
            return;
        }

        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);
        Entity currency = delivery.getBelongsToField(DeliveryFields.CURRENCY);

        if (Objects.isNull(supplier) || Objects.isNull(currency)) {
            return;
        }

        Entity offerProduct = supplyNegotiationsService.getLastOfferProduct(supplier, currency, product);

        BigDecimal pricePerUnit = null;
        BigDecimal totalPrice = null;
        Entity offer = null;

        if (Objects.nonNull(offerProduct)) {
            pricePerUnit = offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT);
            offer = offerProduct.getBelongsToField(OfferProductFields.OFFER);
        }

        supplyNegotiationsService.fillPriceField(view, OrderedProductFields.PRICE_PER_UNIT, pricePerUnit);
        supplyNegotiationsService.fillPriceField(view, OrderedProductFields.TOTAL_PRICE, totalPrice);

        deliveriesService.recalculatePriceFromPricePerUnit(view, OrderedProductFields.ORDERED_QUANTITY);

        supplyNegotiationsService.fillOffer(view, offer);
    }

}
