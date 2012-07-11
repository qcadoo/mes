package com.qcadoo.mes.assignmentToShift.hooks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

public class AssignmentToShiftDetailsHooksTest {

    private AssignmentToShiftDetailsHooks detailsHooks;

    @Mock
    private FieldComponent stateField;

    @Mock
    private SearchCriteriaBuilder builder;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private GridComponent grid;

    @Before
    public void init() {
        detailsHooks = new AssignmentToShiftDetailsHooks();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldDisabledGridWhenStateIsAccepted() throws Exception {
        // given
        String state = "02accepted";
        when(view.getComponentByReference("state")).thenReturn(stateField);
        when(stateField.getFieldValue()).thenReturn(state);
        when(view.getComponentByReference("staffAssignmentToShifts")).thenReturn(grid);
        // when
        detailsHooks.disabledGridWhenStateIsAcceptedOrCorrected(view);
        // then
        verify(grid).setEditable(false);
    }

    @Test
    public void shouldDisabledGridWhenStateIsCorrected() throws Exception {
        // given
        String state = "04corrected";
        when(view.getComponentByReference("state")).thenReturn(stateField);
        when(stateField.getFieldValue()).thenReturn(state);
        when(view.getComponentByReference("staffAssignmentToShifts")).thenReturn(grid);
        // when
        detailsHooks.disabledGridWhenStateIsAcceptedOrCorrected(view);
        // then
        verify(grid).setEditable(false);
    }

    @Test
    public void shouldEnabledGridWhenStateIsDraft() throws Exception {
        // given
        String state = "01draft";
        when(view.getComponentByReference("state")).thenReturn(stateField);
        when(stateField.getFieldValue()).thenReturn(state);
        when(view.getComponentByReference("staffAssignmentToShifts")).thenReturn(grid);
        // when
        detailsHooks.disabledGridWhenStateIsAcceptedOrCorrected(view);
        // then
        verify(grid).setEditable(true);
    }

}
