package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.plugins.products.data.mock.ResultSetMock;

public class ProductsListControllerGetListData {

    @Before
    public void setUp() {

    }

    @Test
    public void shouldReturnValidDataWhenNoOrder() {
        // given
        int maxRes = 10;
        int firstRes = 20;
        DataAccessService dasMock = mock(DataAccessService.class);
        ProductsListController controller = new ProductsListController(null, dasMock);

        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("products.product").withMaxResults(maxRes)
                .withFirstResult(firstRes).build();

        given(dasMock.find("products.product", searchCriteria)).willReturn(new ResultSetMock(searchCriteria));

        // when
        List<Entity> entities = controller.getProductsListData(maxRes, firstRes, null, null).getEntities();

        // then
        assertTrue(entities.size() == maxRes);
        for (int i = 0; i < maxRes; i++) {
            assertTrue(entities.get(i).getId() == firstRes + i);
        }
    }

    @Test
    public void shouldReturnValidDataWhenAscOrder() {
        // given
        int maxRes = 12;
        int firstRes = 4;
        String colName = "testCol2";
        DataAccessService dasMock = mock(DataAccessService.class);
        ProductsListController controller = new ProductsListController(null, dasMock);

        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("products.product").withMaxResults(maxRes)
                .withFirstResult(firstRes).orderBy(Order.asc(colName)).build();

        given(dasMock.find("products.product", searchCriteria)).willReturn(new ResultSetMock(searchCriteria));

        // when
        List<Entity> entities = controller.getProductsListData(maxRes, firstRes, colName, "asc").getEntities();

        // then
        assertTrue(entities.size() == maxRes);
        for (int i = 0; i < maxRes; i++) {
            assertTrue(entities.get(i).getId() == firstRes + i);
        }
    }

    @Test
    public void shouldReturnValidDataWhenDescOrder() {
        // given
        int maxRes = 40;
        int firstRes = 30;
        String colName = "testCol";
        DataAccessService dasMock = mock(DataAccessService.class);
        ProductsListController controller = new ProductsListController(null, dasMock);

        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("products.product").withMaxResults(maxRes)
                .withFirstResult(firstRes).orderBy(Order.desc(colName)).build();

        given(dasMock.find("products.product", searchCriteria)).willReturn(new ResultSetMock(searchCriteria));

        // when
        List<Entity> entities = controller.getProductsListData(maxRes, firstRes, colName, "desc").getEntities();

        // then
        assertTrue(entities.size() == maxRes);
        for (int i = 0; i < maxRes; i++) {
            assertTrue(entities.get(i).getId() == firstRes + i);
        }
    }

    @Test
    public void shouldThrowExceptionWhenIllegalArgument() {
        testIllegalArgument(-30, 10);
        testIllegalArgument(30, -10);
        testIllegalArgument(-30, -10);
    }

    private void testIllegalArgument(int s1, int s2) {
        // given
        ProductsListController controller = new ProductsListController(null, null);
        try {
            // when
            controller.getProductsListData(s1, s2, null, null);
            // then
            fail();
        } catch (Exception e) {
        }
    }
}
