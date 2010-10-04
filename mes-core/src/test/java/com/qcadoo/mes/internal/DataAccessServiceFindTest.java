package com.qcadoo.mes.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;

public class DataAccessServiceFindTest extends DataAccessTest {

    @Test
    public void shouldReturnValidEntities() throws Exception {
        // given
        List<SampleSimpleDatabaseObject> databaseObjects = new ArrayList<SampleSimpleDatabaseObject>();
        databaseObjects.add(createDatabaseObject(1L, "name1", 1));
        databaseObjects.add(createDatabaseObject(2L, "name2", 2));
        databaseObjects.add(createDatabaseObject(3L, "name3", 3));
        databaseObjects.add(createDatabaseObject(4L, "name4", 4));

        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinition.find().withFirstResult(0).withMaxResults(4);

        given(criteria.uniqueResult()).willReturn(4);
        given(criteria.list()).willReturn(databaseObjects);

        // when
        SearchResult resultSet = searchCriteriaBuilder.list();

        // then
        assertEquals(4, resultSet.getTotalNumberOfEntities());
        assertEquals(4, resultSet.getEntities().size());
        assertEquals(Long.valueOf(1L), resultSet.getEntities().get(0).getId());
        assertEquals(Long.valueOf(2L), resultSet.getEntities().get(1).getId());
        assertEquals(Long.valueOf(3L), resultSet.getEntities().get(2).getId());
        assertEquals(Long.valueOf(4L), resultSet.getEntities().get(3).getId());
    }

    private SampleSimpleDatabaseObject createDatabaseObject(final Long id, final String name, final int age) {
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject(id);
        simpleDatabaseObject.setName(name);
        simpleDatabaseObject.setAge(age);
        return simpleDatabaseObject;
    }

}
