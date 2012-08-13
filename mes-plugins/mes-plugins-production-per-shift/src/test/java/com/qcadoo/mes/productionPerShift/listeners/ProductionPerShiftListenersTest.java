/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.productionPerShift.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ProductionPerShiftListenersTest {

    private ProductionPerShiftListeners productionPerShiftListeners;

    @Mock
    private ViewDefinitionState viewState;

    @Mock
    private ComponentState componentState;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionPerShiftListeners = new ProductionPerShiftListeners();

        ReflectionTestUtils.setField(productionPerShiftListeners, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldRedirectToTheProductionPerShiftView() {
        // given
        Long id = 5L;
        given(componentState.getFieldValue()).willReturn(id);
        String url = "../page/productionPerShift/productionPerShiftDetails.html";

        // when
        productionPerShiftListeners.redirect(viewState, id);

        // then
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", id);

        verify(viewState).redirectTo(url, false, true, parameters);
    }

}
