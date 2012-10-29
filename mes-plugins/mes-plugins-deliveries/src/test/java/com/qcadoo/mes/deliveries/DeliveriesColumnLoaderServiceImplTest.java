package com.qcadoo.mes.deliveries;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class DeliveriesColumnLoaderServiceImplTest {

    private DeliveriesColumnLoaderService deliveriesColumnLoaderService;

    @Mock
    private ColumnExtensionService columnExtensionService;

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

        ReflectionTestUtils.setField(deliveriesColumnLoaderService, "columnExtensionService", columnExtensionService);
        ReflectionTestUtils.setField(deliveriesColumnLoaderService, "deliveriesService", deliveriesService);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES)).willReturn(true);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES)).willReturn(false);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForOrdersEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS)).willReturn(true);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForOrdersEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForOrdersEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS)).willReturn(false);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForOrdersEmpty();

        // then
        assertFalse(result);
    }

}
