package com.qcadoo.mes.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.junit.Test;

import com.qcadoo.mes.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.model.types.HasManyType;

public class DataAccessServiceDeleteTest extends DataAccessTest {

    @Test
    public void shouldProperlyDelete() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setDeleted(false);

        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        dataDefinition.delete(1L);

        // then
        SampleSimpleDatabaseObject simpleDatabaseObjectDeleted = new SampleSimpleDatabaseObject();
        simpleDatabaseObjectDeleted.setId(1L);
        simpleDatabaseObjectDeleted.setName("Mr T");
        simpleDatabaseObjectDeleted.setAge(66);
        simpleDatabaseObjectDeleted.setDeleted(true);

        verify(session).update(simpleDatabaseObjectDeleted);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfEntityNotFound() throws Exception {
        // given
        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(null);

        // when
        dataDefinition.delete(1L);
    }

    @Test
    public void shouldProperlyRecursiveDelete() throws Exception {
        // given
        SampleParentDatabaseObject parentDatabaseEntity = new SampleParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");
        parentDatabaseEntity.setId(1L);
        parentDatabaseEntity.setDeleted(false);
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setDeleted(false);
        List<SampleSimpleDatabaseObject> entities = new ArrayList<SampleSimpleDatabaseObject>();
        entities.add(simpleDatabaseObject);

        given(session.get(SampleParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);
        given(criteria.uniqueResult()).willReturn(4);
        given(criteria.list()).willReturn(entities);
        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        parentDataDefinition.delete(1L);

        // then
        SampleParentDatabaseObject parentDatabaseEntityDeleted = new SampleParentDatabaseObject(1L);
        parentDatabaseEntityDeleted.setName("Mr X");
        parentDatabaseEntityDeleted.setId(1L);
        parentDatabaseEntityDeleted.setDeleted(true);
        SampleSimpleDatabaseObject simpleDatabaseObjectDeleted = new SampleSimpleDatabaseObject();
        simpleDatabaseObjectDeleted.setId(1L);
        simpleDatabaseObjectDeleted.setName("Mr T");
        simpleDatabaseObjectDeleted.setAge(66);
        simpleDatabaseObjectDeleted.setDeleted(true);

        verify(session).update(parentDatabaseEntityDeleted);
        verify(session).update(simpleDatabaseObjectDeleted);
    }

    @Test
    public void shouldProperlyDeleteAndNullifyChildren() throws Exception {
        // given
        SampleParentDatabaseObject parentDatabaseEntity = new SampleParentDatabaseObject(1L);
        parentDatabaseEntity.setName("Mr X");
        parentDatabaseEntity.setId(1L);
        parentDatabaseEntity.setDeleted(false);
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        simpleDatabaseObject.setDeleted(false);
        simpleDatabaseObject.setBelongsTo(parentDatabaseEntity);
        List<SampleSimpleDatabaseObject> entities = new ArrayList<SampleSimpleDatabaseObject>();
        entities.add(simpleDatabaseObject);

        parentFieldDefinitionHasMany.withType(fieldTypeFactory.hasManyType("simple", "entity", "belongsTo",
                HasManyType.Cascade.NULLIFY));
        parentDataDefinition.withField(parentFieldDefinitionHasMany);

        Criteria databaseCriteria = mock(Criteria.class, RETURNS_DEEP_STUBS);

        given(session.get(SampleParentDatabaseObject.class, 1L)).willReturn(parentDatabaseEntity);
        given(session.createCriteria(SampleSimpleDatabaseObject.class)).willReturn(databaseCriteria);
        given(databaseCriteria.add(any(Criterion.class))).willReturn(databaseCriteria);
        given(databaseCriteria.setFirstResult(anyInt())).willReturn(databaseCriteria);
        given(databaseCriteria.setMaxResults(anyInt())).willReturn(databaseCriteria);
        given(databaseCriteria.addOrder(any(Order.class))).willReturn(databaseCriteria);
        given(databaseCriteria.setProjection(any(Projection.class)).uniqueResult()).willReturn(4);
        given(databaseCriteria.list()).willReturn(entities);
        given(databaseCriteria.uniqueResult()).willReturn(simpleDatabaseObject);

        // when
        parentDataDefinition.delete(1L);

        // then
        SampleParentDatabaseObject parentDatabaseEntityDeleted = new SampleParentDatabaseObject(1L);
        parentDatabaseEntityDeleted.setName("Mr X");
        parentDatabaseEntityDeleted.setId(1L);
        parentDatabaseEntityDeleted.setDeleted(true);
        SampleSimpleDatabaseObject simpleDatabaseObjectDeleted = new SampleSimpleDatabaseObject();
        simpleDatabaseObjectDeleted.setId(1L);
        simpleDatabaseObjectDeleted.setName("Mr T");
        simpleDatabaseObjectDeleted.setAge(66);
        simpleDatabaseObjectDeleted.setDeleted(false);
        simpleDatabaseObject.setBelongsTo(null);

        verify(session).update(parentDatabaseEntityDeleted);
        verify(session).save(simpleDatabaseObjectDeleted);
    }
}
