package com.qcadoo.mes.products;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.select.SelectComponentState;

public class QualityControlServiceTest {

    private QualityControlService qualityControlService;

    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        qualityControlService = new QualityControlService();
        setField(qualityControlService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldSetRequiredOnCommentForControlResult() {
        // given
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponentState comment = mock(FieldComponentState.class);
        FieldComponentState controlResult = mock(FieldComponentState.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("comment")).willReturn(comment);
        given(state.getComponentByReference("controlResult")).willReturn(controlResult);
        given(state.getComponentByReference("controlResult").getFieldValue()).willReturn("03objection");
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        qualityControlService.checkIfCommentIsRequiredBasedOnResult(state, Locale.ENGLISH);

        // then
        verify(comment).setRequired(true);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldSetRequiredOnCommentForDefects() {
        // given
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponentState comment = mock(FieldComponentState.class);
        FieldComponentState acceptedDefectsQuantity = mock(FieldComponentState.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("comment")).willReturn(comment);
        given(state.getComponentByReference("acceptedDefectsQuantity")).willReturn(acceptedDefectsQuantity);
        given(state.getComponentByReference("acceptedDefectsQuantity").getFieldValue()).willReturn(new BigDecimal("12"));
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        qualityControlService.checkIfCommentIsRequiredBasedOnDefects(state, Locale.ENGLISH);

        // then
        verify(comment).setRequired(true);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    @Ignore
    public void shouldSetRequiredOnCommentAfterSelectChange() {
        // given
        SelectComponentState resultType = mock(SelectComponentState.class);
        FieldComponentState comment = mock(FieldComponentState.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(resultType.getFieldValue()).willReturn("03objection");

        // when
        qualityControlService.checkQualityControlResult(state, resultType, new String[] {});

        // then
        verify(comment).setRequired(true);
    }

    @Test
    public void shouldSetErrorOnCommentFieldForResultType() {
        // given
        Entity entity = new DefaultEntity("plugins", "qualityForOrder");
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        entity.setField("controlResult", "03objection");
        entity.setField("comment", null);

        // when
        qualityControlService.checkIfCommentForResultIsReq(dataDefinition, entity);

        // then
        assertNotNull(entity.getErrors());
    }

    @Test
    public void shouldSetErrorOnCommentFieldForAcceptedDefectsQuantity() {
        // given
        Entity entity = new DefaultEntity("plugins", "qualityForOrder");
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        entity.setField("acceptedDefectsQuantity", new BigDecimal("0"));
        entity.setField("comment", null);

        // when
        qualityControlService.checkIfCommentForResultIsReq(dataDefinition, entity);

        // then
        assertNotNull(entity.getErrors());
    }

    @Test
    @Ignore
    public void shouldCloseQualityControl() {
        // given
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState state = mock(ViewDefinitionState.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponentState controlResult = mock(FieldComponentState.class);
        FieldComponentState closed = mock(FieldComponentState.class);
        FieldComponentState staff = mock(FieldComponentState.class);
        FieldComponentState date = mock(FieldComponentState.class);

        given(form.getFieldValue()).willReturn(7L);
        given(state.getComponentByReference("controlResult")).willReturn(controlResult);
        given(state.getComponentByReference("closed")).willReturn(closed);
        given(controlResult.getFieldValue()).willReturn("03objection");

        // when
        qualityControlService.closeQualityControl(state, form, new String[] { "qualityForOrder" });

        // then
        assertEquals(true, closed.getFieldValue());
    }
}
