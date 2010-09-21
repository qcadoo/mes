package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.util.Assert.isInstanceOf;

import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.model.ModelDefinitionImpl;
import com.qcadoo.mes.core.data.internal.model.FieldDefinitionImpl;
import com.qcadoo.mes.core.data.model.FieldDefinition;

public class EntityServiceTest extends DataAccessTest {

    @Test(expected = IllegalStateException.class)
    public void shouldThrownAnExceptionWhileGettingNotExistingField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("unknown");

        // when
        entityService.getField(databaseEntity, fieldDefinition);
    }

    @Test
    public void shouldReturnNullWhileGettingEmptyField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);

        // when
        Object value = entityService.getField(databaseEntity, fieldDefinitionName);

        // then
        assertNull(value);
    }

    @Test
    public void shouldReturnProperValueOfTheField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);
        databaseEntity.setName("Mr T");

        // when
        Object value = entityService.getField(databaseEntity, fieldDefinitionName);

        // then
        assertEquals("Mr T", value);
    }

    @Test
    public void shouldReturnProperValueOfTheBelongsToField() throws Exception {
        // given
        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(2L);
        databaseEntity.setName("Mr T");
        databaseEntity.setBelongsTo(parentDatabaseEntity);

        // when
        Object value = entityService.getField(databaseEntity, fieldDefinitionBelongsTo);

        // then
        isInstanceOf(Entity.class, value);
        assertEquals(Long.valueOf(1), ((Entity) value).getId());
        assertEquals("Mr X", ((Entity) value).getField("name"));
    }

    @Test
    public void shouldNotThrownAnExceptionWhileGettingFieldWithInvalidType() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);
        databaseEntity.setName("Mr T");

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("name").withType(fieldTypeFactory.integerType());

        // when
        entityService.getField(databaseEntity, fieldDefinition);
    }

    @Test
    public void shouldReturnClassForGivenDataDefinition() throws Exception {
        // when
        Class<?> clazz = dataDefinition.getClassForEntity();

        // then
        assertEquals(SimpleDatabaseObject.class, clazz);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrownAnExceptionIfClassForGivenDataDefinitionDoesNotExist() throws Exception {
        // given
        ModelDefinitionImpl dataDefinition = new ModelDefinitionImpl("definition", null);
        dataDefinition.setFullyQualifiedClassName("java.lang.SomeUselessNotExistingClass");

        // when
        dataDefinition.getClassForEntity();
    }

    @Test
    public void shouldReturnProperId() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(13L);

        // when
        Long id = entityService.getId(databaseEntity);

        // then
        assertEquals(Long.valueOf(13), id);
    }

    @Test
    public void shouldSetProperId() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject();

        // when
        entityService.setId(databaseEntity, 13L);

        // then
        assertEquals(Long.valueOf(13), databaseEntity.getId());
    }

    @Test
    public void shouldSetEntityAsDeleted() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject();

        // when
        entityService.setDeleted(databaseEntity);

        // then
        assertTrue(databaseEntity.isDeleted());
    }

    @Test
    public void shouldNotBeDeletedAfterEntityCreation() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject();

        // then
        assertFalse(databaseEntity.isDeleted());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrownAnExceptionWhileSettingNotExistingField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("unknown").withType(fieldTypeFactory.stringType());

        // when
        entityService.setField(databaseEntity, fieldDefinition, "XXX");
    }

    @Test
    public void shouldSetEmptyField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);
        databaseEntity.setName("name");

        // when
        entityService.setField(databaseEntity, fieldDefinitionName, null);

        // then
        assertNull(databaseEntity.getName());
    }

    @Test
    public void shouldSetNotEmptyField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);
        databaseEntity.setName("name");

        // when
        entityService.setField(databaseEntity, fieldDefinitionName, "XXX");

        // then
        assertEquals("XXX", databaseEntity.getName());
    }

    @Test
    public void shouldSetBelongsToField() throws Exception {
        // given
        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(2L);

        given(session.get(ParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);

        // when
        entityService.setField(databaseEntity, fieldDefinitionBelongsTo, parentDatabaseEntity);

        // then
        assertNotNull(databaseEntity.getBelongsTo());
        assertEquals(parentDatabaseEntity, databaseEntity.getBelongsTo());
    }

    @Test
    public void shouldSetNullIfBelongsToFieldIsEmpty() throws Exception {
        // given
        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(2L);
        databaseEntity.setBelongsTo(parentDatabaseEntity);

        // when
        entityService.setField(databaseEntity, fieldDefinitionBelongsTo, null);

        // then
        assertNull(databaseEntity.getBelongsTo());
    }

    @Test
    public void shouldConvertDatabaseEntityIntoGenericOne() throws Exception {
        // given
        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(2L);
        databaseEntity.setAge(12);
        databaseEntity.setName("Mr T");
        databaseEntity.setBelongsTo(parentDatabaseEntity);

        // when
        Entity genericEntity = entityService.convertToGenericEntity(dataDefinition, databaseEntity);

        // then
        assertNotNull(genericEntity);
        assertEquals(Long.valueOf(2), genericEntity.getId());
        assertEquals(12, genericEntity.getField("age"));
        assertEquals("Mr T", genericEntity.getField("name"));
        isInstanceOf(Entity.class, genericEntity.getField("belongsTo"));
        assertEquals("Mr X", ((Entity) genericEntity.getField("belongsTo")).getField("name"));
    }

    @Test
    public void shouldConvertGenericEntityIntoDatabaseOne() throws Exception {
        // given
        Entity genericEntity = new Entity(2L);
        genericEntity.setField("name", "Mr T");
        genericEntity.setField("age", 12);
        genericEntity.setField("belongsTo", 1L);

        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        given(session.get(ParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);

        validationService.validateGenericEntity(dataDefinition, genericEntity, null);

        // when
        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, null);

        // then
        assertNotNull(databaseEntity);
        isInstanceOf(SimpleDatabaseObject.class, databaseEntity);
        assertEquals(Long.valueOf(2), ((SimpleDatabaseObject) databaseEntity).getId());
        assertEquals(Integer.valueOf(12), ((SimpleDatabaseObject) databaseEntity).getAge());
        assertEquals("Mr T", ((SimpleDatabaseObject) databaseEntity).getName());
        assertNotNull(((SimpleDatabaseObject) databaseEntity).getBelongsTo());
        assertEquals("Mr X", ((SimpleDatabaseObject) databaseEntity).getBelongsTo().getName());
    }

    @Test
    public void shouldConvertGenericEntityIntoDatabaseOneUsingExistingEntity() throws Exception {
        // given
        Entity genericEntity = new Entity(2L);
        genericEntity.setField("name", "Mr T");
        genericEntity.setField("age", 12);
        genericEntity.setField("belongsTo", 1L);

        SimpleDatabaseObject existingDatabaseEntity = new SimpleDatabaseObject(11L);

        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        given(session.get(ParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);

        validationService.validateGenericEntity(dataDefinition, genericEntity, new Entity(2L));

        // when
        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity);

        // then
        assertNotNull(databaseEntity);
        isInstanceOf(SimpleDatabaseObject.class, databaseEntity);
        assertEquals(Long.valueOf(11), ((SimpleDatabaseObject) databaseEntity).getId());
        assertEquals(Integer.valueOf(12), ((SimpleDatabaseObject) databaseEntity).getAge());
        assertEquals("Mr T", ((SimpleDatabaseObject) databaseEntity).getName());
        assertNotNull(((SimpleDatabaseObject) databaseEntity).getBelongsTo());
        assertEquals("Mr X", ((SimpleDatabaseObject) databaseEntity).getBelongsTo().getName());
    }

}
