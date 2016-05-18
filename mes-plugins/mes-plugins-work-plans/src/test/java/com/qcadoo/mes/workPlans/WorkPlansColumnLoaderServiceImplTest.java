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
package com.qcadoo.mes.workPlans;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class WorkPlansColumnLoaderServiceImplTest {

    private WorkPlansColumnLoaderServiceImpl workPlansColumnLoaderServiceImpl;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private WorkPlansService workPlansService;

    @Mock
    private Entity parameter;

    @Mock
    private Entity operation;

    @Mock
    private Entity technologyOperationComponent;

    @Mock
    private List<Entity> operations;

    @Mock
    private List<Entity> technologyOperationComponents;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private DataDefinition operationDD;

    @Mock
    private DataDefinition technologyOperationComponentDD;

    @Mock
    private DataDefinition columnForInputProductsDD;

    @Mock
    private DataDefinition columnForOutputProductsDD;

    @Mock
    private SearchCriteriaBuilder searchCriteria;

    @Mock
    private SearchResult searchResult;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        workPlansColumnLoaderServiceImpl = new WorkPlansColumnLoaderServiceImpl();

        ReflectionTestUtils.setField(workPlansColumnLoaderServiceImpl, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(workPlansColumnLoaderServiceImpl, "parameterService", parameterService);
        ReflectionTestUtils.setField(workPlansColumnLoaderServiceImpl, "workPlansService", workPlansService);
    }

    @Test
    public void shouldSetParameterDefaultValuesIfParameterIsntNull() {
        // given
        when(parameterService.getParameter()).thenReturn(parameter);

        when(parameter.isValid()).thenReturn(true);

        when(parameter.getDataDefinition()).thenReturn(parameterDD);
        when(parameterDD.save(parameter)).thenReturn(parameter);

        // when
        workPlansColumnLoaderServiceImpl.setParameterDefaultValues();

        // then
        verify(parameter).setField(ParameterFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS, false);
        verify(parameter).setField(ParameterFieldsWP.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS, false);
        verify(parameter).setField(ParameterFieldsWP.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS, false);
        verify(parameter).setField(ParameterFieldsWP.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS, false);
    }

    @Ignore
    @Test
    public void shouldFillColumnsForProducts() {
        // given
        when(dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS))
                .thenReturn(columnForInputProductsDD);

        when(dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS))
                .thenReturn(columnForOutputProductsDD);

        // when
        workPlansColumnLoaderServiceImpl.fillColumnsForProducts("plugin");

        // then
    }

    @Ignore
    @Test
    public void shouldClearColumnsForProducts() {
        // given
        when(dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS))
                .thenReturn(columnForInputProductsDD);

        when(dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS))
                .thenReturn(columnForOutputProductsDD);

        // when
        workPlansColumnLoaderServiceImpl.clearColumnsForProducts("plugin");

        // then
    }

}
