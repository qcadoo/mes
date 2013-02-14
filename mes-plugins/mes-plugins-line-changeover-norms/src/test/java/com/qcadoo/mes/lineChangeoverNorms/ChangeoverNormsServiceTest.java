/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class ChangeoverNormsServiceTest {

    private ChangeoverNormsService changeoverNormsService;

    @Mock
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition technologyGrDataDefinition, changeoverDataDefinition;

    @Mock
    private Entity fromTechnology, toTechnology, productionLine, changeover, fromTechnologyGroup, toTechnologyGroup;

    private SearchCriteriaBuilder searchCriteria;

    @Before
    public void init() {
        changeoverNormsService = new ChangeoverNormsServiceImpl();
        MockitoAnnotations.initMocks(this);
        searchCriteria = Mockito.mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(changeoverNormsService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(changeoverNormsService, "changeoverNormsSearchService", changeoverNormsSearchService);

        when(dataDefinitionService.get("lineChangeoverNorms", "lineChangeoverNorms")).thenReturn(changeoverDataDefinition);
        when(changeoverDataDefinition.find()).thenReturn(searchCriteria);
        PowerMockito.mockStatic(SearchRestrictions.class);

        when(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_GROUP))
                .thenReturn(technologyGrDataDefinition);

    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechForLine() throws Exception {
        // given

        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).thenReturn(changeover);

        // when
        Entity returnedChangeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology,
                productionLine);

        // then
        Assert.assertEquals(changeover, returnedChangeover);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechWithoutLine() throws Exception {
        // given
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology, null))
                .thenReturn(changeover);
        // when
        Entity changeoverNorms = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertEquals(changeover, changeoverNorms);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechGrWhenForTechDosnotExists() throws Exception {
        // given
        Long fromTechGrId = 1L;
        Long toTechGrId = 2L;
        // when
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology, null))
                .thenReturn(null);

        when(toTechnology.getBelongsToField("technologyGroup")).thenReturn(toTechnologyGroup);
        when(toTechnologyGroup.getId()).thenReturn(toTechGrId);
        when(technologyGrDataDefinition.get(toTechGrId)).thenReturn(toTechnologyGroup);

        when(fromTechnology.getBelongsToField("technologyGroup")).thenReturn(fromTechnologyGroup);
        when(fromTechnologyGroup.getId()).thenReturn(fromTechGrId);
        when(technologyGrDataDefinition.get(fromTechGrId)).thenReturn(fromTechnologyGroup);

        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                        toTechnologyGroup, productionLine)).thenReturn(changeover);
        Entity changeoverNorms = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertEquals(changeover, changeoverNorms);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechGrWithoutLineWhenFromTechGrDoesnotExists() throws Exception {
        // given
        Long fromTechGrId = 1L;
        Long toTechGrId = 2L;
        // when
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology, null))
                .thenReturn(null);

        when(toTechnology.getBelongsToField("technologyGroup")).thenReturn(toTechnologyGroup);
        when(toTechnologyGroup.getId()).thenReturn(toTechGrId);
        when(technologyGrDataDefinition.get(toTechGrId)).thenReturn(toTechnologyGroup);

        when(fromTechnology.getBelongsToField("technologyGroup")).thenReturn(fromTechnologyGroup);
        when(fromTechnologyGroup.getId()).thenReturn(fromTechGrId);
        when(technologyGrDataDefinition.get(fromTechGrId)).thenReturn(fromTechnologyGroup);

        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                        toTechnologyGroup, productionLine)).thenReturn(null);
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                        toTechnologyGroup, null)).thenReturn(changeover);
        Entity changeoverNorms = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertEquals(changeover, changeoverNorms);
    }

    @Test
    public void shouldReturnNullIfNoMatchingChangeoverNormWasFound() {
        // given
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);

        // when
        Entity returnedChangeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology,
                productionLine);

        // then
        Assert.assertNull(returnedChangeover);
    }
}
