package com.qcadoo.mes.core.data.internal;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToFieldType;
import com.qcadoo.mes.core.data.internal.types.BooleanFieldType;
import com.qcadoo.mes.core.data.internal.types.DateFieldType;
import com.qcadoo.mes.core.data.internal.types.DictionaryFieldType;
import com.qcadoo.mes.core.data.internal.types.EnumFieldType;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.internal.types.NumericFieldType;
import com.qcadoo.mes.core.data.internal.types.StringFieldType;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class FieldTypeFactoryImplTest {

    private final DictionaryService dictionaryService = mock(DictionaryService.class);

    private FieldTypeFactory fieldTypeFactory = null;

    private ValidationResults validationResults = null;

    private final FieldDefinition fieldDefinition = new FieldDefinition("aa");

    @Before
    public void init() {
        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        validationResults = new ValidationResults();
    }

    @Test
    public void shouldReturnEnumType() throws Exception {
        // when
        EnumeratedFieldType fieldType = fieldTypeFactory.enumType("val1", "val2", "val3");

        // then
        Assert.assertThat(fieldType, is(EnumFieldType.class));
        Assert.assertThat(fieldType.values(), JUnitMatchers.hasItems("val1", "val2", "val3"));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(String.class, fieldType.getType());

        Assert.assertTrue(fieldType.validate(fieldDefinition, "val1", validationResults));
        Assert.assertFalse(fieldType.validate(fieldDefinition, "val4", validationResults));
        Assert.assertEquals("form.validate.errors.invalidDictionaryItem", validationResults.getErrorForField("aa").getMessage());
        Assert.assertEquals("[val1, val2, val3]", validationResults.getErrorForField("aa").getVars()[0]);
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_ENUM));
    }

    @Test
    public void shouldReturnDictionaryType() throws Exception {
        // given
        given(dictionaryService.values("dict")).willReturn(newArrayList("val1", "val2", "val3"));

        // when
        EnumeratedFieldType fieldType = fieldTypeFactory.dictionaryType("dict");

        // then
        Assert.assertThat(fieldType, is(DictionaryFieldType.class));
        Assert.assertThat(fieldType.values(), JUnitMatchers.hasItems("val1", "val2", "val3"));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(String.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, "val1", validationResults));
        Assert.assertFalse(fieldType.validate(fieldDefinition, "val4", validationResults));
        Assert.assertEquals("form.validate.errors.invalidDictionaryItem", validationResults.getErrorForField("aa").getMessage());
        Assert.assertEquals("[val1, val2, val3]", validationResults.getErrorForField("aa").getVars()[0]);
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DICTIONARY));
    }

    @Test
    public void shouldReturnBooleanType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.booleanType();

        // then
        Assert.assertThat(fieldType, is(BooleanFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(Boolean.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, false, validationResults));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_BOOLEAN));
    }

    @Test
    public void shouldReturnDateType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateType();

        // then
        Assert.assertThat(fieldType, is(DateFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(Date.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, new Date(), validationResults));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DATE));
    }

    @Test
    public void shouldReturnDateTimeType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateTimeType();

        // then
        Assert.assertThat(fieldType, is(DateFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(Date.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, new Date(), validationResults));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DATE_TIME));
    }

    @Test
    public void shouldReturnDecimalType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.decimalType();

        // then
        Assert.assertThat(fieldType, is(NumericFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertTrue(fieldType.isAggregable());
        Assert.assertEquals(BigDecimal.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1.21), validationResults));
        Assert.assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1), validationResults));
        Assert.assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1), validationResults));
        Assert.assertTrue(fieldType.validate(fieldDefinition, BigDecimal.valueOf(1234567), validationResults));
        Assert.assertFalse(fieldType.validate(fieldDefinition, BigDecimal.valueOf(12345678), validationResults));
        Assert.assertEquals("form.validate.errors.numericIsTooBig", validationResults.getErrorForField("aa").getMessage());
        Assert.assertEquals("9999999", validationResults.getErrorForField("aa").getVars()[0]);
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DECIMAL));
    }

    @Test
    public void shouldReturnIntegerType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.integerType();

        // then
        Assert.assertThat(fieldType, is(NumericFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertTrue(fieldType.isAggregable());
        Assert.assertEquals(Integer.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, 1, validationResults));
        Assert.assertTrue(fieldType.validate(fieldDefinition, 1234567890, validationResults));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_INTEGER));
    }

    @Test
    public void shouldReturnStringType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.stringType();

        // then
        Assert.assertThat(fieldType, is(StringFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(String.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, "test", validationResults));
        Assert.assertTrue(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 255), validationResults));
        Assert.assertFalse(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 256), validationResults));
        Assert.assertEquals("form.validate.errors.stringIsTooLong", validationResults.getErrorForField("aa").getMessage());
        Assert.assertEquals("255", validationResults.getErrorForField("aa").getVars()[0]);
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_STRING));
    }

    @Test
    public void shouldReturnTextType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.textType();

        // then
        Assert.assertThat(fieldType, is(StringFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(String.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, "test", validationResults));
        Assert.assertTrue(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 2048), validationResults));
        Assert.assertFalse(fieldType.validate(fieldDefinition, StringUtils.repeat("a", 2049), validationResults));
        Assert.assertEquals("form.validate.errors.stringIsTooLong", validationResults.getErrorForField("aa").getMessage());
        Assert.assertEquals("2048", validationResults.getErrorForField("aa").getVars()[0]);
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_TEXT));
    }

    @Test
    public void shouldReturnBelongToType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.eagerBelongsToType("entity", "field");

        // then
        Assert.assertThat(fieldType, is(BelongsToFieldType.class));
        Assert.assertFalse(fieldType.isSearchable());
        Assert.assertFalse(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertEquals(Object.class, fieldType.getType());
        Assert.assertTrue(fieldType.validate(fieldDefinition, new Entity(), validationResults));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO));
    }
}
