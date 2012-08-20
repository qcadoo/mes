package com.qcadoo.mes.productCatalogNumbers.hooks;

import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.CATALOG_NUMBER;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.PRODUCT;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.Assert;
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

import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields;
import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumbersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class ProductCatalogNumbersHooksTest {

    private ProductCatalogNumbersHooks productCatalogNumbersHooks;

    @Mock
    private Entity entity, product, company;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private SearchCriteriaBuilder criteria;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> productCatalogNumbers;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        productCatalogNumbersHooks = new ProductCatalogNumbersHooks();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(productCatalogNumbersHooks, "dataDefinitionService", dataDefinitionService);

        PowerMockito.mockStatic(SearchRestrictions.class);
        given(
                dataDefinitionService.get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS)).willReturn(dataDefinition);
        given(dataDefinition.find()).willReturn(criteria);

        given(entity.getBelongsToField(ProductCatalogNumberFields.COMPANY)).willReturn(company);
        given(entity.getBelongsToField(ProductCatalogNumberFields.PRODUCT)).willReturn(product);
    }

    @Test
    public void shouldReturnTrueWhenEntityWithGivenNumberAndCompanyDoesnotExistsInDB() throws Exception {
        // given
        SearchCriterion criterion1 = SearchRestrictions.eq(CATALOG_NUMBER, entity.getStringField(CATALOG_NUMBER));
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(ProductCatalogNumberFields.COMPANY, company);

        given(entity.getId()).willReturn(null);
        given(criteria.add(criterion1)).willReturn(criteria);
        given(criteria.add(criterion2)).willReturn(criteria);
        given(criteria.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(productCatalogNumbers);
        given(productCatalogNumbers.isEmpty()).willReturn(true);
        // when
        boolean result = productCatalogNumbersHooks.checkIfExistsCatalogNumberWithNumberAndCompany(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenEntityWithGivenNumberAndCompanyDoesExistsInDB() throws Exception {
        // given
        SearchCriterion criterion1 = SearchRestrictions.eq(CATALOG_NUMBER, entity.getStringField(CATALOG_NUMBER));
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(ProductCatalogNumberFields.COMPANY, company);

        given(entity.getId()).willReturn(null);
        given(criteria.add(criterion1)).willReturn(criteria);
        given(criteria.add(criterion2)).willReturn(criteria);
        given(criteria.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(productCatalogNumbers);
        given(productCatalogNumbers.isEmpty()).willReturn(false);
        // when
        boolean result = productCatalogNumbersHooks.checkIfExistsCatalogNumberWithNumberAndCompany(dataDefinition, entity);
        // then
        Assert.assertFalse(result);
        Mockito.verify(entity).addGlobalError(
                "productCatalogNumbers.productCatalogNumber.validationError.alreadyExistsCatalogNumerForCompany");
    }

    @Test
    public void shouldReturnTrueWhenEntityWithGivenProductAndCompanyDoesnotExistsInDB() throws Exception {
        // given
        SearchCriterion criterion1 = SearchRestrictions.eq(CATALOG_NUMBER, entity.getStringField(CATALOG_NUMBER));
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(PRODUCT, entity.getBelongsToField(PRODUCT));

        given(entity.getId()).willReturn(null);
        given(criteria.add(criterion1)).willReturn(criteria);
        given(criteria.add(criterion2)).willReturn(criteria);
        given(criteria.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(productCatalogNumbers);
        given(productCatalogNumbers.isEmpty()).willReturn(true);
        // when
        boolean result = productCatalogNumbersHooks.checkIfExistsCatalogNumberWithProductAndCompany(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenEntityWithGivenProductAndCompanyDoesExistsInDB() throws Exception {
        // given
        SearchCriterion criterion1 = SearchRestrictions.eq(CATALOG_NUMBER, entity.getStringField(CATALOG_NUMBER));
        SearchCriterion criterion2 = SearchRestrictions.belongsTo(PRODUCT, entity.getBelongsToField(PRODUCT));

        given(entity.getId()).willReturn(null);
        given(criteria.add(criterion1)).willReturn(criteria);
        given(criteria.add(criterion2)).willReturn(criteria);
        given(criteria.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(productCatalogNumbers);
        given(productCatalogNumbers.isEmpty()).willReturn(false);
        // when
        boolean result = productCatalogNumbersHooks.checkIfExistsCatalogNumberWithProductAndCompany(dataDefinition, entity);
        // then
        Assert.assertFalse(result);
        Mockito.verify(entity).addGlobalError(
                "productCatalogNumbers.productCatalogNumber.validationError.alreadyExistsProductForCompany");
    }
}
