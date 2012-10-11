package com.qcadoo.mes.deliveries;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class DeliveriesColumnLoaderServiceImplTest {

    private DeliveriesColumnLoaderServiceImpl deliveriesColumnLoaderServiceImpl;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition columnForDeliveriesDD, columnForOrdersDD;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesColumnLoaderServiceImpl = new DeliveriesColumnLoaderServiceImpl();

        ReflectionTestUtils.setField(deliveriesColumnLoaderServiceImpl, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES))
                .willReturn(columnForDeliveriesDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS))
                .willReturn(columnForOrdersDD);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(columnForDeliveriesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(0);

        // when
        boolean result = deliveriesColumnLoaderServiceImpl.isColumnsForDeliveriesEmpty();

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
        boolean result = deliveriesColumnLoaderServiceImpl.isColumnsForDeliveriesEmpty();

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
        boolean result = deliveriesColumnLoaderServiceImpl.isColumnsForOrdersEmpty();

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
        boolean result = deliveriesColumnLoaderServiceImpl.isColumnsForOrdersEmpty();

        // then
        assertFalse(result);
    }

}
