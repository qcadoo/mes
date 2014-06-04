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
package com.qcadoo.mes.lineChangeoverNorms;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityTestUtils;

public class ChangeoverNormsServiceTest {

    private static final Long FROM_TECH_ID = 1L;

    private static final Long TO_TECH_ID = 2L;

    private static final Long FROM_TECH_GROUP_ID = 101L;

    private static final Long TO_TECH_GROUP_ID = 202L;

    private static final Long PRODUCTION_LINE_ID = 1001L;

    private ChangeoverNormsService changeoverNormsService;

    @Mock
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Mock
    private DataDefinition technologyGrDataDefinition, changeoverDataDefinition;

    @Mock
    private Entity fromTechnology, toTechnology, productionLine, changeover, fromTechnologyGroup, toTechnologyGroup;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        changeoverNormsService = new ChangeoverNormsServiceImpl();
        ReflectionTestUtils.setField(changeoverNormsService, "changeoverNormsSearchService", changeoverNormsSearchService);

        EntityTestUtils.stubId(fromTechnology, FROM_TECH_ID);
        EntityTestUtils.stubBelongsToField(fromTechnology, TechnologyFields.TECHNOLOGY_GROUP, fromTechnologyGroup);
        EntityTestUtils.stubId(fromTechnologyGroup, FROM_TECH_GROUP_ID);

        EntityTestUtils.stubId(toTechnology, TO_TECH_ID);
        EntityTestUtils.stubBelongsToField(toTechnology, TechnologyFields.TECHNOLOGY_GROUP, toTechnologyGroup);
        EntityTestUtils.stubId(toTechnologyGroup, TO_TECH_GROUP_ID);

        EntityTestUtils.stubId(productionLine, PRODUCTION_LINE_ID);

    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechOnGivenLine() throws Exception {
        // given
        when(
                changeoverNormsSearchService.findBestMatching(FROM_TECH_ID, FROM_TECH_GROUP_ID, TO_TECH_ID, TO_TECH_GROUP_ID,
                        PRODUCTION_LINE_ID)).thenReturn(changeover);

        // when
        Entity returnedChangeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology,
                productionLine);

        // then
        Assert.assertEquals(changeover, returnedChangeover);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechWithoutLine() throws Exception {
        // given
        when(changeoverNormsSearchService.findBestMatching(FROM_TECH_ID, FROM_TECH_GROUP_ID, TO_TECH_ID, TO_TECH_GROUP_ID, null))
                .thenReturn(changeover);

        // when
        Entity changeoverNorms = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, null);
        // then

        Assert.assertEquals(changeover, changeoverNorms);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsBetweenTwoTechsNotBelongingToAnyGroupWithoutLine() throws Exception {
        // given
        EntityTestUtils.stubBelongsToField(fromTechnology, TechnologyFields.TECHNOLOGY_GROUP, null);
        EntityTestUtils.stubBelongsToField(toTechnology, TechnologyFields.TECHNOLOGY_GROUP, null);
        when(changeoverNormsSearchService.findBestMatching(FROM_TECH_ID, null, TO_TECH_ID, null, null)).thenReturn(changeover);

        // when
        Entity changeoverNorms = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, null);
        // then

        Assert.assertEquals(changeover, changeoverNorms);
    }

    @Test
    public void shouldReturnNullIfNoMatchingChangeoverNormWasFound() {
        // given
        when(changeoverNormsSearchService.findBestMatching(anyLong(), anyLong(), anyLong(), anyLong(), anyLong())).thenReturn(
                null);

        // when
        Entity returnedChangeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology,
                productionLine);

        // then
        Assert.assertNull(returnedChangeover);
    }
}
