package com.qcadoo.mes.core.data.internal;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.definition.BooleanFieldType;
import com.qcadoo.mes.core.data.internal.definition.DateFieldType;
import com.qcadoo.mes.core.data.internal.definition.DateTimeFieldType;
import com.qcadoo.mes.core.data.internal.definition.DictionaryFieldType;
import com.qcadoo.mes.core.data.internal.definition.EnumFieldType;
import com.qcadoo.mes.core.data.internal.definition.NumericFieldType;
import com.qcadoo.mes.core.data.internal.definition.StringFieldType;
import com.qcadoo.mes.core.data.internal.definition.TextFieldType;

public class FieldTypeFactoryImplTest {

    private DictionaryService dictionaryService = mock(DictionaryService.class);

    private FieldTypeFactory fieldTypeFactory = null;

    @Before
    public void init() {
        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
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
        Assert.assertTrue(fieldType.isValidType("val1"));
        Assert.assertFalse(fieldType.isValidType("val4"));
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
        Assert.assertTrue(fieldType.isValidType("val1"));
        Assert.assertFalse(fieldType.isValidType("val4"));
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
        Assert.assertTrue(fieldType.isValidType(false));
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
        Assert.assertTrue(fieldType.isValidType(new Date()));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_DATE));
    }

    @Test
    public void shouldReturnDateTimeType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateTimeType();

        // then
        Assert.assertThat(fieldType, is(DateTimeFieldType.class));
        Assert.assertTrue(fieldType.isSearchable());
        Assert.assertTrue(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertTrue(fieldType.isValidType(new Date()));
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
        Assert.assertTrue(fieldType.isValidType(1.21));
        Assert.assertTrue(fieldType.isValidType(1));
        Assert.assertTrue(fieldType.isValidType(1));
        Assert.assertTrue(fieldType.isValidType(1234567));
        Assert.assertFalse(fieldType.isValidType(12345678));
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
        Assert.assertTrue(fieldType.isValidType(1));
        Assert.assertTrue(fieldType.isValidType(1234567890L));
        Assert.assertFalse(fieldType.isValidType(12345678900L));
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
        Assert.assertTrue(fieldType.isValidType("test"));
        Assert.assertTrue(fieldType.isValidType(StringUtils.repeat("a", 255)));
        Assert.assertFalse(fieldType.isValidType(StringUtils.repeat("a", 256)));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_STRING));
    }

    @Test
    public void shouldReturnTextType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.textType();

        // then
        Assert.assertThat(fieldType, is(TextFieldType.class));
        Assert.assertFalse(fieldType.isSearchable());
        Assert.assertFalse(fieldType.isOrderable());
        Assert.assertFalse(fieldType.isAggregable());
        Assert.assertTrue(fieldType.isValidType("test"));
        Assert.assertTrue(fieldType.isValidType(StringUtils.repeat("a", 2048)));
        Assert.assertFalse(fieldType.isValidType(StringUtils.repeat("a", 2049)));
        Assert.assertThat(fieldType.getNumericType(), is(FieldTypeFactory.NUMERIC_TYPE_TEXT));
    }

}
