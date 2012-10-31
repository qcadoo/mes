/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basicProductionCounting;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class BasicProductionCountingServiceTest {

    private BasicProductionCountingService basicProductionCountingService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent productField;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    Entity basicPC, product;

    @Before
    public void init() {
        basicProductionCountingService = new BasicProductionCountingService();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(basicProductionCountingService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldSetProductNameToFieldFromCounting() throws Exception {
        // given
        Long bpcId = 1L;
        String productName = "Product name";
        when(view.getComponentByReference("product")).thenReturn(productField);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(bpcId);
        when(dataDefinitionService.get("basicProductionCounting", "basicProductionCounting")).thenReturn(dataDefinition);
        when(dataDefinition.get(bpcId)).thenReturn(basicPC);
        when(basicPC.getBelongsToField("product")).thenReturn(product);
        when(product.getField("name")).thenReturn(productName);
        // when
        basicProductionCountingService.getProductNameFromCounting(view);
        // then
        Mockito.verify(productField).setFieldValue(productName);
    }
}
