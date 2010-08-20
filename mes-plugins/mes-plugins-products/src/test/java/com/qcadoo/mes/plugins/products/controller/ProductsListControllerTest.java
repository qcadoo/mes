package com.qcadoo.mes.plugins.products.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.plugins.products.data.mock.DataAccessServiceMock;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;

public class ProductsListControllerTest extends TestCase {

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
    public void testProductsList() {

        ModelAndView modelAndView = controller.productsList(null);

        assertEquals("productsGridView", modelAndView.getViewName());

        assertNotNull(modelAndView.getModel().get("headerContent"));
        assertEquals("Produkty:", modelAndView.getModel().get("headerContent"));

        assertNotNull(modelAndView.getModel().get("gridDefinition"));
        assertEquals(GridDefinition.class, modelAndView.getModel().get("gridDefinition").getClass());
        assertEquals(dds.get("product").getGrids().get(0), modelAndView.getModel().get("gridDefinition"));
        assertEquals(dds.get("product").getGrids().get(0).getColumns(),
                ((GridDefinition) modelAndView.getModel().get("gridDefinition")).getColumns());

    }

    @Test
    public void testListData() {
        testListData(10, 20);
        testListData(20, 1);
        testListData(20, 0);
        testListData(1, 20);
        testListData(0, 20);
        testListData(0, 0);
        testListData(1, 1);
    }

    private void testListData(int maxResults, int firstResult) {
        List<Entity> entities = controller.getListData("" + maxResults, "" + firstResult).getEntities();
        assertTrue(entities.size() == maxResults);
        for (int i = 0; i < maxResults; i++) {
            assertTrue(entities.get(i).getId() == firstResult + i);
        }
    }

    @Test
    public void testListDataIllegalArgumentExceptionForTestListData() {
        assertNull(controller.getListData("-30", "10"));
        assertNull(controller.getListData("30", "-10"));
        assertNull(controller.getListData("-30", "-10"));
        assertNull(controller.getListData("3a0", "10"));
        assertNull(controller.getListData("30", "1a0"));
        assertNull(controller.getListData("3a0", "1a0"));
    }

    @Test
    public void testDeleteData() {
        DataAccessService dasMock = mock(DataAccessService.class);
        ProductsListController controller = new ProductsListController(null, dasMock);
        controller.setPrintException(false);

        String r1 = controller.deleteData(Arrays.asList(new String[] { "1", "2", "3" }));
        assertEquals("ok", r1);
        verify(dasMock).delete("product", (long) 1);
        verify(dasMock).delete("product", (long) 3);
        verify(dasMock).delete("product", (long) 2);
        verifyNoMoreInteractions(dasMock);
        reset(dasMock);

        String r2 = controller.deleteData(Arrays.asList(new String[] {}));
        assertEquals("ok", r2);
        verifyNoMoreInteractions(dasMock);
        reset(dasMock);

        String r3 = controller.deleteData(Arrays.asList(new String[] { "123" }));
        assertEquals("ok", r3);
        verify(dasMock).delete("product", (long) 123);
        verifyNoMoreInteractions(dasMock);
        reset(dasMock);

        String r4 = controller.deleteData(Arrays.asList(new String[] { "aa" }));
        assertEquals("error", r4);
        verifyNoMoreInteractions(dasMock);
        reset(dasMock);

        String r5 = controller.deleteData(Arrays.asList(new String[] { "1", "2", "a" }));
        assertEquals("error", r5);
        verifyNoMoreInteractions(dasMock);
        reset(dasMock);

    }
}
