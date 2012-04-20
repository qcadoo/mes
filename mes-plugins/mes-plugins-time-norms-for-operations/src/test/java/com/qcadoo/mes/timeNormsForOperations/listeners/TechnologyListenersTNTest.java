package com.qcadoo.mes.timeNormsForOperations.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class TechnologyListenersTNTest {

    private TechnologyListenersTN technologyListenersTN;

    @Mock
    private Entity technology;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState componentState;

    @Mock
    private FormComponent form;

    @Mock
    private Entity operComp1, operComp2, prod1, prod2, prod1Comp, prod2Comp;

    @Mock
    private NormService normService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyListenersTN = new TechnologyListenersTN();

        ReflectionTestUtils.setField(technologyListenersTN, "normService", normService);
    }

    @Test
    public void shouldReturnIfTheTechnologyIsntInDraftState() {
        // given
        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntity()).willReturn(technology);
        given(technology.getStringField("state")).willReturn("02accepted");

        // when
        technologyListenersTN.checkOperationOutputQuantities(view, componentState, null);

        // then
        verify(normService, never()).checkOperationOutputQuantities(technology);
        verify(form, never()).addMessage(Mockito.anyString(), Mockito.eq(MessageType.INFO), Mockito.eq(false));
    }

    @Ignore
    @Test
    public void shouldPassValidationErrorsToTheEntityForAcceptedTechnology() {
        // given
        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntity()).willReturn(technology);
        given(technology.getStringField("state")).willReturn("01draft");

        given(technology.getDataDefinition()).willReturn(dataDefinition);
        given(technology.getId()).willReturn(0L);
        given(dataDefinition.get(0L)).willReturn(technology);

        // given(normService.checkOperationOutputQuantities(technology)).willReturn(asList("err1", "err2"));

        // when
        technologyListenersTN.checkOperationOutputQuantities(view, componentState, null);

        // then
        verify(form).addMessage(Mockito.eq("err1"), Mockito.eq(MessageType.INFO), Mockito.eq(false));
        verify(form).addMessage(Mockito.eq("err2"), Mockito.eq(MessageType.INFO), Mockito.eq(false));
    }
}
