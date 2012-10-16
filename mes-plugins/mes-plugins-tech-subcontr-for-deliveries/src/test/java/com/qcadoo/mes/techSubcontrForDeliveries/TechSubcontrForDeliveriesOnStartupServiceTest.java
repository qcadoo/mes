package com.qcadoo.mes.techSubcontrForDeliveries;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.techSubcontrForDeliveries.deliveriesColumnExtension.TSFDcolumnLoader;

public class TechSubcontrForDeliveriesOnStartupServiceTest {

    private TechSubcontrForDeliveriesOnStartupService techSubcontrForDeliveriesOnStartupService;

    @Mock
    private TSFDcolumnLoader tSFDcolumnLoader;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        techSubcontrForDeliveriesOnStartupService = new TechSubcontrForDeliveriesOnStartupService();

        ReflectionTestUtils.setField(techSubcontrForDeliveriesOnStartupService, "tSFDcolumnLoader", tSFDcolumnLoader);
    }

    @Test
    public void shouldMultiTenantEnable() {
        // given

        // when
        techSubcontrForDeliveriesOnStartupService.multiTenantEnable();

        // then
        verify(tSFDcolumnLoader).addTSFDcolumnsForDeliveries();
        verify(tSFDcolumnLoader).addTSFDcolumnsForOrders();
    }

    @Test
    public void shouldMultiTenantDisable() {
        // given

        // when
        techSubcontrForDeliveriesOnStartupService.multiTenantDisable();

        // then
        verify(tSFDcolumnLoader).deleteTSFDcolumnsForDeliveries();
        verify(tSFDcolumnLoader).deleteTSFDcolumnsForOrders();
    }

}
