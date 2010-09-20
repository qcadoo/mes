package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;

public final class DataAccessServiceSaveTest extends DataAccessTest {

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfEntityWithGivenIdNotExist() throws Exception {
        // then
        dataDefinition.save(new Entity(1L));
    }

    @Test
    public void shouldSaveNewEntity() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", 66);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject();
        databaseObject.setName("Mr T");
        databaseObject.setAge(66);

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(databaseObject);
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldSaveExistingEntity() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 66);

        SimpleDatabaseObject existingDatabaseObject = new SimpleDatabaseObject();
        existingDatabaseObject.setId(1L);
        existingDatabaseObject.setName("Mr X");
        existingDatabaseObject.setAge(33);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject();
        databaseObject.setId(1L);
        databaseObject.setName("Mr T");
        databaseObject.setAge(66);

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(databaseObject);
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldFailIfFieldTypeIsNotValid() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", "r");

        // when
        entity = dataDefinition.save(entity);

        // then
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldConvertTypeFromInteger() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", "66");

        // when
        entity = dataDefinition.save(entity);

        // then
        assertTrue(entity.isValid());
    }

}
