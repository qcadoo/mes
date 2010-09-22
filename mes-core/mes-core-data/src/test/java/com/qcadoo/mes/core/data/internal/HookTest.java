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
        given(applicationContext.getBean("hook")).willReturn(new CustomHookMethod());
    }

    @Test
    public void shouldNotCallAnyHookIfNotDefined() throws Exception {
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
    public void shouldCallOnCreateHook() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setCreateHook(hookFactory.getHook("hook", "onCreate"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("create", entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallOnUpdateHook() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(databaseObject);

        dataDefinition.setUpdateHook(hookFactory.getHook("hook", "onUpdate"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("update", entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallAllDefinedHooksWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setCreateHook(hookFactory.getHook("hook", "onCreate"));
        dataDefinition.setSaveHook(hookFactory.getHook("hook", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("create", entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallOnSaveHookWhileUpdating() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(databaseObject);

        dataDefinition.setSaveHook(hookFactory.getHook("hook", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallAllDefinedHooksWhileUpdating() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        given(
                sessionFactory.getCurrentSession().createCriteria(SimpleDatabaseObject.class).add(Mockito.any(Criterion.class))
                        .add(Mockito.any(Criterion.class)).uniqueResult()).willReturn(databaseObject);

        dataDefinition.setUpdateHook(hookFactory.getHook("hook", "onUpdate"));
        dataDefinition.setSaveHook(hookFactory.getHook("hook", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("update", entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallOnSaveHookWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setCreateHook(hookFactory.getHook("hook", "onSave"));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    public class CustomHookMethod {

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
