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
package com.qcadoo.mes.catNumbersInNegot.hooks;

import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.SUPPLIER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.PRODUCT;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.REQUEST_FOR_QUOTATION;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class RequestForQuotationProductHooksCNINTest {

    private RequestForQuotationProductHooksCNIN requestForQuotationProductHooksCNIN;

    @Mock
    private ProductCatalogNumbersService productCatalogNumbersService;

    @Mock
    private DataDefinition requestForQuotationProductDD;

    @Mock
    private Entity requestForQuotationProduct, requestForQuotation, supplier, product, productCatalogNumber;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        requestForQuotationProductHooksCNIN = new RequestForQuotationProductHooksCNIN();

        ReflectionTestUtils.setField(requestForQuotationProductHooksCNIN, "productCatalogNumbersService",
                productCatalogNumbersService);
    }

    @Test
    public void shouldntUpdateRequestForQuotationProductCatalogNumbersIfProductCatalogNumberIsNull() {
        // given
        given(requestForQuotationProduct.getBelongsToField(REQUEST_FOR_QUOTATION)).willReturn(requestForQuotation);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(requestForQuotationProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        requestForQuotationProductHooksCNIN.updateRequestForQuotationProductCatalogNumber(requestForQuotationProductDD,
                requestForQuotationProduct);

        // then
        verify(requestForQuotationProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

    @Test
    public void shouldUpdateRequestForQuotationProductCatalogNumbersIfEntityIsntSaved() {
        // given
        given(requestForQuotationProduct.getBelongsToField(REQUEST_FOR_QUOTATION)).willReturn(requestForQuotation);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(requestForQuotationProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        requestForQuotationProductHooksCNIN.updateRequestForQuotationProductCatalogNumber(requestForQuotationProductDD,
                requestForQuotationProduct);

        // then
        verify(requestForQuotationProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

}
