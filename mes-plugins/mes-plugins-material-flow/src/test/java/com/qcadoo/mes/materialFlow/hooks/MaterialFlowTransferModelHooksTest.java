package com.qcadoo.mes.materialFlow.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class MaterialFlowTransferModelHooksTest {

    private MaterialFlowTransferModelHooks materialFlowTransferModelHooks;

    @Mock
    private Entity transfer, transformation;

    @Mock
    private Entity stockAreasTo, stockAreasFrom, staff;

    @Mock
    private DataDefinition dd;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        materialFlowTransferModelHooks = new MaterialFlowTransferModelHooks();
    }

    @Test
    public void shouldCopyProductionDataFromBelongingTransformation() {
        // given
        given(transfer.getBelongsToField("transformationsProduction")).willReturn(transformation);
        given(transformation.getField("time")).willReturn("1234");
        given(transformation.getBelongsToField("stockAreasTo")).willReturn(stockAreasTo);
        given(transformation.getBelongsToField("stockAreasFrom")).willReturn(stockAreasFrom);
        given(transformation.getBelongsToField("staff")).willReturn(staff);

        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(dd, transfer);

        // then
        verify(transfer).setField("type", "Production");
        verify(transfer).setField("time", "1234");
        verify(transfer).setField("stockAreasTo", stockAreasTo);
        verify(transfer).setField("stockAreasFrom", stockAreasFrom);
        verify(transfer).setField("staff", staff);
    }

    @Test
    public void shouldCopyConsumptionDataFromBelongingTransformation() {
        // given
        given(transfer.getBelongsToField("transformationsConsumption")).willReturn(transformation);
        given(transformation.getField("time")).willReturn("1234");
        given(transformation.getBelongsToField("stockAreasTo")).willReturn(stockAreasTo);
        given(transformation.getBelongsToField("stockAreasFrom")).willReturn(stockAreasFrom);
        given(transformation.getBelongsToField("staff")).willReturn(staff);

        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(dd, transfer);

        // then
        verify(transfer).setField("type", "Consumption");
        verify(transfer).setField("time", "1234");
        verify(transfer).setField("stockAreasTo", stockAreasTo);
        verify(transfer).setField("stockAreasFrom", stockAreasFrom);
        verify(transfer).setField("staff", staff);
    }

    @Test
    public void shouldNotTriggerCopyingWhenSavingPlainTransfer() {
        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(dd, transfer);

        // then
        verify(transfer, never()).setField(anyString(), any());
    }
}
