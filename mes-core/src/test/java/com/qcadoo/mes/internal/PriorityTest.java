package com.qcadoo.mes.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.util.Assert.notNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.internal.DefaultEntity;

public class PriorityTest extends DataAccessTest {

    @Before
    public void init() {
        dataDefinition.withPriorityField(fieldDefinitionPriority);
    }

    @Test
    public void shouldBePrioritizable() throws Exception {
        // then
        assertTrue(dataDefinition.isPrioritizable());
    }

    @Test
    public void shouldHasPriorityField() throws Exception {
        // then
        notNull(dataDefinition.getField("priority"));
    }

    @Test
    public void shouldAddPriorityToEntityOnCreate() throws Exception {
        // given
        Entity entity = new DefaultEntity();
        entity.setField("priority", 13);
        entity.setField("belongsTo", 1L);

        given(criteria.uniqueResult()).willReturn(10);

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(11, entity.getField("priority"));
    }

    @Test
    public void shouldNotChangePriorityOnUpdate() throws Exception {
        // given
        Entity entity = new DefaultEntity(1L);
        entity.setField("priority", 13);
        entity.setField("belongsTo", 2L);

        SampleSimpleDatabaseObject existingDatabaseObject = new SampleSimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject);

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(11, entity.getField("priority"));
    }

    @Test
    public void shouldChangeEntitiesWithPriorityGreaterThatDeleted() throws Exception {
        // given
        SampleSimpleDatabaseObject existingDatabaseObject = new SampleSimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(11);

        SampleSimpleDatabaseObject otherDatabaseObject = new SampleSimpleDatabaseObject(2L);
        otherDatabaseObject.setPriority(12);

        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(existingDatabaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList(otherDatabaseObject));

        // when
        dataDefinition.delete(1L);

        // then
        SampleSimpleDatabaseObject deletedDatabaseObject = new SampleSimpleDatabaseObject(1L);
        deletedDatabaseObject.setPriority(11);
        deletedDatabaseObject.setDeleted(true);

        verify(session).update(deletedDatabaseObject);

        SampleSimpleDatabaseObject updatedDatabaseObject = new SampleSimpleDatabaseObject(2L);
        updatedDatabaseObject.setPriority(11);

        verify(session).update(updatedDatabaseObject);
    }

    @Test
    public void shouldChangeEntitiesBetweenCurrentAndTargetPriorityWhileMoving() throws Exception {
        // given
        SampleSimpleDatabaseObject existingDatabaseObject = new SampleSimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(5);

        SampleSimpleDatabaseObject otherDatabaseObject = new SampleSimpleDatabaseObject(2L);
        otherDatabaseObject.setPriority(6);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject, 6);
        given(criteria.list()).willReturn(Lists.newArrayList(otherDatabaseObject));

        // when
        dataDefinition.move(1L, 1);

        // then
        SampleSimpleDatabaseObject movedDatabaseObject = new SampleSimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(6);

        verify(session).update(movedDatabaseObject);

        SampleSimpleDatabaseObject updatedDatabaseObject = new SampleSimpleDatabaseObject(2L);
        updatedDatabaseObject.setPriority(5);

        verify(session).update(updatedDatabaseObject);
    }

    @Test
    public void shouldChangeEntitiesBetweenCurrentAndTargetPriorityWhileMovingTo() throws Exception {
        // given
        SampleSimpleDatabaseObject existingDatabaseObject = new SampleSimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(5);

        SampleSimpleDatabaseObject otherDatabaseObject = new SampleSimpleDatabaseObject(2L);
        otherDatabaseObject.setPriority(6);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject, 6);
        given(criteria.list()).willReturn(Lists.newArrayList(otherDatabaseObject));

        // when
        dataDefinition.moveTo(1L, 6);

        // then
        SampleSimpleDatabaseObject movedDatabaseObject = new SampleSimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(6);

        verify(session).update(movedDatabaseObject);

        SampleSimpleDatabaseObject updatedDatabaseObject = new SampleSimpleDatabaseObject(2L);
        updatedDatabaseObject.setPriority(5);

        verify(session).update(updatedDatabaseObject);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotMoveToPositionBelowOne() throws Exception {
        // when
        dataDefinition.moveTo(1L, -2);
    }

    @Test
    public void shouldNotMoveToOffsetBelowOne() throws Exception {
        // given
        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataDefinition.move(1L, -20);

        // then
        SampleSimpleDatabaseObject movedDatabaseObject = new SampleSimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(1);

        verify(session).update(movedDatabaseObject);
    }

    @Test
    public void shouldNotMoveToPositionAboveMax() throws Exception {
        // given
        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject, 5);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataDefinition.moveTo(1L, 10);

        // then
        SampleSimpleDatabaseObject movedDatabaseObject = new SampleSimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(5);

        verify(session).update(movedDatabaseObject);
    }

    @Test
    public void shouldNotMoveIfPositionDoesNotChange() throws Exception {
        // given
        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject, 11);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataDefinition.moveTo(1L, 15);

        // then
        verify(session, never()).update(Mockito.any(SampleSimpleDatabaseObject.class));
    }

    @Test
    public void shouldNotMoveToOffsetAboveMax() throws Exception {
        // given
        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject, 15);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataDefinition.moveTo(1L, 20);

        // then
        SampleSimpleDatabaseObject movedDatabaseObject = new SampleSimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(15);

        verify(session).update(movedDatabaseObject);
    }
}
