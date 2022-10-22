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
package com.qcadoo.mes.supplyNegotiations.states;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OfferStateChangeListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void setLastAndAveragePriceToProduct(final StateChangeContext stateChangeContext) {
        Entity offer = stateChangeContext.getOwner();
        setLastPriceFromOffer(offer.getHasManyField(OfferFields.OFFER_PRODUCTS));
        setAveragePriceFromOffer(offer.getHasManyField(OfferFields.OFFER_PRODUCTS));
    }

    private void setLastPriceFromOffer(final List<Entity> offerProducts) {
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        for (Entity offerProduct : offerProducts) {
            Entity product = productDD.get(offerProduct.getBelongsToField(OfferProductFields.PRODUCT).getId());
            BigDecimal lastOfferCost = offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT);
            product.setField("lastOfferCost", lastOfferCost);
            productDD.save(product);
        }
    }

    private void setAveragePriceFromOffer(final List<Entity> offerProducts) {
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        for (Entity offerProduct : offerProducts) {
            Entity product = productDD.get(offerProduct.getBelongsToField(OfferProductFields.PRODUCT).getId());
            BigDecimal averageOfferCost = countAveragePrice(product);
            product.setField("averageOfferCost", averageOfferCost);
            productDD.save(product);
        }
    }

    private BigDecimal countAveragePrice(final Entity product) {
        List<Entity> offerProducts = dataDefinitionService
                .get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_OFFER_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(OfferProductFields.PRODUCT, product)).list().getEntities();
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal averagePrice = BigDecimal.ZERO;
        if (offerProducts.isEmpty()) {
            return averagePrice;
        }
        for (Entity offerProduct : offerProducts) {
            totalPrice = totalPrice.add(offerProduct.getDecimalField(OfferProductFields.TOTAL_PRICE));
            totalQuantity = totalQuantity.add(offerProduct.getDecimalField(OfferProductFields.QUANTITY));
        }
        MathContext mc = numberService.getMathContext();
        averagePrice = numberService.setScaleWithDefaultMathContext(totalPrice.divide(totalQuantity, mc));
        return averagePrice;
    }
}
