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
package com.qcadoo.mes.productionPerShift.validators;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
import com.google.common.collect.Lists;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.TechnologyOperationComponentFieldsPPS;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class TechnologyOperationComponentValidatorsPPSTest {

    private TechnologyOperationComponentValidatorsPPS technologyOperationComponentValidatorsPPS;

    @Mock
    private DataDefinition technologyOperationComponentDD;

    @Mock
    private Entity technologyOperationComponent;

    @Mock
    private EntityList progressForDays;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyOperationComponentValidatorsPPS = new TechnologyOperationComponentValidatorsPPS();
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
        given(technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(
                progressForDays);
        given(progressForDays.isEmpty()).willReturn(true);

        // when
        boolean result = technologyOperationComponentValidatorsPPS.checkGrowingNumberOfDays(technologyOperationComponentDD,
                technologyOperationComponent);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseAndEntityHasErrorWhenDaysAreNotOrderDesc() {
        // given
        Long day1 = 11L;
        Long day2 = 10L;

        Entity progressForDay1 = mock(Entity.class);
        Entity progressForDay2 = mock(Entity.class);
        EntityList progressForDays = mockEntityList(Lists.newArrayList(progressForDay1, progressForDay2));

        given(technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(
                progressForDays);
        given(progressForDays.get(0)).willReturn(progressForDay1);
        given(progressForDays.get(1)).willReturn(progressForDay2);
        given(progressForDay1.getField(ProgressForDayFields.DAY)).willReturn(day1);
        given(progressForDay2.getField(ProgressForDayFields.DAY)).willReturn(day2);

        // when
        boolean result = technologyOperationComponentValidatorsPPS.checkGrowingNumberOfDays(technologyOperationComponentDD,
                technologyOperationComponent);

        // then
        Assert.assertFalse(result);
        Mockito.verify(technologyOperationComponent).addGlobalError(
                "productionPerShift.progressForDay.daysAreNotInAscendingOrder", day2.toString());
    }

    @Test
    public void shouldReturnTrueWhenDaysAreOrderDesc() {
        // given
        Long day1 = 10L;
        Long day2 = 11L;

        Entity progressForDay1 = mock(Entity.class);
        Entity progressForDay2 = mock(Entity.class);
        EntityList progressForDays = mockEntityList(Lists.newArrayList(progressForDay1, progressForDay2));

        given(technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(
                progressForDays);
        given(progressForDays.get(0)).willReturn(progressForDay1);
        given(progressForDays.get(1)).willReturn(progressForDay2);
        given(progressForDay1.getField(ProgressForDayFields.DAY)).willReturn(day1);
        given(progressForDay2.getField(ProgressForDayFields.DAY)).willReturn(day2);

        // when
        boolean result = technologyOperationComponentValidatorsPPS.checkGrowingNumberOfDays(technologyOperationComponentDD,
                technologyOperationComponent);

        // then
        Assert.assertTrue(result);
    }

}
