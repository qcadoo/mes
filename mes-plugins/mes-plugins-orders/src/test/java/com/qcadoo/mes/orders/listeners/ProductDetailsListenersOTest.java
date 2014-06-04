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
package com.qcadoo.mes.orders.listeners;

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

public class ProductDetailsListenersOTest {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_PRODUCT_NUMBER = "000001";

    private static final long L_ID = 1L;

    private ProductDetailsListenersO productDetailsListenersO;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productForm;

    @Mock
    private Entity product;

    private Map<String, Object> parameters = Maps.newHashMap();

    private Map<String, String> filters = Maps.newHashMap();

    private Map<String, Object> gridOptions = Maps.newHashMap();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productDetailsListenersO = new ProductDetailsListenersO();

        given(view.getComponentByReference("form")).willReturn(productForm);
        given(productForm.getEntity()).willReturn(product);
    }

    @Test
    public void shouldntShowOrdersWithProductMainIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsListenersO.showOrdersWithProductMain(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductMainIfProductIsSavedAndProductNumberIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(null);

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsListenersO.showOrdersWithProductMain(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowOrdersWithProductMainIfProductIsSavedAndProductNumberIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(L_PRODUCT_NUMBER);

        filters.put("productNumber", "[" + L_PRODUCT_NUMBER + "]");

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/ordersList.html";

        // when
        productDetailsListenersO.showOrdersWithProductMain(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductPlannedIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        String url = "../page/orders/ordersPlanningList.html";

        // when
        productDetailsListenersO.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowOrdersWithProductPlannedIfProductIsSavedAndProductNumberIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(null);

        String url = "../page/orders/ordersPlanningList.html";

        // when
        productDetailsListenersO.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowOrdersWithProductPlannedIfProductIsSavedAndProductNumberIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getStringField(NUMBER)).willReturn(L_PRODUCT_NUMBER);

        filters.put("productNumber", "[" + L_PRODUCT_NUMBER + "]");

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrdersPlanning");

        String url = "../page/orders/ordersPlanningList.html";

        // when
        productDetailsListenersO.showOrdersWithProductPlanned(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

}
