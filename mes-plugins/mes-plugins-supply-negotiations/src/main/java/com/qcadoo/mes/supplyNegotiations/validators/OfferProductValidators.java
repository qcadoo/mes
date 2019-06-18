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
package com.qcadoo.mes.supplyNegotiations.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OfferProductValidators {

    public boolean checkIfPriceIsFill(final DataDefinition offerProductDD, final Entity offerProduct) {
        if (offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT) == null
                && offerProduct.getDecimalField(OfferProductFields.TOTAL_PRICE) == null) {
            offerProduct.addError(offerProductDD.getField(OfferProductFields.PRICE_PER_UNIT),
                    "supplyNegotiations.offerProduct.price.error.mustBeFill");
            offerProduct.addError(offerProductDD.getField(OfferProductFields.TOTAL_PRICE),
                    "supplyNegotiations.offerProduct.price.error.mustBeFill");
            return false;
        }
        return true;
    }

    public boolean checkIfOfferProductAlreadyExists(final DataDefinition offerProductDD, final Entity offerProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = offerProductDD
                .find()
                .add(SearchRestrictions.belongsTo(OfferProductFields.OFFER,
                        offerProduct.getBelongsToField(OfferProductFields.OFFER)))
                .add(SearchRestrictions.belongsTo(OfferProductFields.PRODUCT,
                        offerProduct.getBelongsToField(OfferProductFields.PRODUCT)));

        if (offerProduct.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", offerProduct.getId()));
        }

        Entity negotiationProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();
        if (negotiationProductFromDB == null) {
            return true;
        }
        offerProduct.addError(offerProductDD.getField(OfferProductFields.PRODUCT),
                "supplyNegotiations.offerProduct.error.productAlreadyExists");

        return false;
    }
}
