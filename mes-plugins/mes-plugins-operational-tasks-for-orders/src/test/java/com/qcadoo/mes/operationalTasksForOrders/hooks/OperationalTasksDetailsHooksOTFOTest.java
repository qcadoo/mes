package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class OperationalTasksDetailsHooksOTFOTest {

    private OperationalTasksDetailsHooksOTFO detailsHooksOTFO;

    @Mock
    private ViewDefinitionState viewDefinitionState;

    @Mock
    private FieldComponent typeField, nameField, descriptionField, orderField, productionLineField, tiocField;

    @Before
    public void init() {
        detailsHooksOTFO = new OperationalTasksDetailsHooksOTFO();
        MockitoAnnotations.initMocks(this);

        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.TYPE_TASK)).thenReturn(typeField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.NAME)).thenReturn(nameField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.DESCRIPTION))
                .thenReturn(descriptionField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksOTFRFields.ORDER)).thenReturn(orderField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.PRODUCTION_LINE)).thenReturn(
                productionLineField);
        Mockito.when(
                viewDefinitionState.getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT))
                .thenReturn(tiocField);
    }

    @Test
    public void shouldDisabledFieldWhenTypeForOrderIsSelected() throws Exception {
        // given
        when(typeField.getFieldValue()).thenReturn("02executionOperationInOrder");
        // when
        detailsHooksOTFO.disabledFieldWhenOrderTypeIsSelected(viewDefinitionState);
        // then

        Mockito.verify(nameField).setEnabled(false);
        Mockito.verify(descriptionField).setEnabled(false);
        Mockito.verify(productionLineField).setEnabled(false);
        Mockito.verify(orderField).setEnabled(true);
        Mockito.verify(tiocField).setEnabled(true);
    }

    @Test
    public void shouldEnabledFieldWhenTypeOtherCaseIsSelected() throws Exception {
        // given
        when(typeField.getFieldValue()).thenReturn("01otherCase");
        // when
        detailsHooksOTFO.disabledFieldWhenOrderTypeIsSelected(viewDefinitionState);
        // then

        Mockito.verify(nameField).setEnabled(true);
        Mockito.verify(descriptionField).setEnabled(true);
        Mockito.verify(productionLineField).setEnabled(true);
        Mockito.verify(orderField).setEnabled(false);
        Mockito.verify(tiocField).setEnabled(false);
    }
}
