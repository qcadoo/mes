package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferTemplateFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferTemplateFields.STOCK_AREAS_TO;
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

    private TransferTemplateModelHooks transferTemplateModelHooks;

    @Mock
    private DataDefinition transferTemplateDD;

    @Mock
    private Entity transferTemplate, stockAreasFrom, stockAreasTo;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transferTemplateModelHooks = new TransferTemplateModelHooks();

    }

    @Test
    public void shouldReturnFalseWhenCheckIfOneOfStockAreasIsNullAndStockAreasAreNull() {
        // given
        given(transferTemplate.getBelongsToField(STOCK_AREAS_FROM)).willReturn(null);
        given(transferTemplate.getBelongsToField(STOCK_AREAS_TO)).willReturn(null);

        // when
        boolean result = transferTemplateModelHooks.checkIfOneOfStockAreasIsNotNull(transferTemplateDD, transferTemplate);

        // then
        Assert.assertFalse(result);

        verify(transferTemplate, Mockito.times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOneOfStockAreasIsntAndStockAreasArentNull() {
        // given
        given(transferTemplate.getBelongsToField(STOCK_AREAS_FROM)).willReturn(stockAreasFrom);
        given(transferTemplate.getBelongsToField(STOCK_AREAS_TO)).willReturn(stockAreasTo);

        // when
        boolean result = transferTemplateModelHooks.checkIfOneOfStockAreasIsNotNull(transferTemplateDD, transferTemplate);

        // then
        Assert.assertTrue(result);

        verify(transferTemplate, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }
}
