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
