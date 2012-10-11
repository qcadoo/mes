package com.qcadoo.mes.deliveries;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.deliveriesColumnExtension.DeliveriesColumnLoader;

public class DeliveriesOnStartupServiceTest {

    private DeliveriesOnStartupService deliveriesOnStartupService;

    @Mock
    private DeliveriesColumnLoader deliveriesColumnLoader;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesOnStartupService = new DeliveriesOnStartupService();

        ReflectionTestUtils.setField(deliveriesOnStartupService, "deliveriesColumnLoader", deliveriesColumnLoader);
    }

    @Test
    public void shouldMultiTenantEnable() {
        // given

        // when
        deliveriesOnStartupService.multiTenantEnable();

        // then
        verify(deliveriesColumnLoader).addDeliveriesColumnsForDeliveries();
        verify(deliveriesColumnLoader).addDeliveriesColumnsForOrders();
    }

    @Test
    public void shouldMultiTenantDisable() {
        // given

        // when
        deliveriesOnStartupService.multiTenantDisable();

        // then
        verify(deliveriesColumnLoader).deleteDeliveriesColumnsForDeliveries();
        verify(deliveriesColumnLoader).deleteDeliveriesColumnsForOrders();
    }

}
