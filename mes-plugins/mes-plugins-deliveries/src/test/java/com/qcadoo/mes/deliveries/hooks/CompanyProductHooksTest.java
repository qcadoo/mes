package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.PRODUCTS;
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

public class CompanyProductHooksTest {

    private CompanyProductHooks companyProductHooks;

    @Mock
    private CompanyProductService companyProductService;

    @Mock
    private ProductService productService;

    @Mock
    private DataDefinition companyProductDD;

    @Mock
    private Entity companyProduct, product;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        companyProductHooks = new CompanyProductHooks();

        ReflectionTestUtils.setField(companyProductHooks, "companyProductService", companyProductService);
        ReflectionTestUtils.setField(companyProductHooks, "productService", productService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductIsParticularProduct() {
        // given
        given(companyProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PARTICULAR_PRODUCT)).willReturn(true);

        // when
        boolean result = companyProductHooks.checkIfProductIsParticularProduct(companyProductDD, companyProduct);

        // then
        assertTrue(result);

        verify(companyProduct, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductIsParticularProduct() {
        // given
        given(companyProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PARTICULAR_PRODUCT)).willReturn(false);

        // when
        boolean result = companyProductHooks.checkIfProductIsParticularProduct(companyProductDD, companyProduct);

        // then
        assertFalse(result);

        verify(companyProduct).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProduct, PRODUCT, COMPANY, PRODUCTS)).willReturn(true);

        // when
        boolean result = companyProductHooks.checkIfProductIsNotAlreadyUsed(companyProductDD, companyProduct);

        // then
        assertTrue(result);

        verify(companyProduct, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProduct, PRODUCT, COMPANY, PRODUCTS)).willReturn(false);

        // when
        boolean result = companyProductHooks.checkIfProductIsNotAlreadyUsed(companyProductDD, companyProduct);

        // then
        assertFalse(result);

        verify(companyProduct).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
