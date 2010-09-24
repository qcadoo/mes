package com.qcadoo.mes.core;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.internal.model.FieldDefinitionImpl;
import com.qcadoo.mes.core.internal.types.IntegerType;
import com.qcadoo.mes.core.internal.types.StringType;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.utils.ExpressionUtil;
import com.qcadoo.mes.core.view.elements.grid.ColumnDefinition;

public class ExpressionUtilTest {

    @Test
    public void shouldReturnStringRepresentationOfOneFieldWithoutExpression() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "Mr T");

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("name").withType(new StringType());

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertEquals("Mr T", value);
    }

    @Test
    public void shouldReturnJoinedStringRepresentationsOfMultipleFieldWithoutExpression() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 33);
        entity.setField("sex", "F");

        FieldDefinition fieldDefinitionName = new FieldDefinitionImpl("name").withType(new StringType());
        FieldDefinition fieldDefinitionAge = new FieldDefinitionImpl("age").withType(new IntegerType());
        FieldDefinition fieldDefinitionSex = new FieldDefinitionImpl("sex").withType(new StringType());

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinitionName, fieldDefinitionAge, fieldDefinitionSex));

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertEquals("Mr T, 33, F", value);
    }

    @Test
    public void shouldGenerateValueOfTheSingleFieldColumn() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "Mr T");

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("name");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));
        columnDefinition.setExpression("#name.toUpperCase()");

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertEquals("MR T", value);
    }

    @Test
    public void shouldGenerateValueOfEmptyField() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", null);

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("name");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));
        columnDefinition.setExpression("#name");

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertNull(value);
    }

    @Test
    public void shouldGenerateValueOfTheMultiFieldColumn() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 33);
        entity.setField("sex", "F");

        FieldDefinition fieldDefinitionName = new FieldDefinitionImpl("name");
        FieldDefinition fieldDefinitionAge = new FieldDefinitionImpl("age");
        FieldDefinition fieldDefinitionSex = new FieldDefinitionImpl("sex");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinitionName, fieldDefinitionAge, fieldDefinitionSex));
        columnDefinition.setExpression("#name + \" -> (\" + (#age+1) + \") -> \" + (#sex == \"F\" ? \"female\" : \"male\")");

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertEquals("Mr T -> (34) -> female", value);
    }

    @Test
    public void shouldGenerateValueOfTheBelongsToColumn() throws Exception {
        // given
        Entity product = new Entity(1L);
        product.setField("name", "P1");

        Entity entity = new Entity(1L);
        entity.setField("product", product);

        FieldDefinition fieldDefinition = new FieldDefinitionImpl("product");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));
        columnDefinition.setExpression("#product['name']");

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertEquals("P1", value);
    }

}
