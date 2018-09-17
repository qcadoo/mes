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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferState;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateChangeDescriber;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateStringValues;
import com.qcadoo.mes.supplyNegotiations.util.OfferPricesAndQuantities;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class OfferHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private OfferStateChangeDescriber describer;

    @Autowired
    private NumberService numberService;

    public void onCreate(final DataDefinition offerDD, final Entity offer) {
        setInitialState(offer);
    }

    public void onCopy(final DataDefinition offerDD, final Entity offer) {
        setInitialState(offer);
        clearStateFieldOnCopy(offer);
    }

    public void onView(final DataDefinition offerDD, final Entity offer) {
        fillCumulatedQuantityAndTotalPrice(offer);
    }

    private void setInitialState(final Entity offer) {
        stateChangeEntityBuilder.buildInitial(describer, offer, OfferState.DRAFT);
    }

    private void clearStateFieldOnCopy(final Entity offer) {
        offer.setField(OfferFields.STATE, OfferStateStringValues.DRAFT);
    }

    private void fillCumulatedQuantityAndTotalPrice(final Entity offer) {
        OfferPricesAndQuantities pricesAndQntts = new OfferPricesAndQuantities(offer, numberService);
        offer.setField(OfferFields.OFFER_PRODUCTS_CUMULATED_QUANTITY, pricesAndQntts.getOfferCumulatedQuantity());
        offer.setField(OfferFields.OFFER_PRODUCTS_CUMULATED_TOTAL_PRICE, pricesAndQntts.getOfferTotalPrice());
    }

}
