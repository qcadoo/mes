package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.START_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STATE;
import static com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState.DRAFT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class AssignmentToShiftHooksTest {

    private AssignmentToShiftHooks hooks;

    @Autowired
    private AssignmentToShiftStateChangeDescriber describer;

    @Mock
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Mock
    private DataDefinition assignmentToShiftDD;

    @Mock
    private Entity assignmentToShift, shift;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private Date startDate;

    @Before
    public void init() {
        hooks = new AssignmentToShiftHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "stateChangeEntityBuilder", stateChangeEntityBuilder);

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public void shouldSetInitialState() {
        // given

        // when
        hooks.setInitialState(assignmentToShiftDD, assignmentToShift);

        // then
        verify(stateChangeEntityBuilder).buildInitial(describer, assignmentToShift, DRAFT);
    }

    @Test
    public void shouldClearFieldStateOnCopy() {
        // given

        // when
        hooks.clearState(assignmentToShiftDD, assignmentToShift);

        // then
        verify(assignmentToShift).setField(STATE, DRAFT.getStringValue());
    }

    @Test
    public void shouldReturnTrueWhenCheckUniqueEntityIfEntityIsntSaved() {
        // given
        given(assignmentToShift.getId()).willReturn(null);

        given(assignmentToShift.getBelongsToField(SHIFT)).willReturn(null);
        given(assignmentToShift.getField(START_DATE)).willReturn(null);

        given(assignmentToShiftDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        boolean result = hooks.checkUniqueEntity(assignmentToShiftDD, assignmentToShift);

        // then
        Assert.assertTrue(result);

        verify(assignmentToShift, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckUniqueEntityIfEntityIsSaved() {
        // given
        given(assignmentToShift.getId()).willReturn(1L);

        given(assignmentToShift.getBelongsToField(SHIFT)).willReturn(null);
        given(assignmentToShift.getField(START_DATE)).willReturn(null);

        given(assignmentToShiftDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        boolean result = hooks.checkUniqueEntity(assignmentToShiftDD, assignmentToShift);

        // then
        Assert.assertTrue(result);

        verify(assignmentToShift, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckUniqueEntityIfEntityIsntSaved() {
        // given
        given(assignmentToShift.getId()).willReturn(null);

        given(assignmentToShift.getBelongsToField(SHIFT)).willReturn(shift);
        given(assignmentToShift.getField(START_DATE)).willReturn(startDate);

        given(assignmentToShiftDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(assignmentToShift);

        // when
        boolean result = hooks.checkUniqueEntity(assignmentToShiftDD, assignmentToShift);

        // then
        Assert.assertFalse(result);

        verify(assignmentToShift, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckUniqueEntityIfEntityIsSaved() {
        // given
        given(assignmentToShift.getId()).willReturn(1L);

        given(assignmentToShift.getBelongsToField(SHIFT)).willReturn(shift);
        given(assignmentToShift.getField(START_DATE)).willReturn(startDate);

        given(assignmentToShiftDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(assignmentToShift);

        // when
        boolean result = hooks.checkUniqueEntity(assignmentToShiftDD, assignmentToShift);

        // then
        Assert.assertFalse(result);

        verify(assignmentToShift, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
