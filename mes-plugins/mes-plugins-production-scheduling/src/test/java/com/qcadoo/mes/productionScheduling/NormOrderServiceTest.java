/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.productionScheduling;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;


public class NormOrderServiceTest {

    private NormOrderService normOrderService;
    
    @Before
    public void init() {
        normOrderService = new NormOrderService();
    }
    
    @Test
    public void shouldCheckIfChosenTechnologyTreeIsNotEmptyValidatorReturnFalse() throws Exception {
        // given
        Entity order = mock(Entity.class);
        Entity technology = mock(Entity.class);
        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        EntityTree emptyTree = mock(EntityTree.class);
        
        when(order.getBelongsToField("technology")).thenReturn(technology);
        when(emptyTree.size()).thenReturn(0);
        when(emptyTree.isEmpty()).thenReturn(true);
        when(technology.getTreeField("operationComponents")).thenReturn(emptyTree);
        when(orderDataDefinition.getField(Mockito.anyString())).thenReturn(fieldDefinition);
        when(technology.getField("name")).thenReturn(fieldDefinition);
        when(fieldDefinition.toString()).thenReturn("mocked field");
        

        // when
        boolean returnValue = normOrderService.checkIfChosenTechnologyTreeIsNotEmpty(orderDataDefinition, order);

        //then
        assertFalse(returnValue);
        verify(order, times(1)).addError(Mockito.eq(fieldDefinition), Mockito.anyString());

    }
    
    @Test
    public void shouldCheckIfChosenTechnologyTreeIsNotEmptyValidatorReturnTrue() throws Exception {
        // given
        Entity order = mock(Entity.class);
        Entity technology = mock(Entity.class);
        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        EntityTree notEmptyTree = mock(EntityTree.class);
        
        when(order.getBelongsToField("technology")).thenReturn(technology);
        when(notEmptyTree.size()).thenReturn(1);
        when(notEmptyTree.isEmpty()).thenReturn(false);
        when(technology.getTreeField("operationComponents")).thenReturn(notEmptyTree);
        when(orderDataDefinition.getField(Mockito.anyString())).thenReturn(fieldDefinition);
        

        // when
        boolean returnValue = normOrderService.checkIfChosenTechnologyTreeIsNotEmpty(orderDataDefinition, order);

        //then
        assertTrue(returnValue);
        verify(order, never()).addError(Mockito.eq(fieldDefinition), Mockito.anyString());

    }
}
