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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;

import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class OrderDetailsListenersTest {

    private OrderDetailsListeners orderDetailsListeners;

    private DataDefinitionService dataDefinitionService;

    private TechnologyServiceO technologyServiceO;

    private LookupComponent product;

    private FieldComponent technologyPrototype, defaultTechnology;

    private ViewDefinitionState viewDefinitionState;

    private ComponentState state;

    @Before
    public void init() {
        orderDetailsListeners = new OrderDetailsListeners();
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        technologyServiceO = mock(TechnologyServiceO.class, RETURNS_DEEP_STUBS);
        product = mock(LookupComponent.class);
        technologyPrototype = mock(FieldComponent.class);
        defaultTechnology = mock(FieldComponent.class);
        viewDefinitionState = mock(ViewDefinitionState.class);
        state = mock(ComponentState.class);

        setField(orderDetailsListeners, "dataDefinitionService", dataDefinitionService);
        setField(orderDetailsListeners, "technologyServiceO", technologyServiceO);

        given(viewDefinitionState.getComponentByReference("product")).willReturn(product);
        given(viewDefinitionState.getComponentByReference("technologyPrototype")).willReturn(technologyPrototype);
        given(viewDefinitionState.getComponentByReference("defaultTechnology")).willReturn(defaultTechnology);
    }

    /*
     * @Test public void shouldChangeOrderProductToNull() throws Exception { // given given(product.getEntity()).willReturn(null);
     * // when orderDetailsListeners.changeOrderProduct(viewDefinitionState, state, new String[0]); // then
     * verify(defaultTechnology).setFieldValue(""); verify(technologyPrototype).setFieldValue(null); }
     * @Test public void shouldChangeOrderProductWithoutDefaultTechnology() throws Exception { // given Entity productEntity =
     * mock(Entity.class); given(product.getEntity()).willReturn(productEntity);
     * given(technologyServiceO.getDefaultTechnology(productEntity)).willReturn(null); // when
     * orderDetailsListeners.changeOrderProduct(viewDefinitionState, state, new String[0]); // then
     * verify(defaultTechnology).setFieldValue(""); verify(technologyPrototype).setFieldValue(null); }
     * @Test public void shouldChangeOrderProductWithDefaultTechnology() throws Exception { // given Entity productEntity =
     * mock(Entity.class); Entity technologyEntity = mock(Entity.class); given(product.getEntity()).willReturn(productEntity);
     * given(technologyServiceO.getDefaultTechnology(productEntity)).willReturn(technologyEntity);
     * given(technologyEntity.getId()).willReturn(117L); // when orderDetailsListeners.changeOrderProduct(viewDefinitionState,
     * state, new String[0]); // then verify(defaultTechnology).setFieldValue("");
     * verify(technologyPrototype).setFieldValue(117L); }
     */
}
