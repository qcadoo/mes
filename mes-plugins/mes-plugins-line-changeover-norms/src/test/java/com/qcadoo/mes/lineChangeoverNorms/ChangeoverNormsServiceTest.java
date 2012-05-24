package com.qcadoo.mes.lineChangeoverNorms;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static org.mockito.BDDMockito.given;
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
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity fromTechnology, toTechnology, productionLine, changeover, fromTechnologyGroup, toTechnologyGroup;

    private SearchCriteriaBuilder searchCriteria;

    @Before
    public void init() {
        changeoverNormsService = new ChangeoverNormsServiceImpl();
        MockitoAnnotations.initMocks(this);
        searchCriteria = Mockito.mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(changeoverNormsService, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get("lineChangeoverNorms", "lineChangeoverNorms")).willReturn(dataDefinition);
        given(dataDefinition.find()).willReturn(searchCriteria);
        PowerMockito.mockStatic(SearchRestrictions.class);

    }

    private void stubSCForSearchMatchingCONorms(final Entity fromTechnology, final Entity toTechnology,
            final Entity fromTechnologyGroup, final Entity toTechnologyGroup, final Entity producionLine,
            final Entity returnedChangeover) {
        given(
                searchCriteria.add(belongsTo(FROM_TECHNOLOGY, fromTechnology)).add(belongsTo(TO_TECHNOLOGY, toTechnology))
                        .add(belongsTo(FROM_TECHNOLOGY_GROUP, fromTechnologyGroup))
                        .add(belongsTo(TO_TECHNOLOGY_GROUP, toTechnologyGroup)).add(belongsTo(PRODUCTION_LINE, producionLine))
                        .uniqueResult()).willReturn(returnedChangeover);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechForLine() throws Exception {
        // given
        fromTechnologyGroup = null;
        toTechnologyGroup = null;

        when(fromTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(toTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(toTechnologyGroup);

        stubSCForSearchMatchingCONorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup, productionLine,
                changeover);

        // when
        changeoverNormsService.matchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertNotNull(changeover);
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechWithoutLine() throws Exception {
        // given

        fromTechnologyGroup = null;
        toTechnologyGroup = null;
        when(fromTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(toTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(toTechnologyGroup);

        stubSCForSearchMatchingCONorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup, productionLine, null);
        stubSCForSearchMatchingCONorms(null, null, fromTechnologyGroup, toTechnologyGroup, productionLine, null);
        stubSCForSearchMatchingCONorms(null, null, fromTechnologyGroup, toTechnologyGroup, null, changeover);

        when(searchCriteria.uniqueResult()).thenReturn(null);
        // when
        Entity changeoverNorms = changeoverNormsService.matchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertNotNull(changeoverNorms);
    }

}
