package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.util.Assert.isInstanceOf;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class EntityServiceTest {

    private final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private final DictionaryService dictionaryService = mock(DictionaryService.class);

    private final DataAccessService dataAccessService = mock(DataAccessService.class);

    private final FieldTypeFactory fieldTypeFactory = new FieldTypeFactoryImpl();

    private EntityService entityService = null;

    private ValidationService validationService = null;

    private DataDefinition parentDataDefinition = null;

    private DataDefinition dataDefinition = null;

    private FieldDefinition fieldDefinitionAge = null;

    private FieldDefinition fieldDefinitionName = null;

    private FieldDefinition fieldDefinitionBelongsTo = null;

    private FieldDefinition parentFieldDefinitionName = null;

    @Before
    public void init() {
        validationService = new ValidationService();
        ReflectionTestUtils.setField(validationService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(validationService, "dataDefinitionService", dataDefinitionService);

        entityService = new EntityService();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(entityService, "validationService", validationService);

        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataAccessService", dataAccessService);

        parentFieldDefinitionName = new FieldDefinition("name");
        parentFieldDefinitionName.setType(fieldTypeFactory.stringType());
        parentFieldDefinitionName.setValidators();

        fieldDefinitionBelongsTo = new FieldDefinition("belongsTo");
        fieldDefinitionBelongsTo.setType(fieldTypeFactory.eagerBelongsToType("parent.entity", "name"));
        fieldDefinitionBelongsTo.setValidators();

        fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitionName.setValidators();

        fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitionAge.setValidators();

        parentDataDefinition = new DataDefinition("parent.entity");
        parentDataDefinition.addField(parentFieldDefinitionName);
        parentDataDefinition.setFullyQualifiedClassName(ParentDatabaseObject.class.getCanonicalName());

        dataDefinition = new DataDefinition("simple.entity");
        dataDefinition.addField(fieldDefinitionName);
        dataDefinition.addField(fieldDefinitionAge);
        dataDefinition.addField(fieldDefinitionBelongsTo);
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        given(dataDefinitionService.get("simple.entity")).willReturn(dataDefinition);

        given(dataDefinitionService.get("parent.entity")).willReturn(parentDataDefinition);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrownAnExceptionWhileGettingNotExistingField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);

        FieldDefinition fieldDefinition = new FieldDefinition("unknown");

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

        FieldDefinition fieldDefinition = new FieldDefinition("name");
        fieldDefinition.setType(fieldTypeFactory.integerType());
        fieldDefinition.setValidators();

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
        DataDefinition dataDefinition = new DataDefinition("definition");
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

        FieldDefinition fieldDefinition = new FieldDefinition("unknown");
        fieldDefinition.setType(fieldTypeFactory.stringType());
        fieldDefinition.setValidators();

        // when
        entityService.setField(databaseEntity, dataDefinition, fieldDefinition, "XXX");
    }

    @Test
    public void shouldSetEmptyField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);
        databaseEntity.setName("name");

        // when
        entityService.setField(databaseEntity, dataDefinition, fieldDefinitionName, null);

        // then
        assertNull(databaseEntity.getName());
    }

    @Test
    public void shouldSetNotEmptyField() throws Exception {
        // given
        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(1L);
        databaseEntity.setName("name");

        // when
        entityService.setField(databaseEntity, dataDefinition, fieldDefinitionName, "XXX");

        // then
        assertEquals("XXX", databaseEntity.getName());
    }

    @Test
    public void shouldSetBelongsToField() throws Exception {
        // given
        ParentDatabaseObject parentDatabaseEntity = new ParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");

        SimpleDatabaseObject databaseEntity = new SimpleDatabaseObject(2L);

        given(sessionFactory.getCurrentSession().get(ParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);

        // when
        entityService.setField(databaseEntity, dataDefinition, fieldDefinitionBelongsTo, parentDatabaseEntity);

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
        entityService.setField(databaseEntity, dataDefinition, fieldDefinitionBelongsTo, null);

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

        given(sessionFactory.getCurrentSession().get(ParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);

        // when
        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, null,
                new ValidationResults());

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

        given(sessionFactory.getCurrentSession().get(ParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);

        // when
        Object databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity,
                new ValidationResults());

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
