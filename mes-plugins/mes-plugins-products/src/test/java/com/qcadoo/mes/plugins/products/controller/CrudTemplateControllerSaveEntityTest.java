package com.qcadoo.mes.plugins.products.controller;

import org.junit.Test;

public class CrudTemplateControllerSaveEntityTest {

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
    // TranslationServiceImpl translationService = new TranslationServiceImpl();
    // ReflectionTestUtils.setField(translationService, "messageSource", new MessageSourceMock());
    // ReflectionTestUtils.setField(controller, "translationService", translationService);
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
    // public void shouldPerformSaveEntity() {
    // // given
    // Entity entity = new Entity((long) 11);
    // entity.setField("testField1", "testField1Val");
    //
    // ValidationResults valRes = new ValidationResults();
    // valRes.setEntity(entity);
    //
    // given(dasMock.save(dataDefinition, entity)).willReturn(valRes);
    //
    // // when
    // ValidationResults vr = controller.saveEntity("testView", "testViewElement", entity, null);
    //
    // // then
    // assertEquals(new Long(11), vr.getEntity().getId());
    // assertEquals(1, vr.getEntity().getFields().size());
    // assertEquals("testField1Ok", vr.getEntity().getField("testField1"));
    // }
}