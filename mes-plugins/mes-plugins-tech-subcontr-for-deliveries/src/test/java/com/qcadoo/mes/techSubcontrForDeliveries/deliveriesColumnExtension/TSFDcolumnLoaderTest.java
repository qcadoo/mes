package com.qcadoo.mes.techSubcontrForDeliveries.deliveriesColumnExtension;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.DeliveriesColumnLoaderService;

public class TSFDcolumnLoaderTest {

    private TSFDcolumnLoader tSFDcolumnLoader;

    @Mock
    private DeliveriesColumnLoaderService deliveriesColumnLoaderService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        tSFDcolumnLoader = new TSFDcolumnLoader();

        ReflectionTestUtils.setField(tSFDcolumnLoader, "deliveriesColumnLoaderService", deliveriesColumnLoaderService);
    }

    @Test
    public void shouldAddTSFDcolumnsForDeliveries() {
        // given

        // when
        tSFDcolumnLoader.addTSFDcolumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService).fillColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldDeleteTSFDcolumnsForDeliveries() {
        // given

        // when
        tSFDcolumnLoader.deleteTSFDcolumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService).clearColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldAddTSFDcolumnsForOrders() {
        // given

        // when
        tSFDcolumnLoader.addTSFDcolumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService).fillColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shouldDeleteTSFDcolumnsForOrders() {
        // given

        // when
        tSFDcolumnLoader.deleteTSFDcolumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService).clearColumnsForOrders(Mockito.anyString());
    }

}
