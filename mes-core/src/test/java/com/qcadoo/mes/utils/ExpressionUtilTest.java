/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.internal.FieldDefinitionImpl;
import com.qcadoo.mes.model.types.internal.IntegerType;
import com.qcadoo.mes.model.types.internal.StringType;

public class ExpressionUtilTest {

    @Test
    public void shouldReturnStringRepresentationOfOneFieldWithoutExpression() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);
        entity.setField("name", "Mr T");

        FieldDefinition fieldDefinition = new FieldDefinitionImpl(null, "name").withType(new StringType());

        // when
        String value = ExpressionUtil.getValue(entity, Lists.newArrayList(fieldDefinition), null);

        // then
        assertEquals("Mr T", value);
    }

    @Test
    public void shouldReturnJoinedStringRepresentationsOfMultipleFieldWithoutExpression() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 33);
        entity.setField("sex", "F");

        FieldDefinition fieldDefinitionName = new FieldDefinitionImpl(null, "name").withType(new StringType());
        FieldDefinition fieldDefinitionAge = new FieldDefinitionImpl(null, "age").withType(new IntegerType());
        FieldDefinition fieldDefinitionSex = new FieldDefinitionImpl(null, "sex").withType(new StringType());

        // when
        String value = ExpressionUtil.getValue(entity,
                Lists.newArrayList(fieldDefinitionName, fieldDefinitionAge, fieldDefinitionSex), null);

        // then
        assertEquals("Mr T, 33, F", value);
    }

    @Test
    public void shouldGenerateValueOfTheSingleFieldColumn() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);
        entity.setField("name", "Mr T");

        // when
        String value = ExpressionUtil.getValue(entity, "#name.toUpperCase()", null);

        // then
        assertEquals("MR T", value);
    }

    @Test
    public void shouldGenerateValueOfEmptyField() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);
        entity.setField("name", null);

        // when
        String value = ExpressionUtil.getValue(entity, "#name", null);

        // then
        assertNull(value);
    }

    @Test
    public void shouldGenerateValueOfTheMultiFieldColumn() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 33);
        entity.setField("sex", "F");

        // when
        String value = ExpressionUtil.getValue(entity,
                "#name + \" -> (\" + (#age+1) + \") -> \" + (#sex == \"F\" ? \"female\" : \"male\")", null);

        // then
        assertEquals("Mr T -> (34) -> female", value);
    }

    @Test
    public void shouldGenerateValueOfTheBelongsToColumn() throws Exception {
        // given
        Entity product = new DefaultEntity("", "", 1L);
        product.setField("name", "P1");

        Entity entity = new DefaultEntity("", "", 1L);
        entity.setField("product", product);

        // when
        String value = ExpressionUtil.getValue(entity, "#product['name']", null);

        // then
        assertEquals("P1", value);
    }

}
