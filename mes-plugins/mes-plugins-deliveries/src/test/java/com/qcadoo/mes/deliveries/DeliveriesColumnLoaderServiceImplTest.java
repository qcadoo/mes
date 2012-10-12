package com.qcadoo.mes.deliveries;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class DeliveriesColumnLoaderServiceImplTest {

    private DeliveriesColumnLoaderService deliveriesColumnLoaderService;

    @Mock
    private DeliveriesService deliveriesService;

    @Mock
    private DataDefinition columnForDeliveriesDD, columnForOrdersDD;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesColumnLoaderService = new DeliveriesColumnLoaderServiceImpl();

        ReflectionTestUtils.setField(deliveriesColumnLoaderService, "deliveriesService", deliveriesService);

        given(deliveriesService.getColumnForDeliveriesDD()).willReturn(columnForDeliveriesDD);
        given(deliveriesService.getColumnForOrdersDD()).willReturn(columnForOrdersDD);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(columnForDeliveriesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(0);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(columnForDeliveriesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(1);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForOrdersEmpty() {
        // given
        given(columnForOrdersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(0);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForOrdersEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForOrdersEmpty() {
        // given
        given(columnForOrdersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(1);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForOrdersEmpty();

        // then
        assertFalse(result);
    }

}
