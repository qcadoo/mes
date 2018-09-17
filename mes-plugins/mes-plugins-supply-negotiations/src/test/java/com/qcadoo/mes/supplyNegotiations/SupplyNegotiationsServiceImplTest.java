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

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class SupplyNegotiationsServiceImplTest {

    private SupplyNegotiationsService supplyNegotiationsService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition requestForQuotationDD, requestForQuotationProductDD, offerDD, offerProductDD, negotiationDD,
            negotiationProductDD, columnForRequestsDD, columnForOffersDD;

    @Mock
    private Entity requestForQuotation, requestForQuotationProduct, offer, offerProduct, negotiation, negotiationProduct;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        supplyNegotiationsService = new SupplyNegotiationsServiceImpl();

        ReflectionTestUtils.setField(supplyNegotiationsService, "dataDefinitionService", dataDefinitionService);

        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION)).willReturn(requestForQuotationDD);
        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION_PRODUCT))
                .willReturn(requestForQuotationProductDD);
        given(dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_OFFER))
                .willReturn(offerDD);
        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_OFFER_PRODUCT)).willReturn(offerProductDD);
        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_NEGOTIATION)).willReturn(negotiationDD);
        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_NEGOTIATION_PRODUCT)).willReturn(negotiationProductDD);
        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS)).willReturn(columnForRequestsDD);
        given(
                dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS)).willReturn(columnForOffersDD);
    }

    @Test
    public void shouldReturnNullWhenGetRequestForQuotation() {
        // given
        Long requestForQuotationId = null;

        given(requestForQuotationDD.get(requestForQuotationId)).willReturn(null);

        // when
        Entity result = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnRequestForQuotationWhenGetRequestForQuotation() {
        // given
        Long requestForQuotationId = 1L;

        given(requestForQuotationDD.get(requestForQuotationId)).willReturn(requestForQuotation);

        // when
        Entity result = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);

        // then
        assertEquals(requestForQuotation, result);
    }

    @Test
    public void shouldReturnNullWhenGetRequestForQuotationProduct() {
        // given
        Long requestForQuotationProductId = null;

        given(requestForQuotationProductDD.get(requestForQuotationProductId)).willReturn(null);

        // when
        Entity result = supplyNegotiationsService.getRequestForQuotationProduct(requestForQuotationProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnRequestForQuotationProductWhenGetRequestForQuotationProduct() {
        // given
        Long requestForQuotationProductId = 1L;

        given(requestForQuotationProductDD.get(requestForQuotationProductId)).willReturn(requestForQuotationProduct);

        // when
        Entity result = supplyNegotiationsService.getRequestForQuotationProduct(requestForQuotationProductId);

        // then
        assertEquals(requestForQuotationProduct, result);
    }

    @Test
    public void shouldReturnNullWhenGetOffer() {
        // given
        Long offerId = null;

        given(offerDD.get(offerId)).willReturn(null);

        // when
        Entity result = supplyNegotiationsService.getOffer(offerId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnOfferWhenGetOffer() {
        // given
        Long offerId = 1L;

        given(offerDD.get(offerId)).willReturn(offer);

        // when
        Entity result = supplyNegotiationsService.getOffer(offerId);

        // then
        assertEquals(offer, result);
    }

    @Test
    public void shouldReturnNullWhenGetOfferProduct() {
        // given
        Long offerProductId = null;

        given(offerProductDD.get(offerProductId)).willReturn(null);

        // when
        Entity result = supplyNegotiationsService.getOfferProduct(offerProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnOfferProductWhenGetOfferProduct() {
        // given
        Long offerProductId = 1L;

        given(offerProductDD.get(offerProductId)).willReturn(offerProduct);

        // when
        Entity result = supplyNegotiationsService.getOfferProduct(offerProductId);

        // then
        assertEquals(offerProduct, result);
    }

    @Test
    public void shouldReturnNullWhenGetNegotiation() {
        // given
        Long negotiationId = null;

        given(negotiationDD.get(negotiationId)).willReturn(null);

        // when
        Entity result = supplyNegotiationsService.getOffer(negotiationId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnNegotiationWhenGetNegotiation() {
        // given
        Long negotiationId = 1L;

        given(negotiationDD.get(negotiationId)).willReturn(negotiation);

        // when
        Entity result = supplyNegotiationsService.getNegotiation(negotiationId);

        // then
        assertEquals(negotiation, result);
    }

    @Test
    public void shouldReturnNullWhenGetNegotiationProduct() {
        // given
        Long negotiationProductId = null;

        given(negotiationProductDD.get(negotiationProductId)).willReturn(null);

        // when
        Entity result = supplyNegotiationsService.getNegotiationProduct(negotiationProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnNegotiationProductWhenGetNeogitaitonProduct() {
        // given
        Long negotiationProductId = 1L;

        given(negotiationProductDD.get(negotiationProductId)).willReturn(negotiationProduct);

        // when
        Entity result = supplyNegotiationsService.getNegotiationProduct(negotiationProductId);

        // then
        assertEquals(negotiationProduct, result);
    }

    @Test
    public void shouldReturnRequestForQuotationDD() {
        // given

        // when
        DataDefinition result = supplyNegotiationsService.getRequestForQuotationDD();

        // then
        assertEquals(requestForQuotationDD, result);
    }

    @Test
    public void shouldReturnRequestForQuotationProductDD() {
        // given

        // when
        DataDefinition result = supplyNegotiationsService.getRequestForQuotationProductDD();

        // then
        assertEquals(requestForQuotationProductDD, result);
    }

    @Test
    public void shouldReturnOfferDD() {
        // given

        // when
        DataDefinition result = supplyNegotiationsService.getOfferDD();

        // then
        assertEquals(offerDD, result);
    }

    @Test
    public void shouldReturnOfferProductDD() {
        // given

        // when
        DataDefinition result = supplyNegotiationsService.getOfferProductDD();

        // then
        assertEquals(offerProductDD, result);
    }

    @Test
    public void shouldReturnNegotiationDD() {
        // given

        // when
        DataDefinition result = supplyNegotiationsService.getNegotiationDD();

        // then
        assertEquals(negotiationDD, result);
    }

    @Test
    public void shouldReturnNegotiationProductDD() {
        // given

        // when
        DataDefinition result = supplyNegotiationsService.getNegotiationProductDD();

        // then
        assertEquals(negotiationProductDD, result);
    }

}
