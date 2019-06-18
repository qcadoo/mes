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

import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.FARTHEST_LIMIT_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.*;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.REQUEST_FOR_QUOTATION;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class NegotiationProductHooks {

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    public boolean checkIfNegotiationProductAlreadyExists(final DataDefinition negotiationProductDD,
            final Entity negotiationProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = negotiationProductDD.find()
                .add(SearchRestrictions.belongsTo(NEGOTIATION, negotiationProduct.getBelongsToField(NEGOTIATION)))
                .add(SearchRestrictions.belongsTo(PRODUCT, negotiationProduct.getBelongsToField(PRODUCT)));

        if (negotiationProduct.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", negotiationProduct.getId()));
        }

        Entity negotiationProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();
        if (negotiationProductFromDB == null) {
            return true;
        }
        negotiationProduct.addError(negotiationProductDD.getField(PRODUCT),
                "supplyNegotiations.negotiationProduct.error.productAlreadyExists");

        return false;
    }

    public void updateFarestLimitDate(final DataDefinition negotiationProductDD, final Entity negotiationProduct) {
        Entity negotiation = negotiationProduct.getBelongsToField(NEGOTIATION);

        Date dueDate = (Date) negotiationProduct.getField(DUE_DATE);
        Date farestLimitDate = (Date) negotiation.getField(FARTHEST_LIMIT_DATE);

        if ((farestLimitDate == null) || farestLimitDate.before(dueDate)) {
            farestLimitDate = dueDate;

            negotiation.setField(FARTHEST_LIMIT_DATE, farestLimitDate);

            negotiation.getDataDefinition().save(negotiation);
        }
    }

    public void updateRequestForQuotationsNumber(final DataDefinition negotiationProductDD, final Entity negotiationProduct) {
        Entity negotiation = negotiationProduct.getBelongsToField(NEGOTIATION);
        Entity product = negotiationProduct.getBelongsToField(PRODUCT);

        if (product != null) {
            int requestForQuotationsNumber = supplyNegotiationsService.getRequestForQuotationProductDD().find()
                    .createAlias(REQUEST_FOR_QUOTATION, REQUEST_FOR_QUOTATION)
                    .add(SearchRestrictions.belongsTo(PRODUCT, product))
                    .add(SearchRestrictions.belongsTo(REQUEST_FOR_QUOTATION + "." + NEGOTIATION, negotiation)).list()
                    .getEntities().size();

            negotiationProduct.setField(REQUEST_FOR_QUOTATIONS_NUMBER, requestForQuotationsNumber);
        }
    }

}
