/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState.DRAFT;
import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.dataProviders.AssignmentToShiftCriteria;
import com.qcadoo.mes.assignmentToShift.dataProviders.AssignmentToShiftDataProvider;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsFactory;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class AssignmentToShiftHooksTest {

    private static final LocalDate START_DATE = LocalDate.now();

    private AssignmentToShiftHooks assignmentToShiftHooks;

    @Mock
    private AssignmentToShiftStateChangeDescriber describer;

    @Mock
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Mock
    private AssignmentToShiftDataProvider assignmentToShiftDataProvider;

    @Mock
    private ShiftsFactory shiftsFactory;

    @Mock
    private Shift shiftPojo;

    private Entity assignmentToShift, shiftEntity;

    @Before
    public void init() {
        assignmentToShiftHooks = new AssignmentToShiftHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(assignmentToShiftHooks, "stateChangeEntityBuilder", stateChangeEntityBuilder);
        ReflectionTestUtils.setField(assignmentToShiftHooks, "describer", describer);
        ReflectionTestUtils.setField(assignmentToShiftHooks, "assignmentToShiftDataProvider", assignmentToShiftDataProvider);
        ReflectionTestUtils.setField(assignmentToShiftHooks, "shiftsFactory", shiftsFactory);

        assignmentToShift = mockEntity(mock(DataDefinition.class));
        shiftEntity = mockEntity(mock(DataDefinition.class));
        given(shiftEntity.copy()).willReturn(shiftEntity);
        given(shiftsFactory.buildFrom(any(Entity.class))).willReturn(shiftPojo);

        stubBelongsToField(assignmentToShift, AssignmentToShiftFields.SHIFT, shiftEntity);
        stubDateField(assignmentToShift, AssignmentToShiftFields.START_DATE, START_DATE.toDate());

        stubFind(null);
        stubFindAll(ImmutableList.<Entity> of());
    }

    private void stubFindAll(final List<Entity> results) {
        given(
                assignmentToShiftDataProvider.findAll(any(AssignmentToShiftCriteria.class), any(Optional.class),
                        any(Optional.class))).willReturn(Lists.newArrayList(results));
    }

    private void stubFind(final Entity result) {
        given(assignmentToShiftDataProvider.find(any(AssignmentToShiftCriteria.class), any(Optional.class))).willReturn(
                Optional.fromNullable(result));
    }

    @Test
    public void shouldSetInitialState() {
        // when
        assignmentToShiftHooks.setInitialState(assignmentToShift);

        // then
        verify(stateChangeEntityBuilder).buildInitial(describer, assignmentToShift, DRAFT);
    }

    @Test
    public void shouldReturnTrueWhenCheckUniqueEntityIfEntityIsNotSaved() {
        // when
        boolean result = assignmentToShiftHooks.checkUniqueEntity(assignmentToShift);

        // then
        Assert.assertTrue(result);
        verify(assignmentToShift, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckUniqueEntityIfEntityIsntSaved() {
        // given
        stubFind(mockEntity());

        // when
        boolean result = assignmentToShiftHooks.checkUniqueEntity(assignmentToShift);

        // then
        Assert.assertFalse(result);
        verify(assignmentToShift, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckUniqueEntityIfEntityIsSaved() {
        // given
        stubFind(mockEntity());

        // when
        boolean result = assignmentToShiftHooks.checkUniqueEntity(assignmentToShift);

        // then
        Assert.assertFalse(result);
        verify(assignmentToShift, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public final void shouldPickUpNextDay() {
        // given
        Entity startProjection1 = mockStartDateProjection(START_DATE.plusDays(1));
        Entity startProjection2 = mockStartDateProjection(START_DATE.plusDays(2));
        Entity startProjection3 = mockStartDateProjection(START_DATE.plusDays(3));
        stubFindAll(Lists.newArrayList(startProjection1, startProjection2, startProjection3));

        given(shiftPojo.worksAt(START_DATE)).willReturn(true);
        given(shiftPojo.worksAt(START_DATE.plusDays(1))).willReturn(false);
        given(shiftPojo.worksAt(START_DATE.plusDays(2))).willReturn(true);
        given(shiftPojo.worksAt(START_DATE.plusDays(3))).willReturn(false);
        given(shiftPojo.worksAt(START_DATE.plusDays(4))).willReturn(false);
        given(shiftPojo.worksAt(START_DATE.plusDays(5))).willReturn(true);

        // when
        assignmentToShiftHooks.setNextDay(assignmentToShift);

        // then
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(assignmentToShift).setField(eq(AssignmentToShiftFields.START_DATE), dateCaptor.capture());
        Date pickedUpNextStartDate = dateCaptor.getValue();
        Assert.assertTrue(START_DATE.plusDays(5).toDate().compareTo(pickedUpNextStartDate) == 0);
    }

    @Test
    public final void shouldPickUpNextDayIfThereIsNoOccupiedDates() {
        // given
        given(shiftPojo.worksAt(START_DATE)).willReturn(true);
        given(shiftPojo.worksAt(START_DATE.plusDays(1))).willReturn(false);
        given(shiftPojo.worksAt(START_DATE.plusDays(2))).willReturn(true);
        given(shiftPojo.worksAt(START_DATE.plusDays(3))).willReturn(false);
        given(shiftPojo.worksAt(START_DATE.plusDays(4))).willReturn(false);
        given(shiftPojo.worksAt(START_DATE.plusDays(5))).willReturn(true);

        // when
        assignmentToShiftHooks.setNextDay(assignmentToShift);

        // then
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(assignmentToShift).setField(eq(AssignmentToShiftFields.START_DATE), dateCaptor.capture());
        Date pickedUpNextStartDate = dateCaptor.getValue();
        Assert.assertTrue(START_DATE.plusDays(2).toDate().compareTo(pickedUpNextStartDate) == 0);
    }

    @Test
    public final void shouldNotPickUpAnyDateIfShiftNeverWork() {
        // given
        Entity startProjection1 = mockStartDateProjection(START_DATE.plusDays(1));
        Entity startProjection2 = mockStartDateProjection(START_DATE.plusDays(2));
        Entity startProjection3 = mockStartDateProjection(START_DATE.plusDays(3));
        stubFindAll(Lists.newArrayList(startProjection1, startProjection2, startProjection3));

        // when
        assignmentToShiftHooks.setNextDay(assignmentToShift);

        // then
        verify(assignmentToShift).setField(eq(AssignmentToShiftFields.START_DATE), refEq(null));
    }

    private Entity mockStartDateProjection(final LocalDate localDate) {
        Entity projection = mockEntity();
        stubDateField(projection, AssignmentToShiftFields.START_DATE, localDate.toDate());
        return projection;
    }

}
