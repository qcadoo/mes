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
package com.qcadoo.mes.supplyNegotiations.listeners;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.hooks.OfferProductDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class OfferProductDetailsListeners {

    @Autowired
    private OfferProductDetailsHooks offerProductDetailsHooks;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    public void fillPricePerUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent offerProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OfferProductFields.PRODUCT);

        Entity offerProduct = offerProductForm.getEntity();
        Entity product = productLookup.getEntity();

        Entity offer = offerProduct.getBelongsToField(OfferProductFields.OFFER);

        if (Objects.isNull(offer) || Objects.isNull(product)) {
            return;
        }

        Entity supplier = offer.getBelongsToField(OfferFields.SUPPLIER);
        Entity currency = offer.getBelongsToField(OfferFields.CURRENCY);

        if (Objects.isNull(supplier) || Objects.isNull(currency)) {
            return;
        }

        BigDecimal pricePerUnit = supplyNegotiationsService.getLastPricePerUnit(supplier, currency, product);

        supplyNegotiationsService.fillPriceField(view, OfferProductFields.PRICE_PER_UNIT, pricePerUnit);
        supplyNegotiationsService.fillPriceField(view, OfferProductFields.TOTAL_PRICE, null);

        deliveriesService.recalculatePriceFromPricePerUnit(view, OfferProductFields.QUANTITY);
    }

    public void calculatePriceFromTotalPrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromTotalPrice(view, OfferProductFields.QUANTITY);
    }

    public void calculatePriceFromPricePerUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePriceFromPricePerUnit(view, OfferProductFields.QUANTITY);
    }

    public void calculatePrice(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.recalculatePrice(view, OfferProductFields.QUANTITY);
    }

}
