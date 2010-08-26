package com.qcadoo.mes.plugins.products.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.internal.definition.BelongsToFieldType;

public class ValidationUtilsTranslateEntity {

    @Test
    public void shouldTranslateWhenValid() {
        // given
        DataAccessService das = mock(DataAccessService.class);

        FieldTypeFactory fieldTypeFactory = new FieldTypeFactoryImpl();
        Entity entity = new Entity();
        List<FieldDefinition> fields = new LinkedList<FieldDefinition>();
        fields.add(createFieldDefinition("testBoolean", fieldTypeFactory.booleanType(), true));
        fields.add(createFieldDefinition("testDate", fieldTypeFactory.dateType(), true));
        fields.add(createFieldDefinition("testDateTime", fieldTypeFactory.dateTimeType(), true));
        fields.add(createFieldDefinition("testEnum", fieldTypeFactory.enumType("val1", "val2", "val3"), true));
        fields.add(createFieldDefinition("testInteger", fieldTypeFactory.integerType(), true));
        fields.add(createFieldDefinition("testDecimal", fieldTypeFactory.decimalType(), true));
        fields.add(createFieldDefinition("testString", fieldTypeFactory.stringType(), true));
        fields.add(createFieldDefinition("testText", fieldTypeFactory.textType(), true));
        fields.add(createFieldDefinition("testBelongsTo", new BelongsToFieldType("name", "field", false, das), true));

        entity.setId((long) 666);
        entity.setField("testBoolean", "true");
        entity.setField("testDate", "2010-12-10");
        entity.setField("testDateTime", "2010-12-10 4:51");
        entity.setField("testEnum", "val2");
        entity.setField("testInteger", "123");
        entity.setField("testDecimal", "123");
        entity.setField("testString", "ala ma kota");
        entity.setField("testText", "ala ma koty");
        entity.setField("testBelongsTo", "321");

        // when
        ValidationResult result = ValidationUtils.translateEntity(entity, fields);

        // then
        assertEquals(true, result.isValid());
        assertNull(result.getGlobalMessage());
        assertNull(result.getFieldMessages());

        Entity validEntity = result.getValidEntity();
        assertEquals(entity.getId(), validEntity.getId());
        assertTrue(validEntity.getField("testDate") instanceof Date);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse("2010-12-10");
            assertEquals(date, validEntity.getField("testDate"));
        } catch (Exception e) {
            fail();
        }
        assertTrue(validEntity.getField("testDate") instanceof Date);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse("2010-12-10");
            assertEquals(date, validEntity.getField("testDate"));
        } catch (Exception e) {
            fail();
        }
        assertTrue(validEntity.getField("testDateTime") instanceof Date);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = dateFormat.parse("2010-12-10 4:51");
            assertEquals(date, validEntity.getField("testDateTime"));
        } catch (Exception e) {
            fail();
        }
        assertTrue(validEntity.getField("testEnum") instanceof String);
        assertEquals("val2", validEntity.getField("testEnum"));
        assertTrue(validEntity.getField("testInteger") instanceof Integer);
        assertEquals(123, validEntity.getField("testInteger"));
        assertTrue(validEntity.getField("testDecimal") instanceof BigDecimal);
        assertEquals(new BigDecimal(123), validEntity.getField("testDecimal"));
        assertTrue(validEntity.getField("testString") instanceof String);
        assertEquals("ala ma kota", validEntity.getField("testString"));
        assertTrue(validEntity.getField("testText") instanceof String);
        assertEquals("ala ma koty", validEntity.getField("testText"));
        assertTrue(validEntity.getField("testBelongsTo") instanceof Entity);
        assertEquals(new Long(321), ((Entity) validEntity.getField("testBelongsTo")).getId());
    }

    @Test
    public void shouldFailWhenWrongData() {
        // given
        DataAccessService das = mock(DataAccessService.class);

        FieldTypeFactory fieldTypeFactory = new FieldTypeFactoryImpl();
        Entity entity = new Entity();
        List<FieldDefinition> fields = new LinkedList<FieldDefinition>();
        fields.add(createFieldDefinition("testBoolean", fieldTypeFactory.booleanType(), true));
        fields.add(createFieldDefinition("testDate", fieldTypeFactory.dateType(), true));
        fields.add(createFieldDefinition("testDateTime", fieldTypeFactory.dateTimeType(), true));
        fields.add(createFieldDefinition("testEnum", fieldTypeFactory.enumType("val1", "val2", "val3"), true));
        fields.add(createFieldDefinition("testInteger", fieldTypeFactory.integerType(), true));
        fields.add(createFieldDefinition("testDecimal", fieldTypeFactory.decimalType(), true));
        fields.add(createFieldDefinition("testBelongsTo", new BelongsToFieldType("name", "field", false, das), true));

        entity.setId((long) 666);
        entity.setField("testBoolean", "aa");
        entity.setField("testDate", "xx 2010-12-10");
        entity.setField("testDateTime", "2010-12as-10 4:51");
        entity.setField("testEnum", "val4");
        entity.setField("testInteger", "a123");
        entity.setField("testDecimal", "a123");
        entity.setField("testBelongsTo", "a321");

        // when
        ValidationResult result = ValidationUtils.translateEntity(entity, fields);

        // then
        assertEquals(false, result.isValid());
        assertNull(result.getValidEntity());
        assertEquals("wrongFieldTypesValidateMessage", result.getGlobalMessage());

        assertEquals("notDateValidateMessage", result.getFieldMessages().get("testDate"));
        assertEquals("notDateValidateMessage", result.getFieldMessages().get("testDateTime"));
        assertEquals("notInDictionaryValidateMessage", result.getFieldMessages().get("testEnum"));
        assertEquals("notIntegerValidateMessage", result.getFieldMessages().get("testInteger"));
        assertEquals("notBigDecimalValidateMessage", result.getFieldMessages().get("testDecimal"));
        assertEquals("notIdValidateMessage", result.getFieldMessages().get("testBelongsTo"));

    }

    private FieldDefinition createFieldDefinition(final String name, final FieldType type, final boolean required) {
        FieldDefinition fieldDefinition = new FieldDefinition(name);
        fieldDefinition.setType(type);
        fieldDefinition.setRequired(required);
        return fieldDefinition;
    }
}
