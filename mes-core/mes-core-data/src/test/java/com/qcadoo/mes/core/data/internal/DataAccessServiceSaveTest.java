package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

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
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class DataAccessServiceSaveTest {

    private DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private EntityServiceImpl entityService = new EntityServiceImpl();

    private SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private FieldTypeFactory fieldTypeFactory = new FieldTypeFactoryImpl();

    private DataAccessService dataAccessService = null;

    @Before
    public void init() {
        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitionName.setValidators();
        fieldDefinitions.add(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitionAge.setValidators();
        fieldDefinitions.add(fieldDefinitionAge);

        dataDefinition.setFields(fieldDefinitions);

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
        assertFalse(validationResults.hasError());
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
        assertFalse(validationResults.hasError());
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
        assertTrue(validationResults.hasError());
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
        assertFalse(validationResults.hasError());
    }

    private void givenGetWillReturn(Long id, SimpleDatabaseObject existingDatabaseObject) {
        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(existingDatabaseObject);
    }

}
