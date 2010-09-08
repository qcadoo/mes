package com.qcadoo.mes.core.data.internal;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToFieldType;
import com.qcadoo.mes.core.data.internal.types.BooleanFieldType;
import com.qcadoo.mes.core.data.internal.types.DateFieldType;
import com.qcadoo.mes.core.data.internal.types.DictionaryFieldType;
import com.qcadoo.mes.core.data.internal.types.EnumFieldType;
import com.qcadoo.mes.core.data.internal.types.NumericFieldType;
import com.qcadoo.mes.core.data.internal.types.PasswordFieldType;
import com.qcadoo.mes.core.data.internal.types.PriorityFieldType;
import com.qcadoo.mes.core.data.internal.types.StringFieldType;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class FieldTypeFactoryTest extends DataAccessTest {

    private ValidationResults validationResults = null;

    private final DataFieldDefinition fieldDefinition = new DataFieldDefinition("aa");

    @Before
    public void init() {
        validationResults = new ValidationResults();
    }

    @Test
    public void shouldReturnEnumType() throws Exception {
        // when
        EnumeratedFieldType fieldType = fieldTypeFactory.enumType("val1", "val2", "val3");

        // then
        assertThat(fieldType, is(EnumFieldType.class));
        assertThat(fieldType.values(), JUnitMatchers.hasItems("val1", "val2", "val3"));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());

        assertTrue(fieldType.validate(fieldDefinition, "val1", validationResults));
        assertFalse(fieldType.validate(fieldDefinition, "val4", validationResults));
        assertEquals("form.validate.errors.invalidDictionaryItem", validationResults.getErrorForField("aa").getMessage());
        assertEquals("[val1, val2, val3]", validationResults.getErrorForField("aa").getVars()[0]);
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_ENUM));
    }

    @Test
    public void shouldReturnDictionaryType() throws Exception {
        // given
        given(dictionaryService.values("dict")).willReturn(newArrayList("val1", "val2", "val3"));

        // when
        EnumeratedFieldType fieldType = fieldTypeFactory.dictionaryType("dict");

        // then
        assertThat(fieldType, is(DictionaryFieldType.class));
        assertThat(fieldType.values(), JUnitMatchers.hasItems("val1", "val2", "val3"));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, "val1", validationResults));
        assertFalse(fieldType.validate(fieldDefinition, "val4", validationResults));
        assertEquals("form.validate.errors.invalidDictionaryItem", validationResults.getErrorForField("aa").getMessage());
        assertEquals("[val1, val2, val3]", validationResults.getErrorForField("aa").getVars()[0]);
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DICTIONARY));
    }

    @Test
    public void shouldReturnBooleanType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.booleanType();

        // then
        assertThat(fieldType, is(BooleanFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Boolean.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, false, validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_BOOLEAN));
    }

    @Test
    public void shouldReturnDateType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateType();

        // then
        assertThat(fieldType, is(DateFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Date.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, new Date(), validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DATE));
    }

    @Test
    public void shouldReturnDateTimeType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateTimeType();

        // then
        assertThat(fieldType, is(DateFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Date.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, new Date(), validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DATE_TIME));
    }

    @Test
    public void shouldReturnDecimalType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.decimalType();

        // then
        assertThat(fieldType, is(NumericFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertTrue(fieldType.isAggregable());
        assertEquals(BigDecimal.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1.21), validationResults));
        assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1), validationResults));
        assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1), validationResults));
        assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1234567), validationResults));
        assertFalse(fieldType.validate(fieldDefinition, BigDecimal.valueOf(12345678), validationResults));
        assertEquals("form.validate.errors.numericIsTooBig", validationResults.getErrorForField("aa").getMessage());
        assertEquals("9999999", validationResults.getErrorForField("aa").getVars()[0]);
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DECIMAL));
    }

    @Test
    public void shouldReturnIntegerType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.integerType();

        // then
        assertThat(fieldType, is(NumericFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertTrue(fieldType.isAggregable());
        assertEquals(Integer.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, 1, validationResults));
        assertTrue(fieldType.validate(fieldDefinition, 1234567890, validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_INTEGER));
    }

    @Test
    public void shouldReturnStringType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.stringType();

        // then
        assertThat(fieldType, is(StringFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, "test", validationResults));
        assertTrue(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 255), validationResults));
        assertFalse(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 256), validationResults));
        assertEquals("form.validate.errors.stringIsTooLong", validationResults.getErrorForField("aa").getMessage());
        assertEquals("255", validationResults.getErrorForField("aa").getVars()[0]);
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_STRING));
    }

    @Test
    public void shouldReturnTextType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.textType();

        // then
        assertThat(fieldType, is(StringFieldType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, "test", validationResults));
        assertTrue(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 2048), validationResults));
        assertFalse(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 2049), validationResults));
        assertEquals("form.validate.errors.stringIsTooLong", validationResults.getErrorForField("aa").getMessage());
        assertEquals("2048", validationResults.getErrorForField("aa").getVars()[0]);
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_TEXT));
    }

    @Test
    public void shouldReturnBelongToType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.eagerBelongsToType("parent.entity", "name");

        // then
        assertThat(fieldType, is(BelongsToFieldType.class));
        assertFalse(fieldType.isSearchable());
        assertFalse(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Object.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, new Entity(), validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO));
    }

    @Test
    public void shouldReturnPasswordType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.passwordType();

        // then
        assertThat(fieldType, is(PasswordFieldType.class));
        assertFalse(fieldType.isSearchable());
        assertFalse(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, "", validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_PASSWORD));
    }

    @Test
    public void shouldReturnPriorityType() throws Exception {
        // given
        DataFieldDefinition fieldDefinition = new DataFieldDefinition("aaa");

        // when
        FieldType fieldType = fieldTypeFactory.priorityType(fieldDefinition);

        // then
        assertThat(fieldType, is(PriorityFieldType.class));
        assertFalse(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Integer.class, fieldType.getType());
        assertTrue(fieldType.validate(fieldDefinition, 1, validationResults));
        assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_PRIORITY));
        assertEquals(fieldDefinition, ((PriorityFieldType) fieldType).getScopeFieldDefinition());
    }
}
