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

package com.qcadoo.mes.model.types;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.internal.DataAccessTest;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.internal.FieldDefinitionImpl;
import com.qcadoo.mes.model.types.internal.BelongsToEntityType;
import com.qcadoo.mes.model.types.internal.BooleanType;
import com.qcadoo.mes.model.types.internal.DateTimeType;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.model.types.internal.DecimalType;
import com.qcadoo.mes.model.types.internal.IntegerType;
import com.qcadoo.mes.model.types.internal.PasswordType;
import com.qcadoo.mes.model.types.internal.PriorityType;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.model.types.internal.TextType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class FieldTypeFactoryTest extends DataAccessTest {

    private Entity entity = null;

    private final FieldDefinition fieldDefinition = new FieldDefinitionImpl(null, "aa");

    @Before
    public void init() {
        entity = new DefaultEntity(dataDefinition);
    }

    // TODO
    // @Test
    // public void shouldReturnEnumType() throws Exception {
    // // when
    // EnumeratedType fieldType = fieldTypeFactory.enumType("val1", "val2", "val3");
    //
    // // then
    // assertThat(fieldType, is(EnumType.class));
    //
    // assertThat(fieldType.values(), JUnitMatchers.hasItems("val1", "val2", "val3"));
    // assertTrue(fieldType.isSearchable());
    // assertTrue(fieldType.isOrderable());
    // assertFalse(fieldType.isAggregable());
    // assertEquals(String.class, fieldType.getType());
    //
    // assertNotNull(fieldType.toObject(fieldDefinition, "val1", entity));
    // assertNull(fieldType.toObject(fieldDefinition, "val4", entity));
    // assertEquals("core.validate.field.error.invalidDictionaryItem", entity.getError("aa").getMessage());
    // assertEquals("[val1, val2, val3]", entity.getError("aa").getVars()[0]);
    // }

    // TODO
    // @Test
    // public void shouldReturnDictionaryType() throws Exception {
    // // given
    // given(dictionaryService.values("dict")).willReturn(newArrayList("val1", "val2", "val3"));
    //
    // // when
    // EnumeratedType fieldType = fieldTypeFactory.dictionaryType("dict");
    //
    // // then
    // assertThat(fieldType, is(DictionaryType.class));
    // assertThat(fieldType.values(), JUnitMatchers.hasItems("val1", "val2", "val3"));
    // assertTrue(fieldType.isSearchable());
    // assertTrue(fieldType.isOrderable());
    // assertFalse(fieldType.isAggregable());
    // assertEquals(String.class, fieldType.getType());
    // assertNotNull(fieldType.toObject(fieldDefinition, "val1", entity));
    // assertNull(fieldType.toObject(fieldDefinition, "val4", entity));
    // assertEquals("core.validate.field.error.invalidDictionaryItem", entity.getError("aa").getMessage());
    // assertEquals("[val1, val2, val3]", entity.getError("aa").getVars()[0]);
    // }

    @Test
    public void shouldReturnBooleanType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.booleanType();

        // then
        assertThat(fieldType, is(BooleanType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Boolean.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, false, entity));
    }

    @Test
    public void shouldReturnDateType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateType();

        // then
        assertThat(fieldType, is(DateType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Date.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, new Date(), entity));
    }

    @Test
    public void shouldReturnDateTimeType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.dateTimeType();

        // then
        assertThat(fieldType, is(DateTimeType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Date.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, new Date(), entity));
    }

    @Test
    public void shouldReturnDecimalType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.decimalType();

        // then
        assertThat(fieldType, is(DecimalType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertTrue(fieldType.isAggregable());
        assertEquals(BigDecimal.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1.21), entity));
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1), entity));
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1), entity));
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1234567), entity));
    }

    @Test
    public void shouldReturnIntegerType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.integerType();

        // then
        assertThat(fieldType, is(IntegerType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertTrue(fieldType.isAggregable());
        assertEquals(Integer.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, 1, entity));
        assertNotNull(fieldType.toObject(fieldDefinition, 1234567890, entity));
    }

    @Test
    public void shouldReturnStringType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.stringType();

        // then
        assertThat(fieldType, is(StringType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, "test", entity));
        assertNotNull(fieldType.toObject(fieldDefinition, StringUtils.repeat("a", 255), entity));
        assertNull(fieldType.toObject(fieldDefinition, StringUtils.repeat("a", 256), entity));
        assertEquals("core.validate.field.error.invalidLength", entity.getError("aa").getMessage());
        assertEquals("255", entity.getError("aa").getVars()[0]);
    }

    @Test
    public void shouldReturnTextType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.textType();

        // then
        assertThat(fieldType, is(TextType.class));
        assertTrue(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, "test", entity));
        assertNotNull(fieldType.toObject(fieldDefinition, StringUtils.repeat("a", 2048), entity));
        assertNull(fieldType.toObject(fieldDefinition, StringUtils.repeat("a", 2049), entity));
        assertEquals("core.validate.field.error.invalidLength", entity.getError("aa").getMessage());
        assertEquals("2048", entity.getError("aa").getVars()[0]);
    }

    @Test
    public void shouldReturnBelongToType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.eagerBelongsToType("parent", "entity");

        // then
        assertThat(fieldType, is(BelongsToEntityType.class));
        assertFalse(fieldType.isSearchable());
        assertFalse(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Object.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, new DefaultEntity(dataDefinition), entity));
    }

    @Test
    public void shouldReturnPasswordType() throws Exception {
        // when
        FieldType fieldType = fieldTypeFactory.passwordType();

        // then
        assertThat(fieldType, is(PasswordType.class));
        assertFalse(fieldType.isSearchable());
        assertFalse(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(String.class, fieldType.getType());
    }

    @Test
    public void shouldReturnPriorityType() throws Exception {
        // given
        FieldDefinition fieldDefinition = new FieldDefinitionImpl(null, "aaa");

        // when
        FieldType fieldType = fieldTypeFactory.priorityType(fieldDefinition);

        // then
        assertThat(fieldType, is(PriorityType.class));
        assertFalse(fieldType.isSearchable());
        assertTrue(fieldType.isOrderable());
        assertFalse(fieldType.isAggregable());
        assertEquals(Integer.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, 1, entity));
        assertEquals(fieldDefinition, ((PriorityType) fieldType).getScopeFieldDefinition());
    }
}
