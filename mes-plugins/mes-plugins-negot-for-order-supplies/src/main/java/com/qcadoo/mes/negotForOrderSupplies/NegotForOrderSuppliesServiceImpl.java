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
package com.qcadoo.mes.negotForOrderSupplies;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.negotForOrderSupplies.constants.CoverageProductFieldsNFOS;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductState;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Component
public class NegotForOrderSuppliesServiceImpl implements NegotForOrderSuppliesService {

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Override
    public Entity createNegotiation(final Entity materialRequirementCoverage) {
        Entity negotiation = null;

        List<Entity> negotiationProducts = createNegotiationProducts(filterCoverageProducts(materialRequirementCoverage));

        if (!negotiationProducts.isEmpty()) {
            negotiation = supplyNegotiationsService.getNegotiationDD().create();

            negotiation.setField(NegotiationFields.NUMBER, numberGeneratorService.generateNumber(
                    SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_NEGOTIATION));
            negotiation.setField(NegotiationFields.NEGOTIATION_PRODUCTS, negotiationProducts);

            negotiation = negotiation.getDataDefinition().save(negotiation);
        }

        return negotiation;
    }

    private List<Entity> filterCoverageProducts(final Entity materialRequirementCoverage) {
        return materialRequirementCoverage.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_PRODUCTS).find()
                .add(SearchRestrictions.eq(CoverageProductFields.STATE, CoverageProductState.LACK.getStringValue())).list()
                .getEntities();
    }

    private List<Entity> createNegotiationProducts(final List<Entity> coverageProducts) {
        List<Entity> negotiationProducts = Lists.newArrayList();

        for (Entity coverageProduct : coverageProducts) {
            BigDecimal neededQuantity = getNeededQuantity(coverageProduct);

            if (BigDecimal.ZERO.compareTo(neededQuantity) < 0) {
                negotiationProducts.add(createNegotiationProduct(coverageProduct));
            }
        }

        return negotiationProducts;
    }

    @Override
    public Entity createNegotiationProduct(final Entity coverageProduct) {
        Entity negotiationProduct = supplyNegotiationsService.getNegotiationProductDD().create();

        BigDecimal neededQuantity = getNeededQuantity(coverageProduct);

        negotiationProduct.setField(NegotiationProductFields.PRODUCT,
                coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT));
        negotiationProduct.setField(NegotiationProductFields.NEEDED_QUANTITY, numberService.setScaleWithDefaultMathContext(neededQuantity));
        negotiationProduct.setField(NegotiationProductFields.DUE_DATE,
                coverageProduct.getField(CoverageProductFields.LACK_FROM_DATE));

        return negotiationProduct;
    }

    @Override
    public BigDecimal getNeededQuantity(final Entity coverageProduct) {
        BigDecimal reservceMissingQuantity = BigDecimalUtils.convertNullToZero(coverageProduct
                .getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY));
        BigDecimal negotiatiedQuantity = BigDecimalUtils.convertNullToZero(coverageProduct
                .getDecimalField(CoverageProductFieldsNFOS.NEGOTIATED_QUANTITY));

        return reservceMissingQuantity.abs(numberService.getMathContext()).subtract(negotiatiedQuantity,
                numberService.getMathContext());
    }

}
