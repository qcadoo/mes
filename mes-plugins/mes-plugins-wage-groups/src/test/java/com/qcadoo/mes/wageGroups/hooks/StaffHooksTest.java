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
package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.DETERMINED_INDIVIDUAL;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.INDIVIDUAL_LABOR_COST;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.WAGE_GROUP;
import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.LABOR_HOURLY_COST;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class StaffHooksTest {

    private StaffHooks hooks;

    @Mock
    private Entity entity, wageGroup;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        hooks = new StaffHooks();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveIndividualCost() throws Exception {
        // given
        when(entity.getBooleanField(DETERMINED_INDIVIDUAL)).thenReturn(true);
        when(entity.getField(INDIVIDUAL_LABOR_COST)).thenReturn(BigDecimal.ONE);
        // when
        hooks.saveLaborHourlyCost(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("laborHourlyCost", BigDecimal.ONE);
    }

    @Test
    public void shouldReturnWhenWageDoesnotExists() throws Exception {
        // given
        when(entity.getBooleanField(DETERMINED_INDIVIDUAL)).thenReturn(false);
        when(entity.getBelongsToField(WAGE_GROUP)).thenReturn(null);
        // when
        hooks.saveLaborHourlyCost(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("laborHourlyCost", null);
        // then
    }

    @Test
    public void shouldSaveCostFromWageGroup() throws Exception {
        // given
        when(entity.getBooleanField(DETERMINED_INDIVIDUAL)).thenReturn(false);
        when(entity.getBelongsToField(WAGE_GROUP)).thenReturn(wageGroup);
        when(wageGroup.getField(LABOR_HOURLY_COST)).thenReturn(BigDecimal.TEN);
        // when
        hooks.saveLaborHourlyCost(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("laborHourlyCost", BigDecimal.TEN);
        // then
    }
}
