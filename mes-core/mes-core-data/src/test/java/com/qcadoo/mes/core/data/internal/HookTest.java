package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;

public class HookTest extends DataAccessTest {

    @Before
    public void init() {
        given(applicationContext.getBean("callback")).willReturn(new CustomCallbackMethod());
    }

    @Test
    public void shouldNotCallAnyCallbackIfNotDefined() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallOnCreateCallback() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getHook("callback", "onCreate"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("create", entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallOnUpdateCallback() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(databaseObject);

        dataDefinition.setOnUpdate(callbackFactory.getHook("callback", "onUpdate"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("update", entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallAllDefinedCallbacksWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getHook("callback", "onCreate"));
        dataDefinition.setOnSave(callbackFactory.getHook("callback", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("create", entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallOnSaveCallbackWhileUpdating() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(databaseObject);

        dataDefinition.setOnSave(callbackFactory.getHook("callback", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallAllDefinedCallbacksWhileUpdating() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(databaseObject);

        dataDefinition.setOnUpdate(callbackFactory.getHook("callback", "onUpdate"));
        dataDefinition.setOnSave(callbackFactory.getHook("callback", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("update", entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallOnSaveCallbackWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getHook("callback", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    public class CustomCallbackMethod {

        public void onUpdate(final DataDefinition dataDefinition, final Entity entity) {
            entity.setField("name", "update");
        }

        public void onSave(final DataDefinition dataDefinition, final Entity entity) {
            entity.setField("age", 11);
        }

        public void onCreate(final DataDefinition dataDefinition, final Entity entity) {
            entity.setField("name", "create");
        }

        public void onDelete(final DataDefinition dataDefinition, final Entity entity) {
            entity.setField("name", "delete");
        }

    }

}
