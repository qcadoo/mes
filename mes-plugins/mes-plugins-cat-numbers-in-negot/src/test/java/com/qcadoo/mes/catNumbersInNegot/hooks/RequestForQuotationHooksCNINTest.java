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

import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.SUPPLIER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.PRODUCT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class RequestForQuotationHooksCNINTest {

    private RequestForQuotationHooksCNIN requestForQuotationHooksCNIN;

    @Mock
    private ProductCatalogNumbersService productCatalogNumbersService;

    @Mock
    private SupplyNegotiationsService supplyNegotiationsService;

    @Mock
    private DataDefinition requestForQuotationDD, requestForQuotationProductDD;

    @Mock
    private Entity requestForQuotation, existingRequestForQuotation, requestForQuotationProduct, supplier, existingSupplier,
            product, productCatalogNumber;

    @Mock
    private EntityList requestForQuotationProducts;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        requestForQuotationHooksCNIN = new RequestForQuotationHooksCNIN();

        ReflectionTestUtils.setField(requestForQuotationHooksCNIN, "productCatalogNumbersService", productCatalogNumbersService);
        ReflectionTestUtils.setField(requestForQuotationHooksCNIN, "supplyNegotiationsService", supplyNegotiationsService);
    }

    @Test
    public void shouldntUpdateRequestForQuotationProductsCatalogNumbersIfEntityIsntSaved() {
        // given
        Long requestForQuotationId = null;

        given(requestForQuotation.getId()).willReturn(requestForQuotationId);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        requestForQuotationHooksCNIN.updateRequestForQuotationProductsCatalogNumbers(requestForQuotationDD, requestForQuotation);

        // then
        verify(requestForQuotationProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(requestForQuotationProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateRequestForQuotationProductsCatalogNumbersIfSupplierHasntChanged() {
        // given
        Long requestForQuotationId = 1L;

        given(requestForQuotation.getId()).willReturn(requestForQuotationId);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(supplier);
        given(supplyNegotiationsService.getRequestForQuotation(requestForQuotationId)).willReturn(existingRequestForQuotation);
        given(existingRequestForQuotation.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        requestForQuotationHooksCNIN.updateRequestForQuotationProductsCatalogNumbers(requestForQuotationDD, requestForQuotation);

        // then
        verify(requestForQuotationProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(requestForQuotationProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateRequestForQuotationProductsCatalogNumbersIfRequestForQuotationProductsAreNull() {
        // given
        Long requestForQuotationId = 1L;

        given(requestForQuotation.getId()).willReturn(requestForQuotationId);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(null);
        given(supplyNegotiationsService.getRequestForQuotation(requestForQuotationId)).willReturn(existingRequestForQuotation);
        given(existingRequestForQuotation.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        given(requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS)).willReturn(null);

        // when
        requestForQuotationHooksCNIN.updateRequestForQuotationProductsCatalogNumbers(requestForQuotationDD, requestForQuotation);

        // then
        verify(requestForQuotationProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(requestForQuotationProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateRequestForQuotationProductsCatalogNumbersIfProductCatalogNumberIsNull() {
        // given
        Long requestForQuotationId = 1L;

        given(requestForQuotation.getId()).willReturn(requestForQuotationId);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(null);
        given(supplyNegotiationsService.getRequestForQuotation(requestForQuotationId)).willReturn(existingRequestForQuotation);
        given(existingRequestForQuotation.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        requestForQuotationProducts = mockEntityList(Lists.newArrayList(requestForQuotationProduct));

        given(requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS)).willReturn(requestForQuotationProducts);
        given(requestForQuotationProduct.getDataDefinition()).willReturn(requestForQuotationProductDD);
        given(requestForQuotationProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        requestForQuotationHooksCNIN.updateRequestForQuotationProductsCatalogNumbers(requestForQuotationDD, requestForQuotation);

        // then
        verify(requestForQuotationProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(requestForQuotationProductDD, never()).save(Mockito.any(Entity.class));
    }

    // TODO lupo fix problem with test
    @Ignore
    @Test
    public void shouldUpdateRequestForQuotationProductsCatalogNumbersIfRequestForQuotationProductsArentNull() {
        // given
        Long requestForQuotationId = 1L;

        given(requestForQuotation.getId()).willReturn(requestForQuotationId);
        given(requestForQuotation.getBelongsToField(SUPPLIER)).willReturn(null);
        given(supplyNegotiationsService.getRequestForQuotation(requestForQuotationId)).willReturn(existingRequestForQuotation);
        given(existingRequestForQuotation.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        requestForQuotationProducts = mockEntityList(Lists.newArrayList(requestForQuotationProduct));

        given(requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS)).willReturn(requestForQuotationProducts);
        given(requestForQuotationProduct.getDataDefinition()).willReturn(requestForQuotationProductDD);
        given(requestForQuotationProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        requestForQuotationHooksCNIN.updateRequestForQuotationProductsCatalogNumbers(requestForQuotationDD, requestForQuotation);

        // then
        verify(requestForQuotationProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(requestForQuotationProductDD).save(Mockito.any(Entity.class));
    }

    private static EntityList mockEntityList(final List<Entity> entities) {
        final EntityList entitiesList = mock(EntityList.class);

        given(entitiesList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });

        given(entitiesList.isEmpty()).willReturn(entities.isEmpty());

        return entitiesList;
    }

}
