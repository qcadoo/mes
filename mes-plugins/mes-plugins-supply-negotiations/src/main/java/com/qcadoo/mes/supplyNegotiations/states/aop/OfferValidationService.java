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
package com.qcadoo.mes.supplyNegotiations.states.aop;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferFields.OFFER_PRODUCTS;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;

@Service
public class OfferValidationService {

    private static final String ENTITY_IS_NULL = "entity is null";

    public void validationOnAccepted(final StateChangeContext stateChangeContext) {
        checkifOfferHasOfferedProduct(stateChangeContext);
    }

    private void checkifOfferHasOfferedProduct(final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);
        final Entity stateChangeEntity = stateChangeContext.getOwner();
        List<Entity> offerProducts = stateChangeEntity.getHasManyField(OFFER_PRODUCTS);
        if (offerProducts.isEmpty()) {
            stateChangeContext.addValidationError("supplyNegotiations.offer.offerProductsList.isEmpty");
        }
    }
}
