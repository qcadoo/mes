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
package com.qcadoo.mes.materialFlow;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class MaterialsInStockAreasTest {

    private MaterialsInStockAreasService materialFlowService;

    private Entity entity;

    @Autowired
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        materialFlowService = new MaterialsInStockAreasService();
        entity = mock(Entity.class);
    }

    @Test
    public void shouldClearGeneratedOnCopy() throws Exception {
        // given

        // when
        boolean bool = materialFlowService.clearGeneratedOnCopy(dataDefinition, entity);
        // then
        assertTrue(bool);
        verify(entity, Mockito.times(4)).setField(Mockito.anyString(), Mockito.any());

    }
    
    
}
