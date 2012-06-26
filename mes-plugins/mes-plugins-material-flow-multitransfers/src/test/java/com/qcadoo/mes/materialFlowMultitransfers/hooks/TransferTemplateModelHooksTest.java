package com.qcadoo.mes.materialFlowMultitransfers.hooks;

import static com.qcadoo.mes.materialFlowMultitransfers.constants.TransferTemplateFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlowMultitransfers.constants.TransferTemplateFields.LOCATION_TO;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class TransferTemplateModelHooksTest {

    private TransferTemplateModelValidators transferTemplateModelHooks;

    @Mock
    private DataDefinition transferTemplateDD;

    @Mock
    private Entity transferTemplate, locationFrom, locationTo;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transferTemplateModelHooks = new TransferTemplateModelValidators();

    }

    @Test
    public void shouldReturnFalseWhenCheckIfOneOfLocationsIsNotNullAndLocationsAreNull() {
        // given
        given(transferTemplate.getBelongsToField(LOCATION_FROM)).willReturn(null);
        given(transferTemplate.getBelongsToField(LOCATION_TO)).willReturn(null);

        // when
        boolean result = transferTemplateModelHooks.checkIfOneOfLocationsIsNotNull(transferTemplateDD, transferTemplate);

        // then
        Assert.assertFalse(result);

        verify(transferTemplate, Mockito.times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOneOfLocationsIsNotNullAndLocationsArentNull() {
        // given
        given(transferTemplate.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transferTemplate.getBelongsToField(LOCATION_TO)).willReturn(locationTo);

        // when
        boolean result = transferTemplateModelHooks.checkIfOneOfLocationsIsNotNull(transferTemplateDD, transferTemplate);

        // then
        Assert.assertTrue(result);

        verify(transferTemplate, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }
}
