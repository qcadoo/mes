/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MasterOrderDetailsHooksTest {

    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent masterOrderTypeField, technologyField, defaultTechnologyField, cumulatedQuantityField,
            masterOrderQuantityField, producedOrderQuantityField, cumulatedOrderQuantityUnitField, masterOrderQuantityUnitField,
            producedOrderQuantityUnitField, leftToRelease, leftToReleaseUnit, comments, masterOrderPositionStatus;

    @Mock
    private FormComponent form;

    @Mock
    private LookupComponent productField;

    @Mock
    private GridComponent masterOrderProducts;

    @Mock
    private ExpressionService expressionService;

    @Mock
    private TechnologyServiceO technologyServiceO;

    @Mock
    private Entity productEntity, defaultTechnologyEntity, masterOrderEntity;

    @Mock
    private ComponentState borderLayoutProductQuantity;

    @Before
    public void init() {
        masterOrderDetailsHooks = new MasterOrderDetailsHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(masterOrderDetailsHooks, "technologyServiceO", technologyServiceO);
        ReflectionTestUtils.setField(masterOrderDetailsHooks, "expressionService", expressionService);
        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS)).willReturn(masterOrderProducts);
        WindowComponentState windowComponent = mock(WindowComponentState.class);
        mockRibbon(windowComponent);
    }

    private void mockRibbon(final WindowComponentState windowComponent) {
        given(view.getComponentByReference("window")).willReturn(windowComponent);
        Ribbon ribbon = mock(Ribbon.class);
        given(windowComponent.getRibbon()).willReturn(ribbon);
        RibbonGroup ordersRibbonGroup = mock(RibbonGroup.class);
        given(ribbon.getGroupByName("orders")).willReturn(ordersRibbonGroup);
        RibbonActionItem createOrderButton = mock(RibbonActionItem.class);
        given(ordersRibbonGroup.getItemByName("createOrder")).willReturn(createOrderButton);
    }

    @Test
    public final void shouldFillDefaultTechnologyIfExists() {
        // given
        String defaultTechnologyExpression = "00001 - Tech-1";
        given(productField.getEntity()).willReturn(productEntity);
        given(technologyServiceO.getDefaultTechnology(productEntity)).willReturn(defaultTechnologyEntity);
        given(view.getLocale()).willReturn(Locale.getDefault());
        given(expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name", Locale.getDefault())).willReturn(
                defaultTechnologyExpression);
        // given
        masterOrderDetailsHooks.fillDefaultTechnology(view);
        // then

        verify(defaultTechnologyField).setFieldValue(defaultTechnologyExpression);
    }

    @Test
    public final void shouldFillNullWhenDefaultTechnlogyDoesnotExists() {
        // given
        given(productField.getEntity()).willReturn(null);
        // given
        masterOrderDetailsHooks.fillDefaultTechnology(view);
        // then
        verify(defaultTechnologyField).setFieldValue(null);
    }


}
