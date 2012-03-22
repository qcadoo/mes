package com.qcadoo.mes.productionCounting.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class ProductionRecordViewServiceTest {

    private ProductionRecordViewService productionRecordViewService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent state, orderOperationComponent;

    @Mock
    private ComponentState lookup, dummyComponent, borderLayoutTime, borderLayoutPiecework;

    @Mock
    private Entity formEntity, order;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordViewService = new ProductionRecordViewService();

        ReflectionTestUtils.setField(productionRecordViewService, "dataDefinitionService", dataDefinitionService);

        given(view.getComponentByReference(Mockito.anyString())).willReturn(dummyComponent);

        given(view.getComponentByReference("orderOperationComponent")).willReturn(orderOperationComponent);
        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference("state")).willReturn(state);
        given(view.getComponentByReference("order")).willReturn(lookup);
        given(lookup.getFieldValue()).willReturn(2L);

        given(form.getEntity()).willReturn(formEntity);
        given(formEntity.getDataDefinition()).willReturn(dataDefinition);
        given(form.getEntityId()).willReturn(1L);
        given(dataDefinition.get(1L)).willReturn(formEntity);
        given(dataDefinition.get(2L)).willReturn(order);
        given(dataDefinitionService.get("orders", "order")).willReturn(dataDefinition);
    }

    @Test
    public void shouldDisableTimePanelIfTimeRegistrationIsDisabled() {
        // given
        given(order.getBooleanField("registerProductionTime")).willReturn(false);
        given(view.getComponentByReference("borderLayoutTime")).willReturn(borderLayoutTime);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutTime).setVisible(false);
    }

    @Test
    public void shouldDisablePieceworkPanelIfPieceworkRegistrationIsDisabled() {
        // given
        given(order.getBooleanField("registerPiecework")).willReturn(false);
        given(view.getComponentByReference("borderLayoutPiecework")).willReturn(borderLayoutPiecework);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutPiecework).setVisible(false);
    }

    @Test
    public void shouldEnableTimePanelIfProductionRecordingTypeIsntSetToSimpleAndRegisterTimeIsSetToTrue() {
        // given
        given(order.getStringField("typeOfProductionRecording")).willReturn("03forEach");
        given(order.getBooleanField("registerProductionTime")).willReturn(false);
        given(view.getComponentByReference("borderLayoutTime")).willReturn(borderLayoutTime);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutTime).setVisible(false);
    }

    @Test
    public void shouldEnablePieceworkPanelIfProductionRecordingTypeIsForOperationAndRegisterPiecworkIsSetToTrue() {
        // given
        given(order.getStringField("typeOfProductionRecording")).willReturn("03forEach");
        given(order.getBooleanField("registerPiecework")).willReturn(true);
        given(view.getComponentByReference("borderLayoutPiecework")).willReturn(borderLayoutPiecework);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutPiecework).setVisible(true);
    }
}
