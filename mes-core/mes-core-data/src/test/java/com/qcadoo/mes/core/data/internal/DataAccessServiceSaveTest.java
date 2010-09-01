package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class DataAccessServiceSaveTest {

    private final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private final EntityService entityService = new EntityService();

    private final ValidationService validationService = new ValidationService();

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private final FieldTypeFactory fieldTypeFactory = new FieldTypeFactoryImpl();

    private DataAccessService dataAccessService = null;

    @Before
    public void init() {
        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(entityService, "validationService", validationService);
        ReflectionTestUtils.setField(validationService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(validationService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitionName.setValidators();
        dataDefinition.addField(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitionAge.setValidators();
        dataDefinition.addField(fieldDefinitionAge);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfEntityWithGivenIdNotExist() throws Exception {
        // given
        givenGetWillReturn(1L, null);

        // then
        dataAccessService.save("test.Entity", new Entity(1L));
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
        ValidationResults validationResults = dataAccessService.save("test.Entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(databaseObject);
        assertFalse(validationResults.isNotValid());
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

        givenGetWillReturn(1L, existingDatabaseObject);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject();
        databaseObject.setId(1L);
        databaseObject.setName("Mr T");
        databaseObject.setAge(66);

        // when
        ValidationResults validationResults = dataAccessService.save("test.Entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(databaseObject);
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldFailIfFieldTypeIsNotValid() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", "r");

        // when
        ValidationResults validationResults = dataAccessService.save("test.Entity", entity);

        // then
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldConvertTypeFromInteger() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", "66");

        // when
        ValidationResults validationResults = dataAccessService.save("test.Entity", entity);

        // then
        assertFalse(validationResults.isNotValid());
    }

    private void givenGetWillReturn(final Long id, final SimpleDatabaseObject existingDatabaseObject) {
        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(existingDatabaseObject);
    }

}
