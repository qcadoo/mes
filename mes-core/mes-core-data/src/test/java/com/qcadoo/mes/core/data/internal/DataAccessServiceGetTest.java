package com.qcadoo.mes.core.data.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldTypes;

public class DataAccessServiceGetTest {

    private DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

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

        // when
        given(dataDefinitionService.get("entityWithNoClass")).willReturn(dataDefinition);

        // then
        dataAccessService.get("entityWithNoClass", 1L);
    }

    @Test
    public void shouldReturnValidEntity() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        FieldDefinition fieldDefinitionName = new FieldDefinition();
        fieldDefinitionName.setName("name");
        fieldDefinitionName.setType(FieldTypes.stringType());
        fieldDefinitions.add(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition();
        fieldDefinitionName.setName("age");
        fieldDefinitionName.setType(FieldTypes.intType());
        fieldDefinitions.add(fieldDefinitionAge);

        dataDefinition.setFields(fieldDefinitions);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(sessionFactory.getCurrentSession().get(SimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataAccessService.get("test.Entity", 1L);

        // then
        assertEquals(1L, entity.getId().longValue());
        assertEquals("Mr T", entity.getField("name"));
        assertEquals(66, entity.getField("age"));
    }

}
