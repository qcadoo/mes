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
package com.qcadoo.mes.costCalculation;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class CostCalculationModelValidatorsTest {

    private CostCalculationModelValidators costCalculationModelValidators;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity costCalculation, technology;

    @Mock
    private TechnologyService technologyService;

    private static EntityTree mockEntityTreeIterator(List<Entity> list) {
        EntityTree tree = mock(EntityTree.class);
        when(tree.isEmpty()).thenReturn(false);
        when(tree.iterator()).thenReturn(list.iterator());
        return tree;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        costCalculationModelValidators = new CostCalculationModelValidators();

        ReflectionTestUtils.setField(costCalculationModelValidators, "technologyService", technologyService);
    }

    @Test
    public void shouldNotAcceptNoTechnologyTree() {
        // given
        when(costCalculation.getBelongsToField("technology")).thenReturn(technology);
        Entity opComp = mock(Entity.class);
        EntityTree tree = mockEntityTreeIterator(asList(opComp));
        when(technology.getTreeField("operationComponents")).thenReturn(tree);
        when(technologyService.getProductCountForOperationComponent(opComp)).thenThrow(new IllegalStateException());

        // when
        boolean result = costCalculationModelValidators.checkIfTheTechnologyTreeIsntEmpty(dataDefinition, costCalculation);

        // then
        assertFalse(result);
    }
}
