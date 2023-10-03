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
package com.qcadoo.mes.supplyNegotiations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

import java.math.BigDecimal;
import java.util.List;

public interface SupplyNegotiationsService {

    /**
     * Gets request for quotation
     *
     * @param requestForQuotationId
     * @return request for quotation
     */
    Entity getRequestForQuotation(final Long requestForQuotationId);

    /**
     * Gets request for quotation product
     *
     * @param requestForQuotationProductId
     * @return request for quotation product
     */
    Entity getRequestForQuotationProduct(final Long requestForQuotationProductId);

    /**
     * Gets offer
     *
     * @param offerId
     * @return offer
     */
    Entity getOffer(final Long offerId);

    /**
     * Gets offer product
     *
     * @param offerProductId
     * @return offer product
     */
    Entity getOfferProduct(final Long offerProductId);

    /**
     * Gets negotiation
     *
     * @param negotiationId
     * @return negotiation
     */
    Entity getNegotiation(final Long negotiationId);

    /**
     * Gets negotiation product
     *
     * @param negotiationProductId
     * @return negotiation product
     */
    Entity getNegotiationProduct(final Long negotiationProductId);

    /**
     * Gets request for quotation data definition
     *
     * @return request for quotation data definition
     */
    DataDefinition getRequestForQuotationDD();

    /**
     * Gets request for quotation product data definition
     *
     * @return request for quotation product data definition
     */
    DataDefinition getRequestForQuotationProductDD();

    /**
     * Gets offer data definition
     *
     * @return offer data definition
     */
    DataDefinition getOfferDD();

    /**
     * Gets offer product data definition
     *
     * @return offer product data definition
     */
    DataDefinition getOfferProductDD();

    /**
     * Gets negotiation data definition
     *
     * @return negotiation data definition
     */
    DataDefinition getNegotiationDD();

    /**
     * Gets negotiation product data definition
     *
     * @return negotiation product data definition
     */
    DataDefinition getNegotiationProductDD();

    /**
     * Gets list of columns for requests
     *
     * @return list of columns for requests
     */
    List<Entity> getColumnsForRequests();

    /**
     * Gets list of columns for offers
     *
     * @return list of columns for offers
     */
    List<Entity> getColumnsForOffers();

    /**
     * Gets column for requests data definition
     *
     * @return column for requests data definition
     */
    DataDefinition getColumnForRequestsDD();

    /**
     * Gets column for offers data definition
     *
     * @return column for offers data definition
     */
    DataDefinition getColumnForOffersDD();

    /**
     * Gets price per unit for given offer and product
     *
     * @param offer
     * @param product
     * @return price per unit
     */
    BigDecimal getPricePerUnit(final Entity offer, final Entity product);

    /**
     * Gets last offer product for given supplier and product
     *
     * @param supplier supplier
     * @param currency  currency
     * @param product  product
     * @return offerProduct if entity exist or null
     */
    Entity getLastOfferProduct(final Entity supplier, final Entity currency, final Entity product);

    /**
     * Gets last price per unit
     *
     * @param supplier supplier
     * @param currency  currency
     * @param product  product
     * @return lastPurchase price if entity exist or null
     */
    BigDecimal getLastPricePerUnit(final Entity supplier, final Entity currency, final Entity product);

    /**
     * Fills last purchase price
     *
     * @param view                view
     * @param priceFieldReference price field reference
     * @param lastPurchasePrice   last purchase price
     */
    void fillPriceField(final ViewDefinitionState view, final String priceFieldReference, final BigDecimal lastPurchasePrice);

    /**
     * Fill offer
     *
     * @param view  view
     * @param offer offer
     */
    void fillOffer(final ViewDefinitionState view, final Entity offer);

}
