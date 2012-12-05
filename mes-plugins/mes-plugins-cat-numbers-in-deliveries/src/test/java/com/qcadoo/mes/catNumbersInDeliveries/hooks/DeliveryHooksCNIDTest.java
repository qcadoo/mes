package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.catNumbersInDeliveries.CatNumbersInDeliveriesService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class DeliveryHooksCNIDTest {

    private DeliveryHooksCNID deliveryHooksCNID;

    @Mock
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DeliveriesService deliveriesService;

    @Mock
    private DataDefinition deliveryDD, deliveryProductDD, orderedProductDD;

    @Mock
    private Entity delivery, existingDelivery, deliveredProduct, orderedProduct, supplier, existingSupplier, product,
            productCatalogNumber;

    @Mock
    private EntityList deliveredProducts, orderedProducts;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveryHooksCNID = new DeliveryHooksCNID();

        ReflectionTestUtils.setField(deliveryHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
        ReflectionTestUtils.setField(deliveryHooksCNID, "deliveriesService", deliveriesService);
    }

    @Test
    public void shouldntUpdateOrderedProductsCatalogNumbersIfEntityIsntSaved() {
        // given
        Long deliveryId = null;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(orderedProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(orderedProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateOrderedProductsCatalogNumbersIfSupplierHasntChanged() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(orderedProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(orderedProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateOrderedProductsCatalogNumbersIfOrderedProductsAreNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        given(delivery.getHasManyField(ORDERED_PRODUCTS)).willReturn(null);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(orderedProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(orderedProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateOrderedProductsCatalogNumbersIfOrderedProductsArentNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        orderedProducts = mockEntityList(Lists.newArrayList(orderedProduct));

        given(delivery.getHasManyField(ORDERED_PRODUCTS)).willReturn(orderedProducts);
        given(orderedProduct.getDataDefinition()).willReturn(orderedProductDD);
        given(orderedProduct.getBelongsToField(PRODUCT)).willReturn(product);
        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(orderedProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(orderedProductDD, never()).save(Mockito.any(Entity.class));
    }

    // TODO lupo fix problem with test
    @Ignore
    @Test
    public void shoulUpdateOrderedProductsCatalogNumbersIfOrderedProductsArentNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        orderedProducts = mockEntityList(Lists.newArrayList(orderedProduct));

        given(delivery.getHasManyField(ORDERED_PRODUCTS)).willReturn(orderedProducts);
        given(orderedProduct.getDataDefinition()).willReturn(orderedProductDD);
        given(orderedProduct.getBelongsToField(PRODUCT)).willReturn(product);
        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        deliveryHooksCNID.updateOrderedProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(orderedProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(orderedProductDD).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateDeliveredProductsCatalogNumbersIfEntityIsntSaved() {
        // given
        Long deliveryId = null;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(deliveredProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateDeliveredProductsCatalogNumbersIfSupplierHasntChanged() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(deliveredProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateDeliveredProductsCatalogNumbersIfDeliveredProductsAreNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        given(delivery.getHasManyField(DELIVERED_PRODUCTS)).willReturn(null);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(deliveredProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateDeliveredProductsCatalogNumbersIfDeliveredProductsArentNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        deliveredProducts = mockEntityList(Lists.newArrayList(deliveredProduct));

        given(delivery.getHasManyField(DELIVERED_PRODUCTS)).willReturn(deliveredProducts);
        given(deliveredProduct.getDataDefinition()).willReturn(deliveryProductDD);
        given(deliveredProduct.getBelongsToField(PRODUCT)).willReturn(product);
        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(deliveredProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
        verify(deliveryProductDD, never()).save(Mockito.any(Entity.class));
    }

    // TODO lupo fix problem with test
    @Ignore
    @Test
    public void shoulUpdateDeliveredProductsCatalogNumbersIfDeliveredProductsArentNull() {
        // given
        Long deliveryId = 1L;

        given(delivery.getId()).willReturn(deliveryId);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(null);
        given(deliveriesService.getDelivery(deliveryId)).willReturn(existingDelivery);
        given(existingDelivery.getBelongsToField(SUPPLIER)).willReturn(existingSupplier);

        deliveredProducts = mockEntityList(Lists.newArrayList(deliveredProduct));

        given(delivery.getHasManyField(DELIVERED_PRODUCTS)).willReturn(deliveredProducts);
        given(deliveredProduct.getDataDefinition()).willReturn(deliveryProductDD);
        given(deliveredProduct.getBelongsToField(PRODUCT)).willReturn(product);
        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(deliveredProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
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
