/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.technologies.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class ProductDetailsListenersTTest {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_TECHNOLOGY_GROUP_NUMBER = "000001";

    private static final String L_PRODUCT_NAME = "ProductName";

    private static final long L_ID = 1L;

    private ProductDetailsListenersT productDetailsListenersT;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productForm;

    @Mock
    private Entity product, technologyGroup;

    private Map<String, Object> parameters = Maps.newHashMap();

    private Map<String, String> filters = Maps.newHashMap();

    private Map<String, Object> gridOptions = Maps.newHashMap();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productDetailsListenersT = new ProductDetailsListenersT();

        given(view.getComponentByReference("form")).willReturn(productForm);
        given(productForm.getEntity()).willReturn(product);
    }

    @Test
    public void shouldntAddTechnologyGroupIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/technologies/technologyGroupDetails.html";

        // when
        productDetailsListenersT.addTechnologyGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldAddTechnologyGroupIfProductIsSaved() {
        // given
        given(product.getId()).willReturn(L_ID);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologyGroups");

        String url = "../page/technologies/technologyGroupDetails.html";

        // when
        productDetailsListenersT.addTechnologyGroup(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowTechnologiesWithTechnologyGroupIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsListenersT.showTechnologiesWithTechnologyGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowTechnologiesWithTechnologyGroupIfProductIsSavedAndTechnologyGroupIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsListenersT.showTechnologiesWithTechnologyGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowTechnologiesWithTechnologyGroupIfProductIsSavedAndTechnologyGroupIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(technologyGroup);

        given(technologyGroup.getStringField(NUMBER)).willReturn(L_TECHNOLOGY_GROUP_NUMBER);

        filters.put("technologyGroupNumber", L_TECHNOLOGY_GROUP_NUMBER);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsListenersT.showTechnologiesWithTechnologyGroup(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowTechnologiesWithProductIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsListenersT.showTechnologiesWithProduct(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowTechnologiesWithProductIfProductIsntSavedAndProductNameIsNull() {
        // given
        given(product.getId()).willReturn(1L);

        given(product.getStringField(NAME)).willReturn(null);

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsListenersT.showTechnologiesWithProduct(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowTechnologiesWithProductIfProductIsSavedAndProductNameIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NAME)).willReturn(L_PRODUCT_NAME);

        filters.put("productName", L_PRODUCT_NAME);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.technologies");

        String url = "../page/technologies/technologiesList.html";

        // when
        productDetailsListenersT.showTechnologiesWithProduct(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

}
