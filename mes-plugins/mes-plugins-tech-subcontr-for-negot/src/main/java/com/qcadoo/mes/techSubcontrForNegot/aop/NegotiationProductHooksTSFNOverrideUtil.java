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
package com.qcadoo.mes.techSubcontrForNegot.aop;

import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.NEGOTIATION;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.PRODUCT;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.REQUEST_FOR_QUOTATIONS_NUMBER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.REQUEST_FOR_QUOTATION;
import static com.qcadoo.mes.techSubcontrForNegot.constants.NegotiationProductFieldsTSFN.OPERATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.techSubcontrForNegot.constants.TechSubcontrForNegotConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class NegotiationProductHooksTSFNOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    public boolean shouldOverride() {
        return pluginStateResolver.isEnabled(TechSubcontrForNegotConstants.PLUGIN_IDENTIFIER);
    }

    public boolean checkIfNegotitionProductAlreadyExists(final DataDefinition negotiationProductDD,
            final Entity negotiationProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = negotiationProductDD.find()
                .add(SearchRestrictions.belongsTo(NEGOTIATION, negotiationProduct.getBelongsToField(NEGOTIATION)))
                .add(SearchRestrictions.belongsTo(PRODUCT, negotiationProduct.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(OPERATION, negotiationProduct.getBelongsToField(OPERATION)));

        if (negotiationProduct.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", negotiationProduct.getId()));
        }

        Entity negotiationProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (negotiationProductFromDB == null) {
            return true;
        } else {
            negotiationProduct.addError(negotiationProductDD.getField(PRODUCT),
                    "supplyNegotiations.negotiationProduct.error.productWithOperationAlreadyExists");

            return false;
        }
    }

    public void updateRequestForQuotationsNumber(final DataDefinition negotiationProductDD, final Entity negotiationProduct) {
        Entity negotiation = negotiationProduct.getBelongsToField(NEGOTIATION);
        Entity product = negotiationProduct.getBelongsToField(PRODUCT);
        Entity operation = negotiationProduct.getBelongsToField(OPERATION);

        if (product != null) {
            int requestForQuotationsNumber = supplyNegotiationsService.getRequestForQuotationProductDD().find()
                    .createAlias(REQUEST_FOR_QUOTATION, REQUEST_FOR_QUOTATION)
                    .add(SearchRestrictions.belongsTo(REQUEST_FOR_QUOTATION + "." + NEGOTIATION, negotiation))
                    .add(SearchRestrictions.belongsTo(PRODUCT, product)).add(SearchRestrictions.belongsTo(OPERATION, operation))
                    .list().getEntities().size();

            negotiationProduct.setField(REQUEST_FOR_QUOTATIONS_NUMBER, requestForQuotationsNumber);
        }
    }

}
