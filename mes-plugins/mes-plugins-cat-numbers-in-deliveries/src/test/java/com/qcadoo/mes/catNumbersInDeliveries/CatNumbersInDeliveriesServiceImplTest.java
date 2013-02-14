/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.catNumbersInDeliveries;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class CatNumbersInDeliveriesServiceImplTest {

    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private ProductCatalogNumbersService productCatalogNumbersService;

    @Mock
    private DeliveriesService deliveriesService;

    @Mock
    private DataDefinition deliveryProductDD;

    @Mock
    private Entity deliveryProduct, delivery, existingDelivery, supplier, existingSupplier, product, productCatalogNumber;

    @Mock
    private EntityList deliveryProducts;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        catNumbersInDeliveriesService = new CatNumbersInDeliveriesServiceImpl();

        PowerMockito.mockStatic(SearchRestrictions.class);

        ReflectionTestUtils.setField(catNumbersInDeliveriesService, "productCatalogNumbersService", productCatalogNumbersService);
        ReflectionTestUtils.setField(catNumbersInDeliveriesService, "deliveriesService", deliveriesService);
    }

    @Test
    public void shouldntUpdateProductCatalogNumberIfEntityIsntSaved() {
        // given
        given(deliveryProduct.getBelongsToField(DELIVERY)).willReturn(delivery);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(deliveryProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        catNumbersInDeliveriesService.updateProductCatalogNumber(deliveryProduct);

        // then
        verify(deliveryProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

    @Test
    public void shouldUpdateProductCatalogNumberIfEntityIsntSaved() {
        // given
        given(deliveryProduct.getBelongsToField(DELIVERY)).willReturn(delivery);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(deliveryProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        catNumbersInDeliveriesService.updateProductCatalogNumber(deliveryProduct);

        // then
        verify(deliveryProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateProductsCatalogNumbersIfEntityIsntSaved() {
        // given
        Long deliveryId = null;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        catNumbersInDeliveriesService.updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);

        // then
        verify(deliveryProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateProductsCatalogNumbersIfSupplierHasntChanged() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        catNumbersInDeliveriesService.updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);

        // then
        verify(deliveryProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateProductsCatalogNumbersIfOrderedProductsAreNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        given(delivery.getHasManyField(ORDERED_PRODUCTS)).willReturn(null);

        // when
        catNumbersInDeliveriesService.updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);

        // then
        verify(deliveryProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateProductsCatalogNumbersIfOrderedProductsArentNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        deliveryProducts = mockEntityList(Lists.newArrayList(deliveryProduct));

        given(delivery.getHasManyField(ORDERED_PRODUCTS)).willReturn(deliveryProducts);
        given(deliveryProduct.getDataDefinition()).willReturn(deliveryProductDD);
        given(deliveryProduct.getBelongsToField(PRODUCT)).willReturn(product);
        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        catNumbersInDeliveriesService.updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);

        // then
        verify(deliveryProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    // TODO lupo fix problem with test
    @Ignore
    @Test
    public void shoulUpdateProductsCatalogNumbersIfOrderedProductsArentNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        deliveryProducts = mockEntityList(Lists.newArrayList(deliveryProduct));

        given(delivery.getHasManyField(ORDERED_PRODUCTS)).willReturn(deliveryProducts);
        given(deliveryProduct.getDataDefinition()).willReturn(deliveryProductDD);
        given(deliveryProduct.getBelongsToField(PRODUCT)).willReturn(product);
        given(productCatalogNumbersService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        catNumbersInDeliveriesService.updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);

        // then
        verify(deliveryProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD).save(Mockito.any(Entity.class));
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
