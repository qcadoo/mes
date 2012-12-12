package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF.LOCATION;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class DeliveryHooksDTMFTest {

    private DeliveryHooksDTMF deliveryHooksDTMF;

    @Mock
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Mock
    private DataDefinition deliveryDD;

    @Mock
    private Entity delivery, location;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveryHooksDTMF = new DeliveryHooksDTMF();

        ReflectionTestUtils.setField(deliveryHooksDTMF, "materialFlowResourcesService", materialFlowResourcesService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfLocationIsWarehouse() {
        // given
        given(delivery.getBelongsToField(LOCATION)).willReturn(null);

        // when
        boolean result = deliveryHooksDTMF.checkIfLocationIsWarehouse(deliveryDD, delivery);

        // then
        Assert.assertTrue(result);

        verify(delivery, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfLocationIsWarehouse() {
        // given
        given(delivery.getBelongsToField(LOCATION)).willReturn(location);
        given(materialFlowResourcesService.isLocationIsWarehouse(location)).willReturn(false);

        // when
        boolean result = deliveryHooksDTMF.checkIfLocationIsWarehouse(deliveryDD, delivery);

        // then
        Assert.assertFalse(result);

        verify(delivery).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
