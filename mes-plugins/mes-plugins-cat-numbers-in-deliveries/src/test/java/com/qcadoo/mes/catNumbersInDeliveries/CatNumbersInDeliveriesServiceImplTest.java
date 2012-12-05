package com.qcadoo.mes.catNumbersInDeliveries;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumbersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class CatNumbersInDeliveriesServiceImplTest {

    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition productCatalogNumbersDD;

    @Mock
    private Entity productCatalogNumbers, product, supplier;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        catNumbersInDeliveriesService = new CatNumbersInDeliveriesServiceImpl();

        PowerMockito.mockStatic(SearchRestrictions.class);

        ReflectionTestUtils.setField(catNumbersInDeliveriesService, "dataDefinitionService", dataDefinitionService);

        given(
                dataDefinitionService.get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS)).willReturn(productCatalogNumbersDD);
    }

    @Test
    public void shouldReturnNullWhenGetProductCatalogNumber() {
        // given
        given(productCatalogNumbersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        Entity result = catNumbersInDeliveriesService.getProductCatalogNumber(null, null);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnProductCatalogNumberWhenFetProductCatalogNumber() {
        // given
        given(productCatalogNumbersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(productCatalogNumbers);

        // when
        Entity result = catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier);

        // then
        assertEquals(productCatalogNumbers, result);
    }

}
