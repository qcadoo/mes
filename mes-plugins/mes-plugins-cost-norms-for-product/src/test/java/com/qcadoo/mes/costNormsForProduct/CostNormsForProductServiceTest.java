/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.costNormsForProduct;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class CostNormsForProductServiceTest {

    private ViewDefinitionState viewDefinitionState;

    private CostNormsForProductService costNormsForProductService;

    private DataDefinition dataDefinition;

    private FormComponent form;

    private Entity entity;

    @Before
    public void init() {
        costNormsForProductService = new CostNormsForProductService();
        viewDefinitionState = mock(ViewDefinitionState.class);
        dataDefinition = mock(DataDefinition.class);
        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        form = mock(FormComponent.class);

        when(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT))
                .thenReturn(dataDefinition);

        when(viewDefinitionState.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(3L);
        when(dataDefinition.get(anyLong())).thenReturn(entity);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldReturnExceptionWhenViewDefinitionStateIsNull() throws Exception {
        costNormsForProductService.fillCostTabCurrency(null);
        costNormsForProductService.fillCostTabUnit(null);
    }

}
