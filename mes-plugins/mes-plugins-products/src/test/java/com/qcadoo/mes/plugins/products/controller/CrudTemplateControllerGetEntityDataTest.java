package com.qcadoo.mes.plugins.products.controller;

import org.junit.Test;

public class CrudTemplateControllerGetEntityDataTest {

    @Test
    public void test() {

    }

    // private CrudController controller;
    //
    // private DataAccessService dasMock;
    //
    // private ViewDefinitionService vdsMock;
    //
    // private DataDefinition dataDefinition;
    //
    // @Before
    // public void init() {
    // controller = new CrudController();
    // dasMock = mock(DataAccessService.class);
    // ReflectionTestUtils.setField(controller, "dataAccessService", dasMock);
    //
    // vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
    // ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);
    //
    // DataFieldDefinition f1 = mock(DataFieldDefinition.class);
    // given(f1.getName()).willReturn("testField1");
    // given(f1.getValue("testField1Val")).willReturn("testField1Ok");
    //
    // dataDefinition = new DataDefinition("testEntityType");
    // dataDefinition.addField(f1);
    //
    // given(vdsMock.getViewDefinition("testView").getElementByName("testViewElement").getDataDefinition()).willReturn(
    // dataDefinition);
    // }
    //
    // @Test
    // public void shouldReturnEntityData() {
    // // given
    // Map<String, String> arguments = new HashMap<String, String>();
    // arguments.put("entityId", "22");
    //
    // Entity entity = new Entity((long) 22);
    // entity.setField("testField1", "testField1Val");
    //
    // given(dasMock.get(dataDefinition, (long) 22)).willReturn(entity);
    //
    // // when
    // Entity resultEntity = controller.getEntityData("testView", "testViewElement", arguments);
    //
    // // then
    // assertEquals(new Long(22), resultEntity.getId());
    // assertEquals(1, resultEntity.getFields().size());
    // assertEquals("testField1Ok", resultEntity.getField("testField1"));
    // }
}
