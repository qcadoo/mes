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
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.NEGOTIATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.STATE;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.DUE_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.NEGOTIATION;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationState;
import com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationStateChangeDescriber;
import com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class NegotiationHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private NegotiationStateChangeDescriber describer;

    public void setInitialState(final DataDefinition negotiationDD, final Entity negotiation) {
        stateChangeEntityBuilder.buildInitial(describer, negotiation, NegotiationState.DRAFT);
    }

    public void clearStateFieldOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(STATE, NegotiationStateStringValues.DRAFT);
    }

    public void updateFarestLimitDate(final DataDefinition negotiationDD, final Entity negotiation) {
        if (negotiation.getId() != null) {
            Entity negotiationProduct = negotiation.getHasManyField(NEGOTIATION_PRODUCTS).find()
                    .add(SearchRestrictions.belongsTo(NEGOTIATION, negotiation)).addOrder(SearchOrders.desc(DUE_DATE))
                    .setMaxResults(1).uniqueResult();

            if (negotiationProduct != null) {
                Date farestLimitDate = (Date) negotiationProduct.getField(DUE_DATE);

                negotiation.setField(FARTHEST_LIMIT_DATE, farestLimitDate);
            }
        }
    }

}
