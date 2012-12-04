package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.catNumbersInDeliveries.CatNumbersInDeliveriesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderedProductHooksCNIDTest {

    private OrderedProductHooksCNID orderedProductHooksCNID;

    @Mock
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DataDefinition orderedProductDD;

    @Mock
    private Entity orderedProduct, delivery, supplier, product, productCatalogNumber;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderedProductHooksCNID = new OrderedProductHooksCNID();

        ReflectionTestUtils.setField(orderedProductHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
    }

    @Test
    public void shouldntUpdateOrderedProductCatalogNumbersIfEntityIsntSaved() {
        // given
        given(orderedProduct.getBelongsToField(DELIVERY)).willReturn(delivery);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(orderedProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        orderedProductHooksCNID.updateOrderedProductCatalogNumber(orderedProductDD, orderedProduct);

        // then
        Mockito.verify(orderedProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

    @Test
    public void shouldUpdateOrderedProductCatalogNumbersIfEntityIsntSaved() {
        // given
        given(orderedProduct.getBelongsToField(DELIVERY)).willReturn(delivery);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(orderedProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        orderedProductHooksCNID.updateOrderedProductCatalogNumber(orderedProductDD, orderedProduct);

        // then
        Mockito.verify(orderedProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

}
