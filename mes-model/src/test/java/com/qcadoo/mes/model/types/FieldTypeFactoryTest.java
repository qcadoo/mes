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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.internal.DataAccessTest;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.security.PasswordEncoder;
import com.qcadoo.model.api.types.FieldType;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.FieldDefinitionImpl;
import com.qcadoo.model.internal.types.BelongsToEntityType;
import com.qcadoo.model.internal.types.BooleanType;
import com.qcadoo.model.internal.types.DateTimeType;
import com.qcadoo.model.internal.types.DateType;
import com.qcadoo.model.internal.types.DecimalType;
import com.qcadoo.model.internal.types.IntegerType;
import com.qcadoo.model.internal.types.PasswordType;
import com.qcadoo.model.internal.types.PriorityType;
import com.qcadoo.model.internal.types.StringType;
import com.qcadoo.model.internal.types.TextType;

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
        FieldType fieldType = new BooleanType();

        // then
        assertThat(fieldType, is(BooleanType.class));
        assertEquals(Boolean.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, false, entity));
    }

    @Test
    public void shouldReturnDateType() throws Exception {
        // when
        FieldType fieldType = new DateType();

        // then
        assertThat(fieldType, is(DateType.class));
        assertEquals(Date.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, new Date(), entity));
    }

    @Test
    public void shouldReturnDateTimeType() throws Exception {
        // when
        FieldType fieldType = new DateTimeType();

        // then
        assertThat(fieldType, is(DateTimeType.class));
        assertEquals(Date.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, new Date(), entity));
    }

    @Test
    public void shouldReturnDecimalType() throws Exception {
        // when
        FieldType fieldType = new DecimalType();

        // then
        assertThat(fieldType, is(DecimalType.class));
        assertEquals(BigDecimal.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1.21), entity));
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1), entity));
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1), entity));
        assertNotNull(fieldType.toObject(fieldDefinition, BigDecimal.valueOf(1234567), entity));
    }

    @Test
    public void shouldReturnIntegerType() throws Exception {
        // when
        FieldType fieldType = new IntegerType();

        // then
        assertThat(fieldType, is(IntegerType.class));
        assertEquals(Integer.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, 1, entity));
        assertNotNull(fieldType.toObject(fieldDefinition, 1234567890, entity));
    }

    @Test
    public void shouldReturnStringType() throws Exception {
        // when
        FieldType fieldType = new StringType();

        // then
        assertThat(fieldType, is(StringType.class));
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
        FieldType fieldType = new TextType();

        // then
        assertThat(fieldType, is(TextType.class));
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
        FieldType fieldType = new BelongsToEntityType("parent", "entity", dataDefinitionService, false);

        // then
        assertThat(fieldType, is(BelongsToEntityType.class));
        assertEquals(Object.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, new DefaultEntity(dataDefinition), entity));
    }

    @Test
    public void shouldReturnPasswordType() throws Exception {
        // when
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        FieldType fieldType = new PasswordType(passwordEncoder);

        // then
        assertThat(fieldType, is(PasswordType.class));
        assertEquals(String.class, fieldType.getType());
    }

    @Test
    public void shouldReturnPriorityType() throws Exception {
        // given
        FieldDefinition fieldDefinition = new FieldDefinitionImpl(null, "aaa");

        // when
        FieldType fieldType = new PriorityType(fieldDefinition);

        // then
        assertThat(fieldType, is(PriorityType.class));
        assertEquals(Integer.class, fieldType.getType());
        assertNotNull(fieldType.toObject(fieldDefinition, 1, entity));
        assertEquals(fieldDefinition, ((PriorityType) fieldType).getScopeFieldDefinition());
    }
}
