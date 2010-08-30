package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.plugins.products.data.mock.DataAccessServiceMock;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;

public class CrudTemplateControllerGetEntityListViewTest {

    private CRUD controller;

    private DataDefinitionService dds;

    private DataAccessService das;

    @Before
    public void setUp() {
        dds = new DataDefinitionServiceMock();
        das = new DataAccessServiceMock();
        controller = new CRUD();
        ReflectionTestUtils.setField(controller, "dataAccessService", das);
        ReflectionTestUtils.setField(controller, "dataDefinitionService", dds);
    }

    @Test
    public void shouldReturnValidViewWhenNoMessage() {
        // given

        // when
        ModelAndView modelAndView = controller.getEntityListView("productsGridView", "products.product", null);

        // then
        assertEquals("productsGridView", modelAndView.getViewName());

        assertNull(modelAndView.getModel().get("message"));

        assertNotNull(modelAndView.getModel().get("gridDefinition"));
        assertEquals(GridDefinition.class, modelAndView.getModel().get("gridDefinition").getClass());
        assertEquals(dds.get("products.product").getGrids().get(0), modelAndView.getModel().get("gridDefinition"));
        assertEquals(dds.get("products.product").getGrids().get(0).getColumns(),
                ((GridDefinition) modelAndView.getModel().get("gridDefinition")).getColumns());
    }

    @Test
    public void shouldReturnValidViewWithMessage() {
        // given

        // when
        ModelAndView modelAndView = controller.getEntityListView("productsGridView", "products.product", "testMsg");

        // then
        assertNotNull(modelAndView.getModel().get("message"));
        assertEquals("testMsg", modelAndView.getModel().get("message"));
    }

    private class CRUD extends CrudTemplate {

        public CRUD() {
            super(LoggerFactory.getLogger(CrudTemplateControllerGetEntityListViewTest.class));
        }
    }

}
