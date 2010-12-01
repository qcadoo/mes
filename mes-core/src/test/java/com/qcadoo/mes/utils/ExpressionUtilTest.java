/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
