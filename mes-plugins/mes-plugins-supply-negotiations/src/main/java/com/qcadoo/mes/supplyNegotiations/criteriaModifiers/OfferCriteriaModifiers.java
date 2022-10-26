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
package com.qcadoo.mes.supplyNegotiations.criteriaModifiers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OfferCriteriaModifiers {

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    public void showActiveOffersItems(final SearchCriteriaBuilder scb) {
        final SearchQueryBuilder sqb = supplyNegotiationsService
                .getOfferProductDD()
                .find("select offerProduct from #supplyNegotiations_offerProduct as offerProduct INNER JOIN offerProduct.offer as offer WHERE offer.active = 'true'");

        List<Entity> offerProductsList = sqb.list().getEntities();
        List<Long> offerProductsListLong = new ArrayList<>();

        for (Entity entity : offerProductsList) {
            offerProductsListLong.add(entity.getId());
        }

        if (offerProductsListLong.isEmpty()) {
            final BigDecimal bg = new BigDecimal(-1);
            scb.add(SearchRestrictions.eq("quantity", bg));
        } else {
            scb.add(SearchRestrictions.in("id", offerProductsListLong));
        }
    }

    public void showAcceptedOffers(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(OfferFields.STATE, OfferStateStringValues.ACCEPTED));
        scb.add(SearchRestrictions.eq("active", true));
    }

}
