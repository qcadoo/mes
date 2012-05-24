package com.qcadoo.mes.lineChangeoverNorms;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import com.qcadoo.model.api.search.SearchCriterion;
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

    @Mock
    private SearchCriteriaBuilder searchCriteria;

    @Mock
    private SearchCriterion criterion;

    @Before
    public void init() {
        changeoverNormsService = new ChangeoverNormsServiceImpl();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(changeoverNormsService, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get("lineChangeoverNorms", "lineChangeoverNorms")).willReturn(dataDefinition);
        given(dataDefinition.find()).willReturn(searchCriteria);
        PowerMockito.mockStatic(SearchRestrictions.class);

    }

    private SearchCriteriaBuilder searchMatchingChangeroverNorms(final Entity fromTechnology, final Entity toTechnology,
            final Entity fromTechnologyGroup, final Entity toTechnologyGroup, final Entity producionLine) {

        when(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, fromTechnology)).thenReturn(criterion);
        when(searchCriteria.add(criterion)).thenReturn(searchCriteria);

        when(SearchRestrictions.belongsTo(TO_TECHNOLOGY, toTechnology)).thenReturn(criterion);
        when(searchCriteria.add(criterion)).thenReturn(searchCriteria);

        when(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, fromTechnologyGroup)).thenReturn(criterion);
        when(searchCriteria.add(criterion)).thenReturn(searchCriteria);

        when(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, toTechnologyGroup)).thenReturn(criterion);
        when(searchCriteria.add(criterion)).thenReturn(searchCriteria);

        when(SearchRestrictions.belongsTo(PRODUCTION_LINE, productionLine)).thenReturn(criterion);
        when(searchCriteria.add(criterion)).thenReturn(searchCriteria);
        return searchCriteria;
    }

    @Test
    public void shouldReturnMatchingChangeoverNormsFromTechToTechForLine() throws Exception {
        // given
        fromTechnologyGroup = null;
        toTechnologyGroup = null;

        when(fromTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(toTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(toTechnologyGroup);

        searchCriteria = searchMatchingChangeroverNorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup,
                productionLine);

        when(searchCriteria.uniqueResult()).thenReturn(changeover);
        // when
        changeoverNormsService.matchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertNotNull(changeover);
    }

    @Test
    @Ignore
    public void shouldReturnMatchingChangeoverNormsFromTechToTechWithoutLine() throws Exception {
        // given

        fromTechnologyGroup = null;
        toTechnologyGroup = null;
        when(fromTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(toTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP)).thenReturn(toTechnologyGroup);

        searchCriteria = searchMatchingChangeroverNorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup,
                productionLine);

        when(searchCriteria.uniqueResult()).thenReturn(null);
        // when
        Entity changeoverNorms = changeoverNormsService.matchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        // then

        Assert.assertNotNull(changeoverNorms);
    }

}
