package com.qcadoo.mes.core.data.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class DataAccessServiceGetTest {

    private DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private FieldTypeFactory fieldTypeFactory = new FieldTypeFactory();

    private DataAccessService dataAccessService = null;

    @Before
    public void init() {
        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfThereIsNoDataDefinitionForGivenEntityName() throws Exception {
        // when
        dataAccessService.get("not existing entity name", 1L);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfThereIsNoClassForGivenEntityName() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName("not.existing.class.Name");
        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        // when
        dataAccessService.get("test.Entity", 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfIdIsNull() throws Exception {
        // when
        dataAccessService.get("test.Entity", null);
    }

    @Test
    public void shouldReturnValidEntity() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitions.add(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitions.add(fieldDefinitionAge);

        dataDefinition.setFields(fieldDefinitions);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).uniqueResult()).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataAccessService.get("test.Entity", 1L);

        // then
        assertEquals(1L, entity.getId().longValue());
        assertEquals("Mr T", entity.getField("name"));
        assertEquals(66, entity.getField("age"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfFieldTypeIsNotValid() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.integerType());
        fieldDefinitions.add(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitions.add(fieldDefinitionAge);

        dataDefinition.setFields(fieldDefinitions);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).uniqueResult()).willReturn(simpleDatabaseObject);

        // when
        dataAccessService.get("test.Entity", 1L);
    }

    public void shouldReturnNullIfEntityNotFound() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(any(Criterion.class))
                        .add(any(Criterion.class)).uniqueResult()).willReturn(null);

        // when
        Entity entity = dataAccessService.get("test.Entity", 1L);

        // then
        assertNull(entity);
    }

}
