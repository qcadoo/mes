package com.qcadoo.mes.crud.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.BDDMockito;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;

public class EntityDataUtilsGenerateEntityDataTest {

    @Test
    public void shouldReturnValidEntity() {
        // given
        Entity entity = new DefaultEntity((long) 44);
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

        DataDefinition dataDefinition = mock(DataDefinition.class);
        BDDMockito.given(dataDefinition.getField("testField1")).willReturn(f1);
        BDDMockito.given(dataDefinition.getField("testField2")).willReturn(f2);
        BDDMockito.given(dataDefinition.getField("testField3")).willReturn(f3);

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
