package com.qcadoo.mes.core.data.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldTypes;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaImpl;
import com.qcadoo.mes.core.data.search.ResultSet;

public class DataAccessServiceFindTest {

    private DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private DataAccessService dataAccessService = null;

    @Before
    public void init() {
        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
    }

    @Test
    public void shouldReturnValidEntities() throws Exception {
        int count = 5;
        // given
        DataDefinition dataDefinition = new DataDefinition("test.Entity");
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        FieldDefinition fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(FieldTypes.stringType());
        fieldDefinitions.add(fieldDefinitionName);

        FieldDefinition fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(FieldTypes.integerType());
        fieldDefinitions.add(fieldDefinitionAge);

        dataDefinition.setFields(fieldDefinitions);

        given(dataDefinitionService.get("test.Entity")).willReturn(dataDefinition);

        List<SimpleDatabaseObject> databaseObjects = new ArrayList<SimpleDatabaseObject>();

        for (int i = 0; i <= count; i++) {
            SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
            simpleDatabaseObject.setId(Long.valueOf(i));
            simpleDatabaseObject.setName("Mr T" + i);
            simpleDatabaseObject.setAge(66 + i);

            databaseObjects.add(simpleDatabaseObject);
        }

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class)
                        .setProjection(any(Projection.class)).uniqueResult()).willReturn(Long.valueOf(count - 1));

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).setFirstResult(1).setMaxResults(4)
                        .add(any(Criterion.class)).list()).willReturn(databaseObjects.subList(0, count - 1));

        SearchCriteriaImpl searchCriteria = new SearchCriteriaImpl("test.Entity");
        searchCriteria.setFirstResult(1);
        searchCriteria.setMaxResults(count - 1);

        // when
        ResultSet resultSet = dataAccessService.find("test.Entity", searchCriteria);

        // then
        List<Entity> results = resultSet.getResults();
        assertEquals(count - 1, resultSet.getTotalNumberOfEntities());
        assertEquals(count - 1, results.size());
        for (int i = 0; i < count - 1; i++) {
            Entity entity = (Entity) results.get(i);
            assertEquals(Long.valueOf(i), entity.getId());
            assertEquals("Mr T" + i, entity.getField("name"));
            assertEquals(66 + i, entity.getField("age"));
        }
    }
}
