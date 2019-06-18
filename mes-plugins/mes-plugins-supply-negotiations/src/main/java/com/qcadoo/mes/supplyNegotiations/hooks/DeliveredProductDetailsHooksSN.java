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

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DeliveredProductDetailsHooksSN {

    private static final String L_FORM = "form";

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private DeliveriesService deliveriesService;

    public void fillPricePerUnitAndOffer(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OfferProductFields.PRODUCT);

        Entity deliveredProduct = deliveredProductForm.getEntity();
        Entity product = productLookup.getEntity();

        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        if ((delivery == null) || (product == null)) {
            return;
        }

        Entity supplier = delivery.getBelongsToField(OfferFields.SUPPLIER);

        if (supplier == null) {
            return;
        }

        Entity offerProduct = supplyNegotiationsService.getLastOfferProduct(supplier, product);

        BigDecimal pricePerUnit = null;
        BigDecimal totalPrice = null;
        Entity offer = null;

        if (offerProduct != null) {
            pricePerUnit = offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT);
            offer = offerProduct.getBelongsToField(OfferProductFields.OFFER);
        }

        supplyNegotiationsService.fillPriceField(view, DeliveredProductFields.PRICE_PER_UNIT, pricePerUnit);
        supplyNegotiationsService.fillPriceField(view, DeliveredProductFields.TOTAL_PRICE, totalPrice);
        deliveriesService.recalculatePriceFromPricePerUnit(view, DeliveredProductFields.DELIVERED_QUANTITY);

        supplyNegotiationsService.fillOffer(view, offer);
    }

}
