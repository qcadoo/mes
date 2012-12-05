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

public class DeliveredProductHooksCNIDTest {

    private DeliveredProductHooksCNID deliveredProductHooksCNID;

    @Mock
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DataDefinition deliveredProductDD;

    @Mock
    private Entity deliveredProduct;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveredProductHooksCNID = new DeliveredProductHooksCNID();

        ReflectionTestUtils.setField(deliveredProductHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
    }

    @Test
    public void shouldUpdateDeliveredProductCatalogNumber() {
        // given

        // when
        deliveredProductHooksCNID.updateDeliveredProductCatalogNumber(deliveredProductDD, deliveredProduct);

        // then
        verify(catNumbersInDeliveriesService).updateProductCatalogNumber(deliveredProduct);
    }

}
