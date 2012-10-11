package com.qcadoo.mes.deliveries.deliveriesColumnExtension;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class DeliveriesColumnFillerTest {

    private DeliveriesColumnFiller deliveriesColumnFiller;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesColumnFiller = new DeliveriesColumnFiller();
    }

}
