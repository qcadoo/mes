/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFields.HAS_CORRECTIONS;
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

    private TechInstOperCompHooksPPS hooksPPS;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity;

    @Mock
    private EntityList progressForDays;

    @Before
    public void init() {
        hooksPPS = new TechInstOperCompHooksPPS();
        MockitoAnnotations.initMocks(this);
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
    public void shouldReturnTrueWhenProgressForDayHMIsEmpty() throws Exception {
        // given
        when(entity.getHasManyField("progressForDays")).thenReturn(progressForDays);
        when(progressForDays.isEmpty()).thenReturn(true);
        // when
        boolean result = hooksPPS.checkGrowingNumberOfDays(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseAndEntityHasErrorWhenDaysAreNotOrderDesc() throws Exception {
        // given
        Entity pdf1 = mock(Entity.class);
        Entity pfd2 = mock(Entity.class);
        List<Entity> pfds = asList(pdf1, pfd2);
        EntityList progressForDays = mockEntityList(pfds);
        Integer day1 = 11;
        Integer day2 = 10;
        when(entity.getHasManyField("progressForDays")).thenReturn(progressForDays);
        when(progressForDays.get(0)).thenReturn(pdf1);
        when(progressForDays.get(1)).thenReturn(pfd2);
        when(pdf1.getField("day")).thenReturn(day1);
        when(pfd2.getField("day")).thenReturn(day2);
        // when
        boolean result = hooksPPS.checkGrowingNumberOfDays(dataDefinition, entity);
        // then
        Assert.assertFalse(result);
        Mockito.verify(entity).addGlobalError("productionPerShift.progressForDay.daysIsNotInAscendingOrder", day2.toString());
    }

    @Test
    public void shouldReturnTrueWhenDaysAreOrderDesc() throws Exception {
        // given
        Entity pdf1 = mock(Entity.class);
        Entity pfd2 = mock(Entity.class);
        List<Entity> pfds = asList(pdf1, pfd2);
        EntityList progressForDays = mockEntityList(pfds);
        Integer day1 = 10;
        Integer day2 = 11;
        when(entity.getHasManyField("progressForDays")).thenReturn(progressForDays);
        when(progressForDays.get(0)).thenReturn(pdf1);
        when(progressForDays.get(1)).thenReturn(pfd2);
        when(pdf1.getField("day")).thenReturn(day1);
        when(pfd2.getField("day")).thenReturn(day2);
        // when
        boolean result = hooksPPS.checkGrowingNumberOfDays(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenPFDIsEmpty() throws Exception {
        // given
        progressForDays = mockEntityList(Collections.<Entity> emptyList());
        when(entity.getHasManyField("progressForDays")).thenReturn(progressForDays);
        when(progressForDays.isEmpty()).thenReturn(true);
        // when
        boolean result = hooksPPS.checkShiftsIfWorks(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenEntityHasCorrentionAndPfdIsCorrected() throws Exception {
        // given
        Entity pdf1 = mock(Entity.class);
        List<Entity> pfds = asList(pdf1);
        Integer day = Integer.valueOf(1);
        EntityList progressForDays = mockEntityList(pfds);
        when(entity.getHasManyField("progressForDays")).thenReturn(progressForDays);
        when(progressForDays.get(0)).thenReturn(pdf1);
        when(pdf1.getField("day")).thenReturn(day);
        when(pdf1.getBooleanField("corrected")).thenReturn(true);
        when(entity.getBooleanField(HAS_CORRECTIONS)).thenReturn(false);
        // when
        boolean result = hooksPPS.checkShiftsIfWorks(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

}
