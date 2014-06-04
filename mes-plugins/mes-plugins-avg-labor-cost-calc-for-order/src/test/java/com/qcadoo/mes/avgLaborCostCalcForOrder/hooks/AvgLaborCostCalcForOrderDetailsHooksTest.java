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
package com.qcadoo.mes.avgLaborCostCalcForOrder.hooks;

import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.AVERAGE_LABOR_HOURLY_COST;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class AvgLaborCostCalcForOrderDetailsHooksTest {

    private AvgLaborCostCalcForOrderDetailsHooks orderDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent averageLaborHourlyCost;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup hourlyCostNorms;

    @Mock
    private RibbonActionItem copyToOperationsNorms;

    @Before
    public void init() {
        orderDetailsHooks = new AvgLaborCostCalcForOrderDetailsHooks();
        MockitoAnnotations.initMocks(this);

        when(view.getComponentByReference("window")).thenReturn((ComponentState) window);
        when(window.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("hourlyCostNorms")).thenReturn(hourlyCostNorms);
        when(window.getRibbon().getGroupByName("hourlyCostNorms").getItemByName("copyToOperationsNorms")).thenReturn(
                copyToOperationsNorms);
        when(view.getComponentByReference(AVERAGE_LABOR_HOURLY_COST)).thenReturn(averageLaborHourlyCost);

    }

    @Test
    public void shouldEnabledButtonForCopyNorms() throws Exception {
        // given
        String averageLaborHourlyCostValue = "50";

        when(averageLaborHourlyCost.getFieldValue()).thenReturn(averageLaborHourlyCostValue);

        // when
        orderDetailsHooks.enabledButtonForCopyNorms(view);

        // then
        Mockito.verify(copyToOperationsNorms).setEnabled(true);
    }

    @Test
    public void shouldDisbaleButtonForCopyNorms() throws Exception {
        // given
        String averageLaborHourlyCostValue = "";

        when(averageLaborHourlyCost.getFieldValue()).thenReturn(averageLaborHourlyCostValue);

        // when
        orderDetailsHooks.enabledButtonForCopyNorms(view);

        // then
        Mockito.verify(copyToOperationsNorms).setEnabled(false);
    }

    @After
    public void flush() {

    }
}
