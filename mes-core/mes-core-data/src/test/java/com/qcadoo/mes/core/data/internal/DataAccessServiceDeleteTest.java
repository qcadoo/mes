package com.qcadoo.mes.core.data.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;

public class DataAccessServiceDeleteTest {

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
    }

    @Test
    public void shouldProperlyDelete() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        dataDefinition.addField(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        dataDefinition.addField(fieldDefinitionAge);

        FieldDefinition fieldDefinitionDeleted = new FieldDefinition("deleted");
        fieldDefinitionDeleted.setType(fieldTypeFactory.booleanType());
        dataDefinition.addField(fieldDefinitionDeleted);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setDeleted(false);

        given(sessionFactory.getCurrentSession().get(SimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        dataAccessService.delete("test.Entity", 1L);

        // then
        SimpleDatabaseObject simpleDatabaseObjectDeleted = new SimpleDatabaseObject();
        simpleDatabaseObjectDeleted.setId(1L);
        simpleDatabaseObjectDeleted.setName("Mr T");
        simpleDatabaseObjectDeleted.setAge(66);
        simpleDatabaseObjectDeleted.setDeleted(true);

        Mockito.verify(sessionFactory.getCurrentSession()).update(simpleDatabaseObjectDeleted);
    }

    @Test
    public void shouldFailIfEntityNotFound() throws Exception {
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        dataDefinition.addField(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        dataDefinition.addField(fieldDefinitionAge);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        given(sessionFactory.getCurrentSession().get(SimpleDatabaseObject.class, 1L)).willReturn(null);

        // when
        dataAccessService.delete("test.Entity", 1L);

        // then
    }
}
