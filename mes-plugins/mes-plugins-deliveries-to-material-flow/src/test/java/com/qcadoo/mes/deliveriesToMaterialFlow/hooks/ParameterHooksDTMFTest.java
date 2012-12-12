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

public class ParameterHooksDTMFTest {

    private ParameterHooksDTMF parameterHooksDTMF;

    @Mock
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private Entity parameter, location;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parameterHooksDTMF = new ParameterHooksDTMF();

        ReflectionTestUtils.setField(parameterHooksDTMF, "materialFlowResourcesService", materialFlowResourcesService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfLocationIsWarehouse() {
        // given
        given(parameter.getBelongsToField(LOCATION)).willReturn(null);

        // when
        boolean result = parameterHooksDTMF.checkIfLocationIsWarehouse(parameterDD, parameter);

        // then
        Assert.assertTrue(result);

        verify(parameter, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfLocationIsWarehouse() {
        // given
        given(parameter.getBelongsToField(LOCATION)).willReturn(location);
        given(materialFlowResourcesService.isLocationIsWarehouse(location)).willReturn(false);

        // when
        boolean result = parameterHooksDTMF.checkIfLocationIsWarehouse(parameterDD, parameter);

        // then
        Assert.assertFalse(result);

        verify(parameter).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
