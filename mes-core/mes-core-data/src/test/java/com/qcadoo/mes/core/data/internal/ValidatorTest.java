package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class ValidatorTest {

    private final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private final DictionaryService dictionaryService = mock(DictionaryService.class);

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private FieldTypeFactory fieldTypeFactory = null;

    private FieldValidatorFactory fieldValidatorFactory = null;

    private EntityServiceImpl entityService = null;

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

    @Before
    public void init() {
        entityService = new EntityServiceImpl();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(entityService, "sessionFactory", sessionFactory);

        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);

        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataAccessService", dataAccessService);

        fieldValidatorFactory = new FieldValidatorFactoryImpl();
        ReflectionTestUtils.setField(fieldValidatorFactory, "applicationContext", applicationContext);

        BDDMockito.given(applicationContext.getBean("custom")).willReturn(new CustomValidateMethod());

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
    public void shouldHasNoErrorsIfAllFieldAreNotRequired() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
        assertTrue(validationResults.isValid());
    }

    @Test
    public void shouldBeRequiredIfHasRequiredValidator() throws Exception {
        // given
        fieldDefinitionName.setValidators(fieldValidatorFactory.required());

        // then
        assertTrue(fieldDefinitionName.isRequired());
    }

    @Test
    public void shouldNotBeRequiredIfDoesNotHasRequiredValidator() throws Exception {
        // then
        assertFalse(fieldDefinitionName.isRequired());
    }

    @Test
    public void shouldHasErrorIfIntegerTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", "21w");

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorIfBigDecimalTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "221.2w");

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorIfDateTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "2010-01-a");

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorIfBooleanTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "a");

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldBeUniqueIfHasUniqueValidator() throws Exception {
        // given
        fieldDefinitionName.setValidators(fieldValidatorFactory.unique());

        // then
        assertTrue(fieldDefinitionName.isUnique());
    }

    @Test
    public void shouldNotBeUniqueIfDoesNotHasUniqueValidator() throws Exception {
        // then
        assertFalse(fieldDefinitionName.isUnique());
    }

    @Test
    public void shouldHasErrorsIfRequiredFieldsAreNotSet() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "");
        entity.setField("age", null);

        fieldDefinitionName.setValidators(fieldValidatorFactory.required());
        fieldDefinitionAge.setValidators(fieldValidatorFactory.required());

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfStringValueIsTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwerty");

        fieldDefinitionName.setValidators(fieldValidatorFactory.maxLength(5));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfStringValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "ddd");

        fieldDefinitionName.setValidators(fieldValidatorFactory.range("a", "c"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfStringValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "bbb");

        fieldDefinitionName.setValidators(fieldValidatorFactory.range("a", "c"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfIntegerValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", "11");

        fieldDefinitionAge.setValidators(fieldValidatorFactory.range(null, 10));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfIntegerValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", 5);

        fieldDefinitionAge.setValidators(fieldValidatorFactory.range(4, null));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "31.22");

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.range(40, 50));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "31.22");

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.range(30, 40));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfDateValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.setValidators(fieldValidatorFactory.range(new Date(), new Date()));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfDateValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.setValidators(fieldValidatorFactory.range(null, new Date()));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoCheckRangeOfBoolean() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("retired", "false");

        fieldDefinitionRetired.setValidators(fieldValidatorFactory.range(true, true));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfIntegerValueIsTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", 123456);

        fieldDefinitionAge.setValidators(fieldValidatorFactory.maxLength(5));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalValueIsTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", new BigDecimal("123.456"));

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.maxLength(5));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValueLenghtIsOk() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", new BigDecimal("123.4"));

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.maxLength(5));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoCheckLenghtOfBoolean() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("retired", false);

        fieldDefinitionRetired.setValidators(fieldValidatorFactory.maxLength(0));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoCheckLenghtOfDate() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.setValidators(fieldValidatorFactory.maxLength(0));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfStringValueLenghtIsOk() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwert");

        fieldDefinitionName.setValidators(fieldValidatorFactory.maxLength(5));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorIfCustomValidatorReturnsTrue() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwerty");

        fieldDefinitionName.setValidators(fieldValidatorFactory.beanMethod("custom", "isEqualToQwerty"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession()).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHaveErrorIfCustomValidatorReturnsFalse() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwert");

        fieldDefinitionName.setValidators(fieldValidatorFactory.beanMethod("custom", "isEqualToQwerty"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHaveErrorIfCustomValidationMethodDoesNotExists() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwert");

        fieldDefinitionName.setValidators(fieldValidatorFactory.beanMethod("custom", "isEqualToQwerty"));

        // when
        ValidationResults validationResults = dataAccessService.save("simple.entity", entity);

        // then
        Mockito.verify(sessionFactory.getCurrentSession(), never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    public class CustomValidateMethod {

        public boolean isEqualToQwerty(final Object object) {
            return String.valueOf(object).equals("qwerty");
        }

    }

}
