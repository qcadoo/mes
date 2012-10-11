package com.qcadoo.mes.deliveries.hooks;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class DeliveryHooksTest {

    private DeliveryHooks deliveryHooks;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity delivery;

    @Before
    public void init() {
        deliveryHooks = new DeliveryHooks();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldClearStateField() throws Exception {
        // then
        // when
        deliveryHooks.clearStateFieldOnCopy(dataDefinition, delivery);
        // then
        verify(delivery).setField("state", DeliveryStateStringValues.DRAFT);
    }
}
