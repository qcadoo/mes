/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class ProductDetailsHooksTest {

    private static final String L_FORM = "form";

    private static final String L_SZT = "szt";

    private static final String L_EXTERNAL_NUMBER = "0001";

    private static final Long L_ID = 1L;

    private ProductDetailsHooks productDetailsHooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private UnitService unitService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productForm;

    @Mock
    private FieldComponent parentField, entityTypeField, unitField;

    @Mock
    private DataDefinition productDD;

    @Mock
    private Entity product;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productDetailsHooks = new ProductDetailsHooks();

        ReflectionTestUtils.setField(productDetailsHooks, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(productDetailsHooks, "unitService", unitService);
    }

    @Test
    public void shouldDisabledFormWhenExternalNumberIsNotNull() throws Exception {
        // given
        Long productId = 1L;

        given(view.getComponentByReference(L_FORM)).willReturn(productForm);
        given(view.getComponentByReference(ProductFields.PARENT)).willReturn(parentField);
        given(view.getComponentByReference(ProductFields.ENTITY_TYPE)).willReturn(entityTypeField);

        given(productForm.getEntityId()).willReturn(L_ID);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).willReturn(productDD);
        given(productDD.get(productId)).willReturn(product);

        given(product.getStringField(ProductFields.EXTERNAL_NUMBER)).willReturn(L_EXTERNAL_NUMBER);

        // when
        productDetailsHooks.disableProductFormForExternalItems(view);

        // then
        verify(productForm).setFormEnabled(false);
        verify(entityTypeField).setEnabled(true);
        verify(parentField).setEnabled(true);
    }

    @Test
    public void shouldntDisabledFormWhenExternalNumberIsNull() throws Exception {
        // given
        Long productId = 1L;

        given(view.getComponentByReference(L_FORM)).willReturn(productForm);
        given(view.getComponentByReference(ProductFields.PARENT)).willReturn(parentField);
        given(view.getComponentByReference(ProductFields.ENTITY_TYPE)).willReturn(entityTypeField);

        given(productForm.getEntityId()).willReturn(L_ID);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).willReturn(productDD);
        given(productDD.get(productId)).willReturn(product);

        given(product.getStringField(ProductFields.EXTERNAL_NUMBER)).willReturn(null);

        // when
        productDetailsHooks.disableProductFormForExternalItems(view);

        // then
        verify(productForm).setFormEnabled(true);
    }

    @Test
    public void shouldntFillUnitIfFormIsSaved() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(productForm);
        given(view.getComponentByReference(UNIT)).willReturn(unitField);

        given(productForm.getEntityId()).willReturn(L_ID);

        // when
        productDetailsHooks.fillUnit(view);

        // then
        verify(unitField, never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldFillUnitIfFormIsntSaved() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(productForm);
        given(view.getComponentByReference(UNIT)).willReturn(unitField);

        given(productForm.getEntityId()).willReturn(null);

        given(unitField.getFieldValue()).willReturn(null);

        given(unitService.getDefaultUnitFromSystemParameters()).willReturn(L_SZT);

        // when
        productDetailsHooks.fillUnit(view);

        // then
        verify(unitField).setFieldValue(L_SZT);
    }

}
