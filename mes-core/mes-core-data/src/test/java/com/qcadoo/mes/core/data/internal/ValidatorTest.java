package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.search.SearchResultImpl;
import com.qcadoo.mes.core.data.internal.search.restrictions.RestrictionOperator;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class ValidatorTest extends DataAccessTest {

    @Before
    public void init() {
        given(applicationContext.getBean("custom")).willReturn(new CustomValidateMethod());
        given(applicationContext.getBean("customEntity")).willReturn(new CustomEntityValidateMethod());
    }

    @Test
    public void shouldHasNoErrorsIfAllFieldAreNotRequired() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", null);
        entity.setField("age", null);

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
        assertTrue(validationResults.isValid());
        assertTrue(validationResults.getErrors().isEmpty());
        assertTrue(validationResults.getGlobalErrors().isEmpty());
    }

    @Test
    public void shouldHasErrorMessage() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", "");

        fieldDefinitionAge.setValidators(fieldValidatorFactory.required());

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
        assertEquals(1, validationResults.getErrors().size());
        assertEquals("core.validation.error.missing", validationResults.getErrorForField("age").getMessage());
        assertEquals(0, validationResults.getGlobalErrors().size());
    }

    @Test
    public void shouldHasCustomErrorMessage() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", "");

        fieldDefinitionAge.setValidators(fieldValidatorFactory.required().customErrorMessage("missing age"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
        assertEquals(1, validationResults.getErrors().size());
        assertEquals("missing age", validationResults.getErrorForField("age").getMessage());
        assertEquals(0, validationResults.getGlobalErrors().size());
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
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorIfBigDecimalTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "221.2w");

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorIfDateTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "2010-01-a");

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorIfBooleanTypeIsWrong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "a");

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
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
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfStringValueIsTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwerty");

        fieldDefinitionName.setValidators(fieldValidatorFactory.length(5));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfStringValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "ddd");

        fieldDefinitionName.setValidators(fieldValidatorFactory.range("a", "c"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfStringValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "bbb");

        fieldDefinitionName.setValidators(fieldValidatorFactory.range("a", "c"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfIntegerValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", "11");

        fieldDefinitionAge.setValidators(fieldValidatorFactory.range(null, 10));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfIntegerValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", 5);

        fieldDefinitionAge.setValidators(fieldValidatorFactory.range(4, null));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "31.22");

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.range(40, 50));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", "31.22");

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.range(30, 40));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfDateValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.setValidators(fieldValidatorFactory.range(new Date(), new Date()));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfDateValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.setValidators(fieldValidatorFactory.range(null, new Date()));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoCheckRangeOfBoolean() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("retired", "false");

        fieldDefinitionRetired.setValidators(fieldValidatorFactory.range(true, true));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfIntegerValueIsTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("age", 123456);

        fieldDefinitionAge.setValidators(fieldValidatorFactory.length(5));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalValueIsTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", new BigDecimal("123.456"));

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.length(5));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalPresicionAndScaleAreTooLong() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", new BigDecimal("123.456"));

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.precisionAndScale(6, 2));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValueLenghtIsOk() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", new BigDecimal("123.4"));

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.length(5));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValuePresicionAndScaleIsOk() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("money", new BigDecimal("123.4"));

        fieldDefinitionMoney.setValidators(fieldValidatorFactory.precisionAndScale(4, 1));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoCheckLenghtOfBoolean() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("retired", false);

        fieldDefinitionRetired.setValidators(fieldValidatorFactory.length(0));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoCheckLenghtOfDate() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.setValidators(fieldValidatorFactory.length(0));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfStringValueLenghtIsOk() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwert");

        fieldDefinitionName.setValidators(fieldValidatorFactory.length(5));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfFieldIsNotDuplicated() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "not existed");

        SearchResultImpl resultSet = new SearchResultImpl();
        resultSet.setTotalNumberOfEntities(0);

        given(
                dataAccessServiceMock.find(SearchCriteriaBuilder.forEntity(dataDefinition).withMaxResults(1)
                        .restrictedWith(Restrictions.eq(fieldDefinitionName, "not existed")).build())).willReturn(resultSet);

        fieldDefinitionName.setValidators(fieldValidatorFactory.unique());

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorsIfUpdatedFieldIsNotDuplicated() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "not existed");

        SimpleDatabaseObject databaseObject = new SimpleDatabaseObject(1L);

        SearchResultImpl resultSet = new SearchResultImpl();
        resultSet.setTotalNumberOfEntities(0);

        given(
                dataAccessServiceMock.find(SearchCriteriaBuilder.forEntity(dataDefinition).withMaxResults(1)
                        .restrictedWith(Restrictions.eq(fieldDefinitionName, "not existed"))
                        .restrictedWith(Restrictions.idRestriction(1L, RestrictionOperator.NE)).build())).willReturn(resultSet);

        given(criteria.uniqueResult()).willReturn(databaseObject);

        fieldDefinitionName.setValidators(fieldValidatorFactory.unique());

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHasErrorsIfFieldIsDuplicated() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "existed");

        SearchResultImpl resultSet = new SearchResultImpl();
        resultSet.setTotalNumberOfEntities(1);

        given(
                dataAccessServiceMock.find(SearchCriteriaBuilder.forEntity(dataDefinition).withMaxResults(1)
                        .restrictedWith(Restrictions.eq(fieldDefinitionName, "existed")).build())).willReturn(resultSet);

        fieldDefinitionName.setValidators(fieldValidatorFactory.unique());

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorIfCustomValidatorReturnsTrue() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwerty");

        fieldDefinitionName.setValidators(fieldValidatorFactory.custom("custom", "isEqualToQwerty"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHaveErrorIfCustomValidatorReturnsFalse() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwert");

        fieldDefinitionName.setValidators(fieldValidatorFactory.custom("custom", "isEqualToQwerty"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
        assertEquals(1, validationResults.getErrors().size());
        assertEquals("core.validation.error.custom", validationResults.getErrorForField("name").getMessage());
        assertEquals(0, validationResults.getGlobalErrors().size());
    }

    @Test
    public void shouldHaveErrorIfCustomValidationMethodDoesNotExists() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "qwerty");

        fieldDefinitionName.setValidators(fieldValidatorFactory.custom("custom", "isEqualToQwertz"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    @Test
    public void shouldHasNoErrorIfCustomEntityValidatorReturnsTrue() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", "18");

        dataDefinition.setValidators(fieldValidatorFactory.customEntity("customEntity", "hasAge18AndNameMrT"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session).save(any(SimpleDatabaseObject.class));
        assertFalse(validationResults.isNotValid());
    }

    @Test
    public void shouldHaveErrorIfCustomEntityValidatorReturnsFalse() throws Exception {
        // given
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr");
        entity.setField("age", "18");

        dataDefinition.setValidators(fieldValidatorFactory.customEntity("customEntity", "hasAge18AndNameMrT"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
        assertTrue(validationResults.getErrors().isEmpty());
        assertEquals(1, validationResults.getGlobalErrors().size());
        assertEquals("core.validation.error.customEntity", validationResults.getGlobalErrors().get(0).getMessage());
    }

    @Test
    public void shouldHaveErrorIfCustomEntityValidationMethodDoesNotExists() throws Exception {
        // given
        Entity entity = new Entity();
        entity.setField("name", "Mr T");
        entity.setField("age", "18");

        dataDefinition.setValidators(fieldValidatorFactory.customEntity("customEntity", "hasAge18AndNameMrX"));

        // when
        ValidationResults validationResults = dataAccessService.save(dataDefinition, entity);

        // then
        verify(session, never()).save(any(SimpleDatabaseObject.class));
        assertTrue(validationResults.isNotValid());
    }

    public class CustomValidateMethod {

        public boolean isEqualToQwerty(final Object object) {
            return String.valueOf(object).equals("qwerty");
        }

    }

    public class CustomEntityValidateMethod {

        public boolean hasAge18AndNameMrT(final Entity entity) {
            return (entity.getField("age").equals(18) && entity.getField("name").equals("Mr T"));
        }

    }

}
