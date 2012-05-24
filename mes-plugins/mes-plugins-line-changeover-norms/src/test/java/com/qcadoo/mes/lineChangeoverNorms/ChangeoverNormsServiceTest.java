package com.qcadoo.mes.lineChangeoverNorms;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
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
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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
        changeoverNormsService.matchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then
        Assert.assertNotNull(changeover);
    }

    @Test
    @Ignore
    public void shouldReturnMatchingChangeoverNormsFromTechToTechWithoutLine() throws Exception {
        // given
        Long fromTechGrId = 1L;
        Long toTechGrId = 1L;
        when(fromTechnologyGroup.getId()).thenReturn(fromTechGrId);
        when(toTechnologyGroup.getId()).thenReturn(toTechGrId);
        when(technologyGrDataDefinition.get(fromTechGrId)).thenReturn(fromTechnologyGroup);
        when(technologyGrDataDefinition.get(toTechGrId)).thenReturn(toTechnologyGroup);
        when(fromTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(toTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(toTechnologyGroup);
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).thenReturn(null);
        when(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology, null))
                .thenReturn(null);
        when(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                        toTechnologyGroup, productionLine)).thenReturn(changeover);

        // when
        Entity changeoverNorms = changeoverNormsService.matchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertNotNull(changeoverNorms);
    }

}
