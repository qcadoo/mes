/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class WorkPlansColumnLoaderServiceImplTest {

    @Mock
    private Entity parameter;

    @Mock
    private Entity operation;

    @Mock
    private Entity technologyOperationComponent;

    @Mock
    private Entity technologyInstanceOperationComponent;

    @Mock
    private List<Entity> operations;

    @Mock
    private List<Entity> technologyOperationComponents;

    @Mock
    private List<Entity> technologyInstanceOperationComponents;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private DataDefinition operationDD;

    @Mock
    private DataDefinition technologyOperationComponentDD;

    @Mock
    private DataDefinition technologyInstanceOperationComponentDD;

    @Mock
    private DataDefinition columnForInputProductsDD;

    @Mock
    private DataDefinition columnForOutputProductsDD;

    @Mock
    private SearchCriteriaBuilder searchCriteria;

    @Mock
    private SearchResult searchResult;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ParameterService parameterService;

    private WorkPlansColumnLoaderServiceImpl workPlansColumnLoaderServiceImpl;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        workPlansColumnLoaderServiceImpl = new WorkPlansColumnLoaderServiceImpl();

        ReflectionTestUtils.setField(workPlansColumnLoaderServiceImpl, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(workPlansColumnLoaderServiceImpl, "parameterService", parameterService);
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
        verify(parameter).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
        verify(parameter).setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
        verify(parameter).setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
        verify(parameter).setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
        verify(parameter).setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
    }

    @Test
    public void shouldSetOperationDefaultValuesIfOperationsIsntNull() {
        // given
        when(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION))
                .thenReturn(operationDD);
        when(operationDD.find()).thenReturn(searchCriteria);
        when(searchCriteria.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(operations);

        Entity operation1 = mock(Entity.class);
        Entity operation2 = mock(Entity.class);
        Entity operation3 = mock(Entity.class);

        DataDefinition operation1DD = mock(DataDefinition.class);
        DataDefinition operation2DD = mock(DataDefinition.class);
        DataDefinition operation3DD = mock(DataDefinition.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> operationsIterator = mock(Iterator.class);
        when(operationsIterator.hasNext()).thenReturn(true, true, true, false);
        when(operationsIterator.next()).thenReturn(operation1, operation2, operation3);

        when(operations.iterator()).thenReturn(operationsIterator);

        when(operation1.isValid()).thenReturn(true);
        when(operation2.isValid()).thenReturn(true);
        when(operation3.isValid()).thenReturn(true);

        when(operation1.getDataDefinition()).thenReturn(operation1DD);
        when(operation2.getDataDefinition()).thenReturn(operation2DD);
        when(operation3.getDataDefinition()).thenReturn(operation3DD);

        // when
        workPlansColumnLoaderServiceImpl.setOperationDefaultValues();

        // then
        for (Entity operation : Arrays.asList(operation1, operation2, operation3)) {
            verify(operation).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
            verify(operation).setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
            verify(operation).setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
            verify(operation).setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
            verify(operation).setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
        }
    }

    @Test
    public void shouldntSetOperationDefaultValuesIfOperationsIsNull() {
        // given
        when(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION))
                .thenReturn(operationDD);
        when(operationDD.find()).thenReturn(searchCriteria);
        when(searchCriteria.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(null);

        // when
        workPlansColumnLoaderServiceImpl.setOperationDefaultValues();

        // then
        verify(operation, never()).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
        verify(operation, never()).setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
        verify(operation, never()).setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
        verify(operation, never()).setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
        verify(operation, never()).setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
    }

    @Test
    public void shouldSetTechnologyOperationComponentDefaultValuesIfTechnologyOperationComponentsIsntNull() {
        // given
        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)).thenReturn(technologyOperationComponentDD);
        when(technologyOperationComponentDD.find()).thenReturn(searchCriteria);
        when(searchCriteria.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(technologyOperationComponents);

        Entity technologyOperationComponent1 = mock(Entity.class);
        Entity technologyOperationComponent2 = mock(Entity.class);
        Entity technologyOperationComponent3 = mock(Entity.class);

        DataDefinition technologyOperationComponent1DD = mock(DataDefinition.class);
        DataDefinition technologyOperationComponent2DD = mock(DataDefinition.class);
        DataDefinition technologyOperationComponent3DD = mock(DataDefinition.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> operationsIterator = mock(Iterator.class);
        when(operationsIterator.hasNext()).thenReturn(true, true, true, false);
        when(operationsIterator.next()).thenReturn(technologyOperationComponent1, technologyOperationComponent2,
                technologyOperationComponent3);

        when(technologyOperationComponents.iterator()).thenReturn(operationsIterator);

        when(technologyOperationComponent1.isValid()).thenReturn(true);
        when(technologyOperationComponent2.isValid()).thenReturn(true);
        when(technologyOperationComponent3.isValid()).thenReturn(true);

        when(technologyOperationComponent1.getDataDefinition()).thenReturn(technologyOperationComponent1DD);
        when(technologyOperationComponent2.getDataDefinition()).thenReturn(technologyOperationComponent2DD);
        when(technologyOperationComponent3.getDataDefinition()).thenReturn(technologyOperationComponent3DD);

        // when
        workPlansColumnLoaderServiceImpl.setTechnologyOperationComponentDefaultValues();

        // then
        for (Entity technologyOperationCoponent : Arrays.asList(technologyOperationComponent1, technologyOperationComponent2,
                technologyOperationComponent3)) {
            verify(technologyOperationCoponent).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
            verify(technologyOperationCoponent).setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
            verify(technologyOperationCoponent).setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
            verify(technologyOperationCoponent).setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
            verify(technologyOperationCoponent)
                    .setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
        }
    }

    @Test
    public void shouldntSetTechnologyOperationComponentDefaultValuesIfTechnologyOperationComponentsIsNull() {
        // given
        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)).thenReturn(technologyOperationComponentDD);
        when(technologyOperationComponentDD.find()).thenReturn(searchCriteria);
        when(searchCriteria.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(null);

        // when
        workPlansColumnLoaderServiceImpl.setTechnologyOperationComponentDefaultValues();

        // then

        verify(technologyOperationComponent, never()).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
        verify(technologyOperationComponent, never()).setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
        verify(technologyOperationComponent, never()).setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD,
                false);
        verify(technologyOperationComponent, never()).setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD,
                false);
        verify(technologyOperationComponent, never()).setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD,
                false);
    }

    @Test
    public void shouldSetTechnologyInstanceOperationComponentsDefaultValuesIfTechnologyInstanceOperationComponentsIsntNull() {
        // given
        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).thenReturn(
                technologyInstanceOperationComponentDD);
        when(technologyInstanceOperationComponentDD.find()).thenReturn(searchCriteria);
        when(searchCriteria.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(technologyInstanceOperationComponents);

        Entity technologyInstanceOperationComponent1 = mock(Entity.class);
        Entity technologyInstanceOperationComponent2 = mock(Entity.class);
        Entity technologyInstanceOperationComponent3 = mock(Entity.class);

        DataDefinition technologyInstanceOperationComponent1DD = mock(DataDefinition.class);
        DataDefinition technologyInstanceOperationComponent2DD = mock(DataDefinition.class);
        DataDefinition technologyInstanceOperationComponent3DD = mock(DataDefinition.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> operationsIterator = mock(Iterator.class);
        when(operationsIterator.hasNext()).thenReturn(true, true, true, false);
        when(operationsIterator.next()).thenReturn(technologyInstanceOperationComponent1, technologyInstanceOperationComponent2,
                technologyInstanceOperationComponent3);

        when(technologyInstanceOperationComponents.iterator()).thenReturn(operationsIterator);

        when(technologyInstanceOperationComponent1.isValid()).thenReturn(true);
        when(technologyInstanceOperationComponent2.isValid()).thenReturn(true);
        when(technologyInstanceOperationComponent3.isValid()).thenReturn(true);

        when(technologyInstanceOperationComponent1.getDataDefinition()).thenReturn(technologyInstanceOperationComponent1DD);
        when(technologyInstanceOperationComponent2.getDataDefinition()).thenReturn(technologyInstanceOperationComponent2DD);
        when(technologyInstanceOperationComponent3.getDataDefinition()).thenReturn(technologyInstanceOperationComponent3DD);

        // when
        workPlansColumnLoaderServiceImpl.setTechnologyInstanceOperationComponentDefaultValues();

        // then
        for (Entity orderOperationCoponent : Arrays.asList(technologyInstanceOperationComponent1,
                technologyInstanceOperationComponent2, technologyInstanceOperationComponent3)) {
            verify(orderOperationCoponent).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD, false);
            verify(orderOperationCoponent).setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
            verify(orderOperationCoponent).setField(WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
            verify(orderOperationCoponent).setField(WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
            verify(orderOperationCoponent).setField(WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
        }
    }

    @Test
    public void shouldntSetTechnologyInstanceOperationComponentsDefaultValuesIfTechnologyInstanceOperationComponentsIsNull() {
        // given
        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).thenReturn(
                technologyInstanceOperationComponentDD);
        when(technologyInstanceOperationComponentDD.find()).thenReturn(searchCriteria);
        when(searchCriteria.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(null);

        // when
        workPlansColumnLoaderServiceImpl.setTechnologyInstanceOperationComponentDefaultValues();

        // then
        verify(technologyInstanceOperationComponent, never()).setField(WorkPlansConstants.HIDE_DESCRIPTION_IN_WORK_PLANS_FIELD,
                false);
        verify(technologyInstanceOperationComponent, never())
                .setField(WorkPlansConstants.HIDE_DETAILS_IN_WORK_PLANS_FIELD, false);
        verify(technologyInstanceOperationComponent, never()).setField(
                WorkPlansConstants.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS_FIELD, false);
        verify(technologyInstanceOperationComponent, never()).setField(
                WorkPlansConstants.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
        verify(technologyInstanceOperationComponent, never()).setField(
                WorkPlansConstants.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS_FIELD, false);
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
