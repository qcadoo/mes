package com.qcadoo.mes.crud.controller;

import org.junit.Test;

public class CrudTemplateControllerDeleteEntitiesTest {

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
    // dataDefinition = new DataDefinition("testEntityType");
    // given(vdsMock.getViewDefinition("testView").getElementByName("testViewElement").getDataDefinition()).willReturn(
    // dataDefinition);
    //
    // }
    //
    // @Test
    // public void shouldDeleteAndReturnOkWhenListOfOneData() {
    // // given
    //
    // // when
    // String r = controller.deleteEntities("testView", "testViewElement", Arrays.asList(new Integer[] { 123 }));
    //
    // // then
    // assertEquals("ok", r);
    // verify(dasMock).delete(dataDefinition, (long) 123);
    // verifyNoMoreInteractions(dasMock);
    // }
    //
    // @Test
    // public void shouldDeleteAndReturnOkWhenListOfThreeData() {
    // // given
    //
    // // when
    // String r = controller.deleteEntities("testView", "testViewElement", Arrays.asList(new Integer[] { 1, 2, 3 }));
    //
    // // then
    // assertEquals("ok", r);
    // verify(dasMock).delete(dataDefinition, (long) 1, (long) 2, (long) 3);
    // verifyNoMoreInteractions(dasMock);
    // }
    //
    // @Test
    // public void shouldDoNothingAndReturnOkWhenEmptyList() {
    // // given
    //
    // // when
    // String r = controller.deleteEntities("testView", "testViewElement", Arrays.asList(new Integer[] {}));
    //
    // // then
    // assertEquals("ok", r);
    // verifyNoMoreInteractions(dasMock);
    // }

}
