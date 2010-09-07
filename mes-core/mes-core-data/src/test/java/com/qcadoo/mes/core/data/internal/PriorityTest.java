package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class PriorityTest extends DataAccessTest {

    @Before
    public void init() {
        dataDefinition.setPriorityField(fieldDefinitionPriority);
    }

    @Test
    public void shouldBePrioritizable() throws Exception {
        // then
        assertTrue(dataDefinition.isPrioritizable());
    }

    @Test
    public void shouldAddPriorityToEntityOnCreate() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("priority", 13);
        entity.setField("belongsTo", 1L);

        given(criteria.uniqueResult()).willReturn(10);

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals(11, validationResults.getEntity().getField("priority"));
    }

    @Test
    public void shouldNotChangePriorityOnUpdate() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("priority", 13);
        entity.setField("belongsTo", 2L);

        SimpleDatabaseObject existingDatabaseObject = new SimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject);

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals(11, validationResults.getEntity().getField("priority"));
    }

    @Test
    public void shouldChangeEntitiesWithPriorityGreaterThatDeleted() throws Exception {
        // given
        SimpleDatabaseObject existingDatabaseObject = new SimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(11);

        SimpleDatabaseObject otherDatabaseObject = new SimpleDatabaseObject(2L);
        otherDatabaseObject.setPriority(12);

        given(session.get(SimpleDatabaseObject.class, 1L)).willReturn(existingDatabaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList(otherDatabaseObject));

        // when
        dataAccessService.delete(dataDefinition, 1L);

        // then
        SimpleDatabaseObject deletedDatabaseObject = new SimpleDatabaseObject(1L);
        deletedDatabaseObject.setPriority(11);
        deletedDatabaseObject.setDeleted(true);

        verify(session).update(deletedDatabaseObject);

        SimpleDatabaseObject updatedDatabaseObject = new SimpleDatabaseObject(2L);
        updatedDatabaseObject.setPriority(11);

        verify(session).update(updatedDatabaseObject);
    }

    @Test
    public void shouldChangeEntitiesBetweenCurrentAndTargetPriorityWhileMoving() throws Exception {
        // given
        SimpleDatabaseObject existingDatabaseObject = new SimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(5);

        SimpleDatabaseObject otherDatabaseObject = new SimpleDatabaseObject(2L);
        otherDatabaseObject.setPriority(6);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject, 6);
        given(criteria.list()).willReturn(Lists.newArrayList(otherDatabaseObject));

        // when
        dataAccessService.move(dataDefinition, 1L, 1);

        // then
        SimpleDatabaseObject movedDatabaseObject = new SimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(6);

        verify(session).update(movedDatabaseObject);

        SimpleDatabaseObject updatedDatabaseObject = new SimpleDatabaseObject(2L);
        updatedDatabaseObject.setPriority(5);

        verify(session).update(updatedDatabaseObject);
    }

    @Test
    public void shouldChangeEntitiesBetweenCurrentAndTargetPriorityWhileMovingTo() throws Exception {
        // given
        SimpleDatabaseObject existingDatabaseObject = new SimpleDatabaseObject(1L);
        existingDatabaseObject.setPriority(5);

        SimpleDatabaseObject otherDatabaseObject = new SimpleDatabaseObject(2L);
        otherDatabaseObject.setPriority(6);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject, 6);
        given(criteria.list()).willReturn(Lists.newArrayList(otherDatabaseObject));

        // when
        dataAccessService.moveTo(dataDefinition, 1L, 6);

        // then
        SimpleDatabaseObject movedDatabaseObject = new SimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(6);

        verify(session).update(movedDatabaseObject);

        SimpleDatabaseObject updatedDatabaseObject = new SimpleDatabaseObject(2L);
        updatedDatabaseObject.setPriority(5);

        verify(session).update(updatedDatabaseObject);
    }

    @Test
    public void shouldNotMoveToPositionBelowOne() throws Exception {
        // given
        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataAccessService.moveTo(dataDefinition, 1L, -2);

        // then
        SimpleDatabaseObject movedDatabaseObject = new SimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(1);

        verify(session).update(movedDatabaseObject);
    }

    @Test
    public void shouldNotMoveToOffsetBelowOne() throws Exception {
        // given
        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataAccessService.move(dataDefinition, 1L, -20);

        // then
        SimpleDatabaseObject movedDatabaseObject = new SimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(1);

        verify(session).update(movedDatabaseObject);
    }

    @Test
    public void shouldNotMoveToPositionAboveMax() throws Exception {
        // given
        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject, 5);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataAccessService.moveTo(dataDefinition, 1L, 10);

        // then
        SimpleDatabaseObject movedDatabaseObject = new SimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(5);

        verify(session).update(movedDatabaseObject);
    }

    @Test
    public void shouldNotMoveIfPositionDoesNotChange() throws Exception {
        // given
        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject, 11);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataAccessService.moveTo(dataDefinition, 1L, 15);

        // then
        verify(session, never()).update(Mockito.any(SimpleDatabaseObject.class));
    }

    @Test
    public void shouldNotMoveToOffsetAboveMax() throws Exception {
        // given
        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);
        databaseObject.setPriority(11);

        given(criteria.uniqueResult()).willReturn(databaseObject, 15);
        given(criteria.list()).willReturn(Lists.newArrayList());

        // when
        dataAccessService.moveTo(dataDefinition, 1L, 20);

        // then
        SimpleDatabaseObject movedDatabaseObject = new SimpleDatabaseObject(1L);
        movedDatabaseObject.setPriority(15);

        verify(session).update(movedDatabaseObject);
    }
}
