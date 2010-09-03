package com.qcadoo.mes.plugins.products.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public class EntityDataUtilsGenerateValidationResultTest {

    private DataDefinition dataDefinition;

    @Before
    public void init() {
        FieldDefinition f1 = mock(FieldDefinition.class);
        given(f1.getName()).willReturn("testField1");
        given(f1.getValue("testField1Val")).willReturn("testField1Ok");
        FieldDefinition f2 = mock(FieldDefinition.class);
        given(f2.getName()).willReturn("testField2");
        given(f2.getValue("testField2Val")).willReturn("testField2Ok");
        FieldDefinition f3 = mock(FieldDefinition.class);
        given(f3.getName()).willReturn("testField3");
        given(f3.getValue("testField3Val")).willReturn("testField3Ok");

        dataDefinition = new DataDefinition(null);
        dataDefinition.addField(f1);
        dataDefinition.addField(f2);
        dataDefinition.addField(f3);
    }

    @Test
    public void shouldDoNothingWhenEntityIsNull() {
        // given
        ValidationResults vr = new ValidationResults();
        vr.setEntity(null);

        // when
        ValidationResults resultVr = EntityDataUtils.generateValidationResultWithEntityData(vr, dataDefinition);

        // then
        assertEquals(resultVr, vr);
        assertNull(resultVr.getEntity());
    }

    @Test
    public void shouldGenerateEntityDataWhenEntityIsNotNull() {
        // given
        Entity entity = new Entity((long) 44);
        entity.setField("testField1", "testField1Val");
        entity.setField("testField2", "testField2Val");
        entity.setField("testField3", "testField3Val");

        ValidationResults vr = new ValidationResults();
        vr.setEntity(entity);

        // when
        ValidationResults resultVr = EntityDataUtils.generateValidationResultWithEntityData(vr, dataDefinition);

        // then
        assertEquals(new Long(44), resultVr.getEntity().getId());
        assertEquals(3, resultVr.getEntity().getFields().size());
        assertEquals("testField1Ok", resultVr.getEntity().getField("testField1"));
        assertEquals("testField2Ok", resultVr.getEntity().getField("testField2"));
        assertEquals("testField3Ok", resultVr.getEntity().getField("testField3"));
    }
}
