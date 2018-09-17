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
package com.qcadoo.mes.negotForOrderSupplies.hooks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.negotForOrderSupplies.constants.CoverageProductFieldsNFOS;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields;
import com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class CoverageProductHooksNFOS {

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private NumberService numberService;

    public void onCreate(final DataDefinition coverageProductDD, final Entity coverageProduct) {
        fillNegotiatedQuantity(coverageProduct);
    }

    private void fillNegotiatedQuantity(final Entity coverageProduct) {
        Entity materialRequirementCoverage = coverageProduct
                .getBelongsToField(CoverageProductFields.MATERIAL_REQUIREMENT_COVERAGE);
        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);
        Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

        coverageProduct.setField(CoverageProductFieldsNFOS.NEGOTIATED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(getNegotiatedQuantity(product, coverageToDate)));
    }

    private BigDecimal getNegotiatedQuantity(final Entity product, final Date coverageToDate) {
        BigDecimal negotiatedQuantity = BigDecimal.ZERO;

        List<Entity> negotiationProducts = supplyNegotiationsService
                .getNegotiationProductDD()
                .find()
                .createAlias(NegotiationProductFields.NEGOTIATION, NegotiationProductFields.NEGOTIATION)
                .add(SearchRestrictions.belongsTo(NegotiationProductFields.PRODUCT, product))
                .add(SearchRestrictions.lt(NegotiationProductFields.DUE_DATE, coverageToDate))
                .add(SearchRestrictions.or(SearchRestrictions.eq(NegotiationProductFields.NEGOTIATION + "."
                        + NegotiationFields.STATE, NegotiationStateStringValues.DRAFT), SearchRestrictions.eq(
                        NegotiationProductFields.NEGOTIATION + "." + NegotiationFields.STATE,
                        NegotiationStateStringValues.GENERATED_REQUESTS))).list().getEntities();

        for (Entity negotiationProduct : negotiationProducts) {
            BigDecimal neededQuantity = negotiationProduct.getDecimalField(NegotiationProductFields.NEEDED_QUANTITY);

            if (neededQuantity != null) {
                negotiatedQuantity = negotiatedQuantity.add(neededQuantity, numberService.getMathContext());
            }
        }

        return negotiatedQuantity;
    }

}
