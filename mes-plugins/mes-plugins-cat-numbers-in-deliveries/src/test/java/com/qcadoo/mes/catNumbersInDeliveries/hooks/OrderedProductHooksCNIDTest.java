package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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
    private Entity orderedProduct;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderedProductHooksCNID = new OrderedProductHooksCNID();

        ReflectionTestUtils.setField(orderedProductHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
    }

    @Test
    public void shouldUpdateOrderedProductCatalogNumber() {
        // given

        // when
        orderedProductHooksCNID.updateOrderedProductCatalogNumber(orderedProductDD, orderedProduct);

        // then
        verify(catNumbersInDeliveriesService).updateProductCatalogNumber(orderedProduct);
    }

}
