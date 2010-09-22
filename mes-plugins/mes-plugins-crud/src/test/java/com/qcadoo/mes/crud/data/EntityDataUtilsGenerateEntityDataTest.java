package com.qcadoo.mes.crud.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.model.DataDefinitionImpl;
import com.qcadoo.mes.core.data.model.FieldDefinition;

public class EntityDataUtilsGenerateEntityDataTest {

    @Test
    public void shouldReturnValidEntity() {
        // given
        Entity entity = new Entity((long) 44);
        entity.setField("testField1", "testField1Val");
        entity.setField("testField2", "testField2Val");
        entity.setField("testField3", "testField3Val");

        FieldDefinition f1 = mock(FieldDefinition.class);
        given(f1.getName()).willReturn("testField1");
        given(f1.getValue("testField1Val")).willReturn("testField1Ok");
        FieldDefinition f2 = mock(FieldDefinition.class);
        given(f2.getName()).willReturn("testField2");
        given(f2.getValue("testField2Val")).willReturn("testField2Ok");
        FieldDefinition f3 = mock(FieldDefinition.class);
        given(f3.getName()).willReturn("testField3");
        given(f3.getValue("testField3Val")).willReturn("testField3Ok");

        DataDefinitionImpl dataDefinition = new DataDefinitionImpl(null, null);
        dataDefinition.addField(f1);
        dataDefinition.addField(f2);
        dataDefinition.addField(f3);

        // when
        Entity stringEntity = EntityDataUtils.generateEntityData(entity, dataDefinition);

        // then
        assertEquals(new Long(44), stringEntity.getId());
        assertEquals(3, stringEntity.getFields().size());
        assertEquals("testField1Ok", stringEntity.getField("testField1"));
        assertEquals("testField2Ok", stringEntity.getField("testField2"));
        assertEquals("testField3Ok", stringEntity.getField("testField3"));
    }
}
