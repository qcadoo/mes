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
package com.qcadoo.mes.costNormsForOperation;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

public class CostNormsForOperationServiceTest {

    private CostNormsForOperationService costNormsForOperationService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent field1, field2;

    @Mock
    private ComponentState state;

    @Mock
    private FormComponent form;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition operationDD;

    @Mock
    private Entity operationEntity;

    @Mock
    private Object obj1, obj2;

    @Mock
    private CurrencyService currencyService;

    @Before
    public void init() {
        costNormsForOperationService = new CostNormsForOperationService();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(costNormsForOperationService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(costNormsForOperationService, "currencyService", currencyService);

        when(dataDefinitionService.get("technologies", "operation")).thenReturn(operationDD);

        when(view.getComponentByReference("operation")).thenReturn(state);
        when(view.getComponentByReference(QcadooViewConstants.L_FORM)).thenReturn(form);

        when(view.getComponentByReference("laborHourlyCost")).thenReturn(field1);
        when(view.getComponentByReference("machineHourlyCost")).thenReturn(field2);

    }

    @Test
    public void shouldReturnWhenOperationIsNull() throws Exception {
        // when
        costNormsForOperationService.copyCostValuesFromOperation(view, state, null);
    }

    @Test
    public void shouldApplyCostNormsForGivenSource() throws Exception {
        // given
        when(state.getFieldValue()).thenReturn(1L);
        Long operationId = 1L;
        when(operationDD.get(operationId)).thenReturn(operationEntity);
        when(operationEntity.getField("laborHourlyCost")).thenReturn(obj1);
        when(operationEntity.getField("machineHourlyCost")).thenReturn(obj2);
        // when
        costNormsForOperationService.copyCostValuesFromOperation(view, state, null);
        // then
        Mockito.verify(field1).setFieldValue(obj1);
        Mockito.verify(field2).setFieldValue(obj2);

    }

    @Test
    public void shouldFillCurrencyFields() throws Exception {
        // given
        String currency = "PLN";
        when(currencyService.getCurrencyAlphabeticCode()).thenReturn(currency);
        when(view.getComponentByReference("laborHourlyCostCURRENCY")).thenReturn(field1);
        when(view.getComponentByReference("machineHourlyCostCURRENCY")).thenReturn(field2);

        // when
        costNormsForOperationService.fillCurrencyFields(view);
        // then

        Mockito.verify(field1).setFieldValue(currency);
        Mockito.verify(field2).setFieldValue(currency);
    }

}
