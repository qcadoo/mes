/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basic;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class ShiftsServiceTest {

    private ShiftsServiceImpl shiftsService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, shift, exception;

    List<Entity> shifts = new ArrayList<Entity>();

    List<Entity> exceptions;

    @org.junit.Before
    public void init() {
        shiftsService = new ShiftsServiceImpl();
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(shiftsService, "dataDefinitionService", dataDefinitionService);
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void sholudValidateShiftTimetableException() throws Exception {
        // given
        Date fromDate = Mockito.mock(Date.class);
        Date toDate = Mockito.mock(Date.class);
        when(entity.getField("fromDate")).thenReturn(fromDate);
        when(entity.getField("toDate")).thenReturn(toDate);

        when(fromDate.compareTo(toDate)).thenReturn(0);
        // when
        boolean result = shiftsService.validateShiftTimetableException(dataDefinition, entity);
        // then

        assertEquals(result, true);
    }

    @Test
    public void shouldValidateShiftHoursField() throws Exception {
        // given
        String hours = "07:00-15:00";
        when(entity.getField("mondayWorking")).thenReturn(true);
        when(entity.getStringField("mondayHours")).thenReturn(hours);
        when(entity.getField("tuesdayWorking")).thenReturn(true);
        when(entity.getStringField("tuesdayHours")).thenReturn(hours);
        when(entity.getField("wensdayWorking")).thenReturn(true);
        when(entity.getStringField("wensdayHours")).thenReturn(hours);
        when(entity.getField("thursdayWorking")).thenReturn(true);
        when(entity.getStringField("thursdayHours")).thenReturn(hours);
        when(entity.getField("fridayWorking")).thenReturn(true);
        when(entity.getStringField("fridayHours")).thenReturn(hours);
        when(entity.getField("saturdayWorking")).thenReturn(true);
        when(entity.getStringField("saturdayHours")).thenReturn(hours);
        when(entity.getField("sundayWorking")).thenReturn(true);
        when(entity.getStringField("sundayHours")).thenReturn(hours);
        // when
        shiftsService.validateShiftHoursField(dataDefinition, entity);
        // then
    }

    @Test
    public void shouldReturnTrueWhenDayWorkingIsNotActive() throws Exception {
        // given
        when(entity.getField("mondayWorking")).thenReturn(false);
        when(entity.getField("tuesdayWorking")).thenReturn(false);
        when(entity.getField("wensdayWorking")).thenReturn(false);
        when(entity.getField("thursdayWorking")).thenReturn(false);
        when(entity.getField("fridayWorking")).thenReturn(false);
        when(entity.getField("saturdayWorking")).thenReturn(false);
        when(entity.getField("sundayWorking")).thenReturn(false);
        // when
        shiftsService.validateShiftHoursField(dataDefinition, entity);
        // then
    }

    @Test
    public void shouldAddErrorToEntityWhenFieldIsNull() throws Exception {
        // given
        when(entity.getField("mondayWorking")).thenReturn(true);
        when(entity.getStringField("mondayHours")).thenReturn(null);
        when(entity.getField("tuesdayWorking")).thenReturn(true);
        when(entity.getStringField("tuesdayHours")).thenReturn(null);
        when(entity.getField("wensdayWorking")).thenReturn(true);
        when(entity.getStringField("wensdayHours")).thenReturn(null);
        when(entity.getField("thursdayWorking")).thenReturn(true);
        when(entity.getStringField("thursdayHours")).thenReturn(null);
        when(entity.getField("fridayWorking")).thenReturn(true);
        when(entity.getStringField("fridayHours")).thenReturn(null);
        when(entity.getField("saturdayWorking")).thenReturn(true);
        when(entity.getStringField("saturdayHours")).thenReturn(null);
        when(entity.getField("sundayWorking")).thenReturn(true);
        when(entity.getStringField("sundayHours")).thenReturn(null);

        // when
        shiftsService.validateShiftHoursField(dataDefinition, entity);
        // then
    }

    @Test
    public void shouldReturnNullWhenShiftDoesnotExist() throws Exception {
        // given
        Date dateTo = mock(Date.class);
        SearchCriteriaBuilder builder = mock(SearchCriteriaBuilder.class);
        SearchResult result = mock(SearchResult.class);
        when(dataDefinitionService.get("basic", "shift")).thenReturn(dataDefinition);
        when(dataDefinition.find()).thenReturn(builder);
        when(builder.list()).thenReturn(result);
        when(result.getTotalNumberOfEntities()).thenReturn(0);
        // when
        Date dateToFromMethod = shiftsService.findDateFromForOrder(dateTo, 123L);
        // then
        Assert.assertNull(dateToFromMethod);
    }

    @Test
    public void shouldReturnDateFrom() throws Exception {
        // given
        Date dateTo = mock(Date.class);
        shifts.add(shift);
        exceptions = mockEntityList(new ArrayList<Entity>());
        exceptions.add(exception);
        String hours = "07:00-15:00";
        SearchCriteriaBuilder builder = mock(SearchCriteriaBuilder.class);
        SearchResult result = mock(SearchResult.class);
        when(dataDefinitionService.get("basic", "shift")).thenReturn(dataDefinition);
        when(dataDefinition.find()).thenReturn(builder);
        when(builder.list()).thenReturn(result);
        when(result.getTotalNumberOfEntities()).thenReturn(1);
        when(result.getEntities()).thenReturn(shifts);

        when(shift.getField("mondayWorking")).thenReturn(true);
        when(shift.getStringField("mondayHours")).thenReturn(hours);
        when(shift.getField("tuesdayWorking")).thenReturn(true);
        when(shift.getStringField("tuesdayHours")).thenReturn(hours);
        when(shift.getField("wensdayWorking")).thenReturn(true);
        when(shift.getStringField("wensdayHours")).thenReturn(hours);
        when(shift.getField("thursdayWorking")).thenReturn(true);
        when(shift.getStringField("thursdayHours")).thenReturn(hours);
        when(shift.getField("fridayWorking")).thenReturn(true);
        when(shift.getStringField("fridayHours")).thenReturn(hours);
        when(shift.getField("saturdayWorking")).thenReturn(true);
        when(shift.getStringField("saturdayHours")).thenReturn(hours);
        when(shift.getField("sundayWorking")).thenReturn(true);
        when(shift.getStringField("sundayHours")).thenReturn(hours);

        when(shift.getHasManyField("timetableExceptions")).thenReturn((EntityList) exceptions);
        // when
        shiftsService.findDateFromForOrder(dateTo, 123L);
        // then
    }
}
