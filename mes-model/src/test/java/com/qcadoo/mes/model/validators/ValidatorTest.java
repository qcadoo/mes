/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.model.validators;

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

import com.qcadoo.mes.beans.sample.CustomEntityService;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.internal.DataAccessTest;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.api.EntityHookDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;
import com.qcadoo.model.internal.hooks.EntityHookDefinitionImpl;
import com.qcadoo.model.internal.hooks.FieldHookDefinitionImpl;
import com.qcadoo.model.internal.validators.CustomEntityValidator;
import com.qcadoo.model.internal.validators.CustomValidator;
import com.qcadoo.model.internal.validators.LengthValidator;
import com.qcadoo.model.internal.validators.PrecisionValidator;
import com.qcadoo.model.internal.validators.RangeValidator;
import com.qcadoo.model.internal.validators.RequiredValidator;
import com.qcadoo.model.internal.validators.ScaleValidator;
import com.qcadoo.model.internal.validators.UniqueValidator;

public class ValidatorTest extends DataAccessTest {

    @Before
    public void init() {
        given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());
    }

    @Test
    public void shouldHasNoErrorsIfAllFieldAreNotRequired() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", null);
        entity.setField("age", null);

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        assertTrue(entity.getErrors().isEmpty());
        assertTrue(entity.getGlobalErrors().isEmpty());
    }

    @Test
    public void shouldHasErrorMessage() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("age", "");

        fieldDefinitionAge.withValidator(initializeValidator(new RequiredValidator(), fieldDefinitionAge));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
        assertEquals(1, entity.getErrors().size());
        assertEquals("core.validate.field.error.missing", entity.getError("age").getMessage());
        assertEquals(0, entity.getGlobalErrors().size());
    }

    @Test
    public void shouldHasCustomErrorMessage() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("age", "");

        RequiredValidator requiredValidator = new RequiredValidator();
        requiredValidator.setErrorMessage("missing age");

        fieldDefinitionAge.withValidator(initializeValidator(requiredValidator, fieldDefinitionAge));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
        assertEquals(1, entity.getErrors().size());
        assertEquals("missing age", entity.getError("age").getMessage());
        assertEquals(0, entity.getGlobalErrors().size());
    }

    @Test
    public void shouldBeRequiredIfHasRequiredValidator() throws Exception {
        // given
        fieldDefinitionName.withValidator(new RequiredValidator());

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
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("age", "21w");

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorIfBigDecimalTypeIsWrong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", "221.2w");

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorIfDateTypeIsWrong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", "2010-01-a");

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorIfBooleanTypeIsWrong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", "a");

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldBeUniqueIfHasUniqueValidator() throws Exception {
        // given
        fieldDefinitionName.withValidator(new UniqueValidator());

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
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "");
        entity.setField("age", null);

        fieldDefinitionName.withValidator(initializeValidator(new RequiredValidator(), fieldDefinitionName));
        fieldDefinitionAge.withValidator(initializeValidator(new RequiredValidator(), fieldDefinitionAge));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfStringValueIsTooLong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "qwerty");

        fieldDefinitionName.withValidator(initializeValidator(new LengthValidator(null, null, 5), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfStringValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "ddd");

        fieldDefinitionName.withValidator(initializeValidator(new RangeValidator("a", "c", true), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfStringValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "bbb");

        fieldDefinitionName.withValidator(initializeValidator(new RangeValidator("a", "c", true), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfIntegerValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("age", "11");

        fieldDefinitionAge.withValidator(initializeValidator(new RangeValidator(null, 10, true), fieldDefinitionAge));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfIntegerValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("age", 5);

        fieldDefinitionAge.withValidator(initializeValidator(new RangeValidator(4, null, true), fieldDefinitionAge));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", "31.22");

        fieldDefinitionMoney.withValidator(initializeValidator(new RangeValidator(40, 50, true), fieldDefinitionMoney));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", "31.22");

        fieldDefinitionMoney.withValidator(initializeValidator(new RangeValidator(30, 40, true), fieldDefinitionMoney));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfDateValueIsOutsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.withValidator(initializeValidator(new RangeValidator(new Date(), new Date(), true),
                fieldDefinitionBirthDate));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfDateValueIsInsideTheRange() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.withValidator(initializeValidator(new RangeValidator(null, new Date(), true),
                fieldDefinitionBirthDate));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoCheckRangeOfBoolean() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("retired", "false");

        fieldDefinitionRetired.withValidator(initializeValidator(new RangeValidator(true, true, true), fieldDefinitionRetired));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfIntegerValueIsTooLong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("age", 123456);

        fieldDefinitionAge.withValidator(initializeValidator(new LengthValidator(null, null, 5), fieldDefinitionAge));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalValueIsTooLong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", new BigDecimal("123.456"));

        fieldDefinitionMoney.withValidator(initializeValidator(new LengthValidator(null, null, 5), fieldDefinitionMoney));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfBigDecimalPresicionAndScaleAreTooLong() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", new BigDecimal("123.456"));

        fieldDefinitionMoney.withValidator(initializeValidator(new ScaleValidator(null, null, 2), fieldDefinitionMoney))
                .withValidator(initializeValidator(new PrecisionValidator(null, null, 6), fieldDefinitionMoney));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValueLenghtIsOk() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", new BigDecimal("123.4"));

        fieldDefinitionMoney.withValidator(initializeValidator(new LengthValidator(null, null, 5), fieldDefinitionMoney));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfBigDecimalValuePresicionAndScaleIsOk() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("money", new BigDecimal("123.4"));

        fieldDefinitionMoney.withValidator(initializeValidator(new PrecisionValidator(null, null, 4), fieldDefinitionMoney))
                .withValidator(initializeValidator(new ScaleValidator(null, null, 1), fieldDefinitionMoney));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoCheckLenghtOfBoolean() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("retired", false);

        fieldDefinitionRetired.withValidator(initializeValidator(new LengthValidator(null, null, 0), fieldDefinitionRetired));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoCheckLenghtOfDate() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("birthDate", "2010-01-01");

        fieldDefinitionBirthDate.withValidator(initializeValidator(new LengthValidator(null, null, 0), fieldDefinitionBirthDate));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfStringValueLenghtIsOk() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "qwert");

        fieldDefinitionName.withValidator(initializeValidator(new LengthValidator(null, null, 5), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfFieldIsNotDuplicated() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "not existed");

        given(criteria.uniqueResult()).willReturn(0);

        fieldDefinitionName.withValidator(initializeValidator(new UniqueValidator(), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorsIfUpdatedFieldIsNotDuplicated() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition, 1L);
        entity.setField("name", "not existed");

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);

        given(session.get(SampleSimpleDatabaseObject.class, 1L)).willReturn(databaseObject);
        given(criteria.uniqueResult()).willReturn(0);

        fieldDefinitionName.withValidator(initializeValidator(new UniqueValidator(), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHasErrorsIfFieldIsDuplicated() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "existed");

        given(criteria.uniqueResult()).willReturn(1);

        fieldDefinitionName.withValidator(initializeValidator(new UniqueValidator(), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldHasNoErrorIfCustomValidatorReturnsTrue() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "qwerty");

        fieldDefinitionName.withValidator(initializeValidator(new CustomValidator(new FieldHookDefinitionImpl(
                CustomEntityService.class.getName(), "isEqualToQwerty", applicationContext)), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHaveErrorIfCustomValidatorReturnsFalse() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "qwert");

        fieldDefinitionName.withValidator(initializeValidator(new CustomValidator(new FieldHookDefinitionImpl(
                CustomEntityService.class.getName(), "isEqualToQwerty", applicationContext)), fieldDefinitionName));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
        assertEquals(1, entity.getErrors().size());
        assertEquals("core.validate.field.error.custom", entity.getError("name").getMessage());
        assertEquals(0, entity.getGlobalErrors().size());
    }

    @Test
    public void shouldHasNoErrorIfCustomEntityValidatorReturnsTrue() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "Mr T");
        entity.setField("age", "18");

        dataDefinition.addValidatorHook(new CustomEntityValidator(new EntityHookDefinitionImpl(CustomEntityService.class
                .getName(), "hasAge18AndNameMrT", applicationContext)));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldHaveErrorIfCustomEntityValidatorReturnsFalse() throws Exception {
        // given
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", "Mr");
        entity.setField("age", "18");

        dataDefinition.addValidatorHook(initializeValidator(new CustomEntityValidator(new EntityHookDefinitionImpl(
                CustomEntityService.class.getName(), "hasAge18AndNameMrT", applicationContext))));

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session, never()).save(any(SampleSimpleDatabaseObject.class));
        assertFalse(entity.isValid());
    }

    private FieldHookDefinition initializeValidator(final FieldHookDefinition fieldHook, final FieldDefinition fieldDefinition) {
        fieldHook.initialize(dataDefinition, fieldDefinition);
        return fieldHook;
    }

    private EntityHookDefinition initializeValidator(final EntityHookDefinition entityHook) {
        entityHook.initialize(dataDefinition);
        return entityHook;
    }

}
