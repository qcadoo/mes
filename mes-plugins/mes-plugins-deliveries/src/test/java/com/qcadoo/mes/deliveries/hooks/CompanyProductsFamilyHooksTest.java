package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.PRODUCTS_FAMILIES;
import static com.qcadoo.mes.deliveries.constants.CompanyProductFields.COMPANY;
import static com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields.PRODUCT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class CompanyProductsFamilyHooksTest {

    private CompanyProductsFamilyHooks companyProductsFamilyHooks;

    @Mock
    private CompanyProductService companyProductService;

    @Mock
    private ProductService productService;

    @Mock
    private DataDefinition companyProductFamilyDD;

    @Mock
    private Entity companyProductsFamily, product;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        companyProductsFamilyHooks = new CompanyProductsFamilyHooks();

        ReflectionTestUtils.setField(companyProductsFamilyHooks, "companyProductService", companyProductService);
        ReflectionTestUtils.setField(companyProductsFamilyHooks, "productService", productService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductIsProductsFamily() {
        // given
        given(companyProductsFamily.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PRODUCTS_FAMILY)).willReturn(true);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductIsProductsFamily(companyProductFamilyDD, companyProductsFamily);

        // then
        assertTrue(result);

        verify(companyProductsFamily, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductIsProductsFamily() {
        // given
        given(companyProductsFamily.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PRODUCTS_FAMILY)).willReturn(false);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductIsProductsFamily(companyProductFamilyDD, companyProductsFamily);

        // then
        assertFalse(result);

        verify(companyProductsFamily).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductsFamilyIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProductsFamily, PRODUCT, COMPANY, PRODUCTS_FAMILIES))
                .willReturn(true);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductsFamilyIsNotAlreadyUsed(companyProductFamilyDD,
                companyProductsFamily);

        // then
        assertTrue(result);

        verify(companyProductsFamily, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductsFamilyIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProductsFamily, PRODUCT, COMPANY, PRODUCTS_FAMILIES))
                .willReturn(false);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductsFamilyIsNotAlreadyUsed(companyProductFamilyDD,
                companyProductsFamily);

        // then
        assertFalse(result);

        verify(companyProductsFamily).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }
}
