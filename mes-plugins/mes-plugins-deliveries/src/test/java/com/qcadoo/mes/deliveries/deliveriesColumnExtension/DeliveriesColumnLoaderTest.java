package com.qcadoo.mes.deliveries.deliveriesColumnExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.DeliveriesColumnLoaderService;

public class DeliveriesColumnLoaderTest {

    private DeliveriesColumnLoader deliveriesColumnLoader;

    @Mock
    private DeliveriesColumnLoaderService deliveriesColumnLoaderService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesColumnLoader = new DeliveriesColumnLoader();

        ReflectionTestUtils.setField(deliveriesColumnLoader, "deliveriesColumnLoaderService", deliveriesColumnLoaderService);
    }

    @Test
    public void shouldAddDeliveriesColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.addDeliveriesColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService).fillColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldntAddDeliveriesColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.addDeliveriesColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService, never()).fillColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldDeleteDeliveriesColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.deleteDeliveriesColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService).clearColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldntDeleteDeliveriesColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.deleteDeliveriesColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService, never()).clearColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldAddDeliveriesColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.addDeliveriesColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService).fillColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shouldntAddDeliveriesColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.addDeliveriesColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService, never()).fillColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shouldDeleteDeliveriesColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.deleteDeliveriesColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService).clearColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shoulntdDeleteDeliveriesColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.deleteDeliveriesColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService, never()).clearColumnsForOrders(Mockito.anyString());
    }

}
