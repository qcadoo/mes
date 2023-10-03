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

import com.google.common.collect.Lists;
import com.qcadoo.mes.supplyNegotiations.constants.*;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class SupplyNegotiationsServiceImpl implements SupplyNegotiationsService {

    private static final String L_OFFER = "offer";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    public Entity getRequestForQuotation(final Long requestForQuotationId) {
        return getRequestForQuotationDD().get(requestForQuotationId);
    }

    @Override
    public Entity getRequestForQuotationProduct(final Long requestForQuotationProductId) {
        return getRequestForQuotationProductDD().get(requestForQuotationProductId);
    }

    @Override
    public Entity getOffer(final Long offerId) {
        return getOfferDD().get(offerId);
    }

    @Override
    public Entity getOfferProduct(final Long offerProductId) {
        return getOfferProductDD().get(offerProductId);
    }

    @Override
    public Entity getNegotiation(final Long negotiationId) {
        return getNegotiationDD().get(negotiationId);
    }

    @Override
    public Entity getNegotiationProduct(final Long negotiationProductId) {
        return getNegotiationProductDD().get(negotiationProductId);
    }

    @Override
    public List<Entity> getColumnsForRequests() {
        List<Entity> columns = Lists.newLinkedList();

        List<Entity> columnComponents = getColumnForRequestsDD().find()
                .addOrder(SearchOrders.asc(ColumnForRequestsFields.SUCCESSION)).list().getEntities();

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField("columnForRequests");

            columns.add(columnDefinition);
        }

        return columns;
    }

    @Override
    public List<Entity> getColumnsForOffers() {
        List<Entity> columns = Lists.newLinkedList();

        List<Entity> columnComponents = getColumnForOffersDD().find()
                .addOrder(SearchOrders.asc(ColumnForOffersFields.SUCCESSION)).list().getEntities();

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField("columnForOffers");

            columns.add(columnDefinition);
        }

        return columns;
    }

    @Override
    public DataDefinition getRequestForQuotationDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION);
    }

    @Override
    public DataDefinition getRequestForQuotationProductDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION_PRODUCT);
    }

    @Override
    public DataDefinition getOfferDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_OFFER);
    }

    @Override
    public DataDefinition getOfferProductDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_OFFER_PRODUCT);
    }

    @Override
    public DataDefinition getNegotiationDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_NEGOTIATION);
    }

    @Override
    public DataDefinition getNegotiationProductDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_NEGOTIATION_PRODUCT);
    }

    @Override
    public DataDefinition getColumnForRequestsDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_PARAMETER_COLUMN_FOR_REQUESTS);
    }

    @Override
    public DataDefinition getColumnForOffersDD() {
        return dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_PARAMETER_COLUMN_FOR_OFFERS);
    }

    @Override
    public BigDecimal getPricePerUnit(final Entity offer, final Entity product) {
        Entity offerProduct = getOfferProduct(offer, product);

        if (Objects.isNull(offerProduct)) {
            return null;
        } else {
            return offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT);
        }
    }

    private Entity getOfferProduct(final Entity offer, final Entity product) {
        return offer.getHasManyField(OfferFields.OFFER_PRODUCTS).find()
                .add(SearchRestrictions.belongsTo(OfferProductFields.PRODUCT, product)).setMaxResults(1).uniqueResult();
    }

    @Override
    public Entity getLastOfferProduct(final Entity supplier, final Entity currency, final Entity product) {
        String query = String.format("SELECT offerProduct FROM #%s_%s AS offerProduct "
                        + " INNER JOIN offerProduct.%s AS offer "
                        + " WHERE offer.%s = :state AND offer.%s = :supplier"
                        + " AND offer.%s = :currency AND offerProduct.%s = :product "
                        + " ORDER BY offer.offerDate DESC",
                SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_OFFER_PRODUCT,
                SupplyNegotiationsConstants.MODEL_OFFER, OfferFields.STATE, OfferFields.SUPPLIER, OfferFields.CURRENCY, OfferProductFields.PRODUCT);

        SearchQueryBuilder searchQueryBuilder = dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_OFFER_PRODUCT).find(query);

        searchQueryBuilder.setString("state", OfferStateStringValues.ACCEPTED);
        searchQueryBuilder.setEntity("supplier", supplier);
        searchQueryBuilder.setEntity("currency", currency);
        searchQueryBuilder.setEntity("product", product);

        return searchQueryBuilder.setMaxResults(1).uniqueResult();
    }

    @Override
    public BigDecimal getLastPricePerUnit(final Entity supplier, final Entity currency, final Entity product) {
        Entity offerProduct = getLastOfferProduct(supplier, currency, product);

        if (Objects.nonNull(offerProduct)) {
            return offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT);
        }

        return null;
    }

    @Override
    public void fillPriceField(final ViewDefinitionState view, final String priceFieldReference,
                               final BigDecimal lastPurchasePrice) {
        FieldComponent pricePerUnitField = (FieldComponent) view.getComponentByReference(priceFieldReference);

        pricePerUnitField.setFieldValue(numberService.format(lastPurchasePrice));
        pricePerUnitField.requestComponentUpdateState();
    }

    @Override
    public void fillOffer(final ViewDefinitionState view, final Entity offer) {
        LookupComponent offerLookup = (LookupComponent) view.getComponentByReference(L_OFFER);

        if (Objects.isNull(offer)) {
            offerLookup.setFieldValue(null);
        } else {
            offerLookup.setFieldValue(offer.getId());
        }

        offerLookup.requestComponentUpdateState();
    }

}
