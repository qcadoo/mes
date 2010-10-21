package com.qcadoo.mes.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;

public final class DataAccessServiceSaveTest extends DataAccessTest {

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfEntityWithGivenIdNotExist() throws Exception {
        // then
        dataDefinition.save(new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName(), 1L));
    }

    @Test
    public void shouldSaveNewEntity() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        entity.setField("name", "Mr T");
        entity.setField("age", 66);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject();
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
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName(), 1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 66);

        SampleSimpleDatabaseObject existingDatabaseObject = new SampleSimpleDatabaseObject();
        existingDatabaseObject.setId(1L);
        existingDatabaseObject.setName("Mr X");
        existingDatabaseObject.setAge(33);

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject();
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
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
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
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        entity.setField("name", "Mr T");
        entity.setField("age", "66");

        // when
        entity = dataDefinition.save(entity);

        // then
        assertTrue(entity.isValid());
    }

}
