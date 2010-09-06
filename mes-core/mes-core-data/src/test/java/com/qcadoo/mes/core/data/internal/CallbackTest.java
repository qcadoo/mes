package com.qcadoo.mes.core.data.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import junit.framework.Assert;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class CallbackTest {

    private final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private final DictionaryService dictionaryService = mock(DictionaryService.class);

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final DataAccessService dataAccessServiceMock = mock(DataAccessService.class);

    private FieldTypeFactory fieldTypeFactory = null;

    private final FieldValidatorFactory fieldValidatorFactory = null;

    private EntityService entityService = null;

    private ValidationService validationService = null;

    private DataAccessService dataAccessService = null;

    private DataDefinition parentDataDefinition = null;

    private DataDefinition dataDefinition = null;

    private FieldDefinition fieldDefinitionAge = null;

    private FieldDefinition fieldDefinitionMoney = null;

    private FieldDefinition fieldDefinitionRetired = null;

    private FieldDefinition fieldDefinitionBirthDate = null;

    private FieldDefinition fieldDefinitionName = null;

    private FieldDefinition fieldDefinitionBelongsTo = null;

    private FieldDefinition parentFieldDefinitionName = null;

    private CallbackFactory callbackFactory;

    @Before
    public void init() {
        validationService = new ValidationService();
        ReflectionTestUtils.setField(validationService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(validationService, "dataDefinitionService", dataDefinitionService);

        entityService = new EntityService();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(entityService, "validationService", validationService);

        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);

        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataAccessService", dataAccessService);

        callbackFactory = new CallbackFactory();
        ReflectionTestUtils.setField(callbackFactory, "applicationContext", applicationContext);

        given(applicationContext.getBean("callback")).willReturn(new CustomCallbackMethod());

        parentFieldDefinitionName = new FieldDefinition("name");
        parentFieldDefinitionName.setType(fieldTypeFactory.stringType());
        parentFieldDefinitionName.setValidators();

        fieldDefinitionBelongsTo = new FieldDefinition("belongsTo");
        fieldDefinitionBelongsTo.setType(fieldTypeFactory.eagerBelongsToType("parent.entity", "name"));
        fieldDefinitionBelongsTo.setValidators();

        fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitionName.setValidators();

        fieldDefinitionAge = new FieldDefinition("age");
        fieldDefinitionAge.setType(fieldTypeFactory.integerType());
        fieldDefinitionAge.setValidators();

        fieldDefinitionMoney = new FieldDefinition("money");
        fieldDefinitionMoney.setType(fieldTypeFactory.decimalType());
        fieldDefinitionMoney.setValidators();

        fieldDefinitionRetired = new FieldDefinition("retired");
        fieldDefinitionRetired.setType(fieldTypeFactory.booleanType());
        fieldDefinitionRetired.setValidators();

        fieldDefinitionBirthDate = new FieldDefinition("birthDate");
        fieldDefinitionBirthDate.setType(fieldTypeFactory.dateType());
        fieldDefinitionBirthDate.setValidators();

        parentDataDefinition = new DataDefinition("parent.entity");
        parentDataDefinition.addField(parentFieldDefinitionName);
        parentDataDefinition.setFullyQualifiedClassName(ParentDatabaseObject.class.getCanonicalName());

        dataDefinition = new DataDefinition("simple.entity");
        dataDefinition.addField(fieldDefinitionName);
        dataDefinition.addField(fieldDefinitionAge);
        dataDefinition.addField(fieldDefinitionMoney);
        dataDefinition.addField(fieldDefinitionRetired);
        dataDefinition.addField(fieldDefinitionBirthDate);
        dataDefinition.addField(fieldDefinitionBelongsTo);
        dataDefinition.setFullyQualifiedClassName(SimpleDatabaseObject.class.getCanonicalName());

        given(dataDefinitionService.get("simple.entity")).willReturn(dataDefinition);

        given(dataDefinitionService.get("parent.entity")).willReturn(parentDataDefinition);
    }

    @Test
    public void shouldNotCallAnyCallbackIfNotDefined() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals(null, validationResults.getEntity().getField("name"));
        Assert.assertEquals(null, validationResults.getEntity().getField("age"));
    }

    @Test
    public void shouldCallOnCreateCallback() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getCallback("callback", "onCreate"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals("create", validationResults.getEntity().getField("name"));
        Assert.assertEquals(null, validationResults.getEntity().getField("age"));
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
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals("update", validationResults.getEntity().getField("name"));
        Assert.assertEquals(null, validationResults.getEntity().getField("age"));
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
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals("create", validationResults.getEntity().getField("name"));
        Assert.assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
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
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals(null, validationResults.getEntity().getField("name"));
        Assert.assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
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
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals("update", validationResults.getEntity().getField("name"));
        Assert.assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
    }

    @Test
    public void shouldCallOnSaveCallbackWhileCreating() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.setOnCreate(callbackFactory.getCallback("callback", "onSave"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Assert.assertEquals(null, validationResults.getEntity().getField("name"));
        Assert.assertEquals(Integer.valueOf(11), validationResults.getEntity().getField("age"));
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
