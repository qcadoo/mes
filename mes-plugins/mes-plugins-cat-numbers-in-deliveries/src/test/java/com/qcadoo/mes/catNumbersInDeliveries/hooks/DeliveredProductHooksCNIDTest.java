package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.catNumbersInDeliveries.CatNumbersInDeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class DeliveredProductHooksCNIDTest {

    private DeliveredProductHooksCNID deliveredProductHooksCNID;

    @Mock
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DataDefinition deliveredProductDD;

    @Mock
    private Entity deliveredProduct, delivery, supplier, product, productCatalogNumber;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveredProductHooksCNID = new DeliveredProductHooksCNID();

        ReflectionTestUtils.setField(deliveredProductHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
    }

    @Test
    public void shouldntUpdateDeliveredProductCatalogNumbersIfEntityIsntSaved() {
        // given
        given(deliveredProduct.getBelongsToField(DELIVERY)).willReturn(delivery);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)).willReturn(product);

        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(null);

        // when
        deliveredProductHooksCNID.updateDeliveredProductCatalogNumber(deliveredProductDD, deliveredProduct);

        // then
        Mockito.verify(deliveredProduct, never()).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

    @Test
    public void shouldUpdateDeliveredProductCatalogNumbersIfEntityIsntSaved() {
        // given
        given(deliveredProduct.getBelongsToField(DELIVERY)).willReturn(delivery);
        given(delivery.getBelongsToField(SUPPLIER)).willReturn(supplier);

        given(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)).willReturn(product);

        given(catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier)).willReturn(productCatalogNumber);

        // when
        deliveredProductHooksCNID.updateDeliveredProductCatalogNumber(deliveredProductDD, deliveredProduct);

        // then
        Mockito.verify(deliveredProduct).setField(Mockito.anyString(), Mockito.any(Entity.class));
    }

}
