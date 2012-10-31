/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
import java.util.List;

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
import com.qcadoo.model.api.search.SearchResult;

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
    private SearchResult searchResult;

    @Mock
    private List<Entity> assignmentToShifts;

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
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(assignmentToShifts);
        given(assignmentToShifts.isEmpty()).willReturn(true);

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
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(assignmentToShifts);
        given(assignmentToShifts.isEmpty()).willReturn(true);

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
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(assignmentToShifts);
        given(assignmentToShifts.isEmpty()).willReturn(false);

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
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(assignmentToShifts);
        given(assignmentToShifts.isEmpty()).willReturn(false);

        // when
        boolean result = hooks.checkUniqueEntity(assignmentToShiftDD, assignmentToShift);

        // then
        Assert.assertFalse(result);

        verify(assignmentToShift, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
