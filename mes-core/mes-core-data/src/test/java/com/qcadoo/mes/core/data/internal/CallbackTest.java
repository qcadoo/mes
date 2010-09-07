package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class CallbackTest extends DataAccessTest {

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
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals(null, validationResults.getEntity().getField("name"));
        assertEquals(null, validationResults.getEntity().getField("age"));
    }

    @Test
    public void shouldCallOnCreateCallback() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getCallback("callback", "onCreate"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals("create", validationResults.getEntity().getField("name"));
        assertEquals(null, validationResults.getEntity().getField("age"));
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

        dataDefinition.setOnUpdate(callbackFactory.getCallback("callback", "onUpdate"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals("update", validationResults.getEntity().getField("name"));
        assertEquals(null, validationResults.getEntity().getField("age"));
    }

    @Test
    public void shouldCallAllDefinedCallbacksWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getCallback("callback", "onCreate"));
        dataDefinition.setOnSave(callbackFactory.getCallback("callback", "onSave"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals("create", validationResults.getEntity().getField("name"));
        assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
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

        dataDefinition.setOnSave(callbackFactory.getCallback("callback", "onSave"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals(null, validationResults.getEntity().getField("name"));
        assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
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

        dataDefinition.setOnUpdate(callbackFactory.getCallback("callback", "onUpdate"));
        dataDefinition.setOnSave(callbackFactory.getCallback("callback", "onSave"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals("update", validationResults.getEntity().getField("name"));
        assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
    }

    @Test
    public void shouldCallOnSaveCallbackWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getCallback("callback", "onSave"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        assertEquals(null, validationResults.getEntity().getField("name"));
        assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
    }

    public class CustomCallbackMethod {

        public void onUpdate(final Entity entity) {
            entity.setField("name", "update");
        }

        public void onSave(final Entity entity) {
            entity.setField("age", 11);
        }

        public void onCreate(final Entity entity) {
            entity.setField("name", "create");
        }

        public void onDelete(final Entity entity) {
            entity.setField("name", "delete");
        }

    }

}
