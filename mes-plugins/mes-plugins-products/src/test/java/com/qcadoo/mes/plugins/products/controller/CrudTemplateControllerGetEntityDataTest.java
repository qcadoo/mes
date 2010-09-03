package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public class CrudTemplateControllerGetEntityDataTest {

    private CrudController controller;

    private DataAccessService dasMock;

    private ViewDefinitionService vdsMock;

    @Before
    public void init() {
        controller = new CrudController();
        dasMock = mock(DataAccessService.class);
        ReflectionTestUtils.setField(controller, "dataAccessService", dasMock);

        vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);

        FieldDefinition f1 = mock(FieldDefinition.class);
        given(f1.getName()).willReturn("testField1");
        given(f1.getValue("testField1Val")).willReturn("testField1Ok");

        DataDefinition dataDefinition = new DataDefinition("testEntityType");
        dataDefinition.addField(f1);

        given(vdsMock.getViewDefinition("testView").getElementByName("testViewElement").getDataDefinition()).willReturn(
                dataDefinition);
    }

    @Test
    public void shouldReturnEntityData() {
        // given
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("entityId", "22");

        Entity entity = new Entity((long) 22);
        entity.setField("testField1", "testField1Val");

        given(dasMock.get("testEntityType", (long) 22)).willReturn(entity);

        // when
        Entity resultEntity = controller.getEntityData("testView", "testViewElement", arguments);

        // then
        assertEquals(new Long(22), resultEntity.getId());
        assertEquals(1, resultEntity.getFields().size());
        assertEquals("testField1Ok", resultEntity.getField("testField1"));
    }
}
