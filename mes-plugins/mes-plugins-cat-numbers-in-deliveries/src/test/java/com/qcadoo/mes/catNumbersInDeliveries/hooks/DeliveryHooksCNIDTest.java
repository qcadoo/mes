package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.catNumbersInDeliveries.CatNumbersInDeliveriesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class DeliveryHooksCNIDTest {

    private DeliveryHooksCNID deliveryHooksCNID;

    @Mock
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DataDefinition deliveryDD;

    @Mock
    private Entity delivery;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveryHooksCNID = new DeliveryHooksCNID();

        ReflectionTestUtils.setField(deliveryHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
    }

    @Test
    public void shouldUpdateOrderedProductsCatalogNumbers() {
        // given

        // when
        deliveryHooksCNID.updateOrderedProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(catNumbersInDeliveriesService).updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);
    }

    @Test
    public void shouldUpdateDeliveredProductsCatalogNumbers() {
        // given

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(catNumbersInDeliveriesService).updateProductsCatalogNumbers(delivery, DELIVERED_PRODUCTS);
    }

}
