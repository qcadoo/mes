package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.plugins.products.data.mock.DataAccessServiceMock;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;

public class ProductsListControllerGetViewTest {

    private ProductsListController controller;

    private DataDefinitionService dds;

    private DataAccessService das;

    @Before
    public void setUp() {
        dds = new DataDefinitionServiceMock();
        das = new DataAccessServiceMock();
        controller = new ProductsListController(dds, das);
        controller.setPrintException(false);
    }

    @Test
    public void shouldReturnValidViewWhenNoMessage() {
        // given

        // when
        ModelAndView modelAndView = controller.getProductsListView(null);

        // then
        assertEquals("productsGridView", modelAndView.getViewName());

        assertNotNull(modelAndView.getModel().get("headerContent"));
        assertEquals("Produkty:", modelAndView.getModel().get("headerContent"));

        assertNull(modelAndView.getModel().get("message"));

        assertNotNull(modelAndView.getModel().get("gridDefinition"));
        assertEquals(GridDefinition.class, modelAndView.getModel().get("gridDefinition").getClass());
        assertEquals(dds.get("product").getGrids().get(0), modelAndView.getModel().get("gridDefinition"));
        assertEquals(dds.get("product").getGrids().get(0).getColumns(),
                ((GridDefinition) modelAndView.getModel().get("gridDefinition")).getColumns());
    }

    @Test
    public void shouldReturnValidViewWithMessage() {
        // given

        // when
        ModelAndView modelAndView = controller.getProductsListView("testMsg");

        // then
        assertNotNull(modelAndView.getModel().get("message"));
        assertEquals("testMsg", modelAndView.getModel().get("message"));
    }

}
