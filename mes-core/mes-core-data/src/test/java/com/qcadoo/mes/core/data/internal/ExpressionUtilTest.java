package com.qcadoo.mes.core.data.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.grid.ColumnDefinition;
import com.qcadoo.mes.core.data.internal.types.IntegerType;
import com.qcadoo.mes.core.data.internal.types.StringType;
import com.qcadoo.mes.core.data.internal.utils.ExpressionUtil;

public class ExpressionUtilTest {

    @Test
    public void shouldReturnStringRepresentationOfOneFieldWithoutExpression() throws Exception {
        // given
        Entity entity = new Entity(1L);
        entity.setField("name", "Mr T");

        DataFieldDefinition fieldDefinition = new DataFieldDefinition("name");
        fieldDefinition.withType(new StringType());

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

        DataFieldDefinition fieldDefinitionName = new DataFieldDefinition("name");
        fieldDefinitionName.withType(new StringType());
        DataFieldDefinition fieldDefinitionAge = new DataFieldDefinition("age");
        fieldDefinitionAge.withType(new IntegerType());
        DataFieldDefinition fieldDefinitionSex = new DataFieldDefinition("sex");
        fieldDefinitionSex.withType(new StringType());

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

        DataFieldDefinition fieldDefinition = new DataFieldDefinition("name");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));
        columnDefinition.setExpression("fields['name'].toUpperCase()");

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

        DataFieldDefinition fieldDefinition = new DataFieldDefinition("name");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));
        columnDefinition.setExpression("fields['name']");

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

        DataFieldDefinition fieldDefinitionName = new DataFieldDefinition("name");
        DataFieldDefinition fieldDefinitionAge = new DataFieldDefinition("age");
        DataFieldDefinition fieldDefinitionSex = new DataFieldDefinition("sex");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinitionName, fieldDefinitionAge, fieldDefinitionSex));
        columnDefinition
                .setExpression("fields['name'] + \" -> (\" + (fields['age']+1) + \") -> \" + (fields['sex'] == \"F\" ? \"female\" : \"male\")");

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

        DataFieldDefinition fieldDefinition = new DataFieldDefinition("product");

        ColumnDefinition columnDefinition = new ColumnDefinition("col");
        columnDefinition.setFields(Lists.newArrayList(fieldDefinition));
        columnDefinition.setExpression("fields['product'].fields['name']");

        // when
        String value = ExpressionUtil.getValue(entity, columnDefinition);

        // then
        assertEquals("P1", value);
    }

}
