package com.qcadoo.mes.deliveries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class CompanyProductServiceImplTest {

    private CompanyProductService companyProductService;

    @Mock
    private Entity companyProduct, product, company;

    @Mock
    private EntityList hasMany;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> entities;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        companyProductService = new CompanyProductServiceImpl();

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public void shouldRetrunTrueWhenCheckIfProductIsNotUsedIfEntityIsSaved() {
        // given
        String belongsToProductName = "product";
        String belongsToCompanyName = "company";
        String hasManyName = "products";

        given(companyProduct.getId()).willReturn(1L);

        // when
        boolean result = companyProductService.checkIfProductIsNotUsed(companyProduct, belongsToProductName,
                belongsToCompanyName, hasManyName);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldRetrunTrueWhenCheckIfProductIsNotUsedIfProductIsNull() {
        // given
        String belongsToProductName = "product";
        String belongsToCompanyName = "company";
        String hasManyName = "products";

        given(companyProduct.getId()).willReturn(null);
        given(companyProduct.getBelongsToField(belongsToProductName)).willReturn(null);

        // when
        boolean result = companyProductService.checkIfProductIsNotUsed(companyProduct, belongsToProductName,
                belongsToCompanyName, hasManyName);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldRetrunTrueWhenCheckIfProductIsNotUsedIfCompanyIsNull() {
        // given
        String belongsToProductName = "product";
        String belongsToCompanyName = "company";
        String hasManyName = "products";

        given(companyProduct.getId()).willReturn(null);
        given(companyProduct.getBelongsToField(belongsToProductName)).willReturn(product);
        given(companyProduct.getBelongsToField(belongsToCompanyName)).willReturn(null);

        // when
        boolean result = companyProductService.checkIfProductIsNotUsed(companyProduct, belongsToProductName,
                belongsToCompanyName, hasManyName);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldRetrunTrueWhenCheckIfProductIsNotUsedIfIsntUsed() {
        // given
        String belongsToProductName = "product";
        String belongsToCompanyName = "company";
        String hasManyName = "products";

        given(companyProduct.getId()).willReturn(null);
        given(companyProduct.getBelongsToField(belongsToProductName)).willReturn(product);
        given(companyProduct.getBelongsToField(belongsToCompanyName)).willReturn(company);

        given(company.getHasManyField(hasManyName)).willReturn(hasMany);
        given(hasMany.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(SearchRestrictions.belongsTo(belongsToProductName, product))).willReturn(
                searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(entities);
        given(entities.isEmpty()).willReturn(true);

        // when
        boolean result = companyProductService.checkIfProductIsNotUsed(companyProduct, belongsToProductName,
                belongsToCompanyName, hasManyName);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldRetrunFalseWhenCheckIfProductIsNotUsedIfIsUsed() {
        // given
        String belongsToProductName = "product";
        String belongsToCompanyName = "company";
        String hasManyName = "products";

        given(companyProduct.getId()).willReturn(null);
        given(companyProduct.getBelongsToField(belongsToProductName)).willReturn(product);
        given(companyProduct.getBelongsToField(belongsToCompanyName)).willReturn(company);

        given(company.getHasManyField(hasManyName)).willReturn(hasMany);
        given(hasMany.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(SearchRestrictions.belongsTo(belongsToProductName, product))).willReturn(
                searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(entities);
        given(entities.isEmpty()).willReturn(false);

        // when
        boolean result = companyProductService.checkIfProductIsNotUsed(companyProduct, belongsToProductName,
                belongsToCompanyName, hasManyName);

        // then
        assertFalse(result);
    }
}
