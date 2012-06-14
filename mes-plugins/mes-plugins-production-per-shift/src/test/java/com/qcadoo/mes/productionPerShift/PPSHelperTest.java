package com.qcadoo.mes.productionPerShift;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class PPSHelperTest {

    private PPSHelper helper;

    @Mock
    private ViewDefinitionState viewState;

    @Mock
    private ComponentState lookup;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity entity;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private FieldComponent field;

    @Before
    public void init() {
        helper = new PPSHelper();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(helper, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldReturnNullWhenTiocIdNull() throws Exception {
        // given
        when(viewState.getComponentByReference("productionPerShiftOperation")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(null);

        // when
        Entity result = helper.getTiocFromOperationLookup(viewState);
        // then
        Assert.assertNull(result);
    }

    @Test
    public void shouldReturnTiocFromDB() throws Exception {
        // given
        Long tiocId = 1L;
        when(viewState.getComponentByReference("productionPerShiftOperation")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(tiocId);
        when(dataDefinitionService.get("technologies", "technologyInstanceOperationComponent")).thenReturn(dataDefinition);
        when(dataDefinition.get(tiocId)).thenReturn(entity);
        // when
        Entity result = helper.getTiocFromOperationLookup(viewState);
        // then
        Assert.assertEquals(entity, result);
    }

    @Test
    public void shouldReturnNullWhenOrderIdNull() throws Exception {
        // given
        when(viewState.getComponentByReference("order")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(null);

        // when
        Entity result = helper.getOrderFromLookup(viewState);
        // then
        Assert.assertNull(result);
    }

    @Test
    public void shouldReturnOrderFromDB() throws Exception {
        // given
        Long orderId = 1L;
        when(viewState.getComponentByReference("order")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(orderId);
        when(dataDefinitionService.get("orders", "order")).thenReturn(dataDefinition);
        when(dataDefinition.get(orderId)).thenReturn(entity);

        // when
        Entity result = helper.getOrderFromLookup(viewState);
        // then
        Assert.assertEquals(entity, result);
    }

    @Test
    public void shouldReturnNullWhenShiftIdNull() throws Exception {
        // given
        when(field.getFieldValue()).thenReturn(null);

        // when
        Entity result = helper.getShiftFromLookup(field);
        // then
        Assert.assertNull(result);
    }

    @Test
    public void shouldReturnShiftFromDB() throws Exception {
        // given
        Long shiftId = 1L;
        when(field.getFieldValue()).thenReturn(shiftId);
        when(dataDefinitionService.get("basic", "shift")).thenReturn(dataDefinition);
        when(dataDefinition.get(shiftId)).thenReturn(entity);

        // when
        Entity result = helper.getShiftFromLookup(field);
        // then
        Assert.assertEquals(entity, result);
    }

    @Test
    public void shouldReturnFalseWhenProgressTypeIsPlanned() throws Exception {
        // given
        when(viewState.getComponentByReference("plannedProgressType")).thenReturn(field);
        when(field.getFieldValue()).thenReturn("01planned");
        // when
        boolean result = helper.shouldHasCorrections(viewState);
        // then
        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenProgressTypeIsPlanned() throws Exception {
        // given
        when(viewState.getComponentByReference("plannedProgressType")).thenReturn(field);
        when(field.getFieldValue()).thenReturn("02corrected");
        // when
        boolean result = helper.shouldHasCorrections(viewState);
        // then
        Assert.assertTrue(result);
    }

}