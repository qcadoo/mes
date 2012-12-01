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
package com.qcadoo.mes.productionPerShift.validators;

import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.HAS_CORRECTIONS;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.PROGRESS_FOR_DAYS;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableList;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class TechInstOperCompHooksPPSTest {

    private TechInstOperCompHooksPPS techInstOperCompHooksPPS;

    @Mock
    private DataDefinition technologyInstanceOperationComponentDD;

    @Mock
    private Entity technologyInstanceOperationComponent;

    @Mock
    private EntityList progressForDays;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        techInstOperCompHooksPPS = new TechInstOperCompHooksPPS();
    }

    public EntityList mockEntityList(final List<Entity> entities) {
        final EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });
        given(entityList.isEmpty()).willReturn(entities.isEmpty());

        return entityList;
    }

    @Test
    public void shouldReturnTrueWhenProgressForDayHMIsEmpty() {
        // given
        when(technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS)).thenReturn(progressForDays);
        when(progressForDays.isEmpty()).thenReturn(true);

        // when
        boolean result = techInstOperCompHooksPPS.checkGrowingNumberOfDays(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseAndEntityHasErrorWhenDaysAreNotOrderDesc() {
        // given
        String day1 = "11";
        String day2 = "10";

        Entity pfd1 = mock(Entity.class);
        Entity pfd2 = mock(Entity.class);
        List<Entity> pfds = asList(pfd1, pfd2);
        EntityList progressForDays = mockEntityList(pfds);

        when(technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS)).thenReturn(progressForDays);
        when(progressForDays.get(0)).thenReturn(pfd1);
        when(progressForDays.get(1)).thenReturn(pfd2);
        when(pfd1.getStringField(DAY)).thenReturn(day1);
        when(pfd2.getStringField(DAY)).thenReturn(day2);

        // when
        boolean result = techInstOperCompHooksPPS.checkGrowingNumberOfDays(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);

        // then
        Assert.assertFalse(result);
        Mockito.verify(technologyInstanceOperationComponent).addGlobalError(
                "productionPerShift.progressForDay.daysAreNotInAscendingOrder", day2.toString());
    }

    @Test
    public void shouldReturnTrueWhenDaysAreOrderDesc() {
        // given
        String day1 = "10";
        String day2 = "11";

        Entity pfd1 = mock(Entity.class);
        Entity pfd2 = mock(Entity.class);
        List<Entity> pfds = asList(pfd1, pfd2);
        EntityList progressForDays = mockEntityList(pfds);

        when(technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS)).thenReturn(progressForDays);
        when(progressForDays.get(0)).thenReturn(pfd1);
        when(progressForDays.get(1)).thenReturn(pfd2);
        when(pfd1.getField(DAY)).thenReturn(day1);
        when(pfd2.getField(DAY)).thenReturn(day2);

        // when
        boolean result = techInstOperCompHooksPPS.checkGrowingNumberOfDays(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenPFDIsEmpty() {
        // given
        progressForDays = mockEntityList(Collections.<Entity> emptyList());
        when(technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS)).thenReturn(progressForDays);
        when(progressForDays.isEmpty()).thenReturn(true);

        // when
        boolean result = techInstOperCompHooksPPS.checkShiftsIfWorks(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenEntityHasCorrentionAndPfdIsCorrected() {
        // given
        String day = "1";

        Entity pfd1 = mock(Entity.class);
        List<Entity> pfds = asList(pfd1);

        EntityList progressForDays = mockEntityList(pfds);
        when(technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS)).thenReturn(progressForDays);
        when(progressForDays.get(0)).thenReturn(pfd1);
        when(pfd1.getField(DAY)).thenReturn(day);
        when(pfd1.getBooleanField(CORRECTED)).thenReturn(true);
        when(technologyInstanceOperationComponent.getBooleanField(HAS_CORRECTIONS)).thenReturn(false);

        // when
        boolean result = techInstOperCompHooksPPS.checkShiftsIfWorks(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);

        // then
        Assert.assertTrue(result);
    }

}
