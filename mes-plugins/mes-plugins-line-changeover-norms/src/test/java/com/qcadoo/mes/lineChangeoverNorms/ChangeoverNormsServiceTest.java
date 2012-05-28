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
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology,
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
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology, null))
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
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology, null))
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
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology, null))
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
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);

        // when
        Entity returnedChangeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology,
                productionLine);

        // then
        Assert.assertNull(returnedChangeover);
    }
}
