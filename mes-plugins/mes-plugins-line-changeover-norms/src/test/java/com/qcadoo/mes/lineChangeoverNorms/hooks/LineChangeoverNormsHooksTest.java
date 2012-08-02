package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.NUMBER;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class LineChangeoverNormsHooksTest {

    private static final String L_ERROR = "lineChangeoverNorms.lineChangeoverNorm.fieldIsRequired";

    private LineChangeoverNormsHooks hooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity entity, fromTechnology, toTechnology, productionLine, changeover, fromTechnologyGroup, toTechnologyGroup;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private SearchCriteriaBuilder searchCriteria;

    @Mock
    private SearchCriterion criterion;

    @Mock
    FieldDefinition field;

    @Before
    public void init() {
        hooks = new LineChangeoverNormsHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "dataDefinitionService", dataDefinitionService);
        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    private SearchCriteriaBuilder searchMatchingChangeroverNorms(final Entity fromTechnology, final Entity toTechnology,
            final Entity fromTechnologyGroup, final Entity toTechnologyGroup, final Entity producionLine) {
        given(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, fromTechnology)).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        given(SearchRestrictions.belongsTo(TO_TECHNOLOGY, toTechnology)).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        given(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, fromTechnologyGroup)).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        given(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, toTechnologyGroup)).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        given(SearchRestrictions.belongsTo(PRODUCTION_LINE, productionLine)).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        given(SearchRestrictions.ne("id", entity.getId())).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        return searchCriteria;
    }

    @Test
    public void shouldAddErrorForEntityWhenNotUnique() throws Exception {
        // given
        Long id = 1L;
        String changeoverNumber = "0002";

        given(entity.getId()).willReturn(id);
        given(entity.getBelongsToField(FROM_TECHNOLOGY)).willReturn(fromTechnology);
        given(entity.getBelongsToField(TO_TECHNOLOGY)).willReturn(toTechnology);
        given(entity.getBelongsToField(FROM_TECHNOLOGY_GROUP)).willReturn(fromTechnologyGroup);
        given(entity.getBelongsToField(TO_TECHNOLOGY_GROUP)).willReturn(toTechnologyGroup);
        given(entity.getBelongsToField(PRODUCTION_LINE)).willReturn(productionLine);
        given(
                dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                        LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)).willReturn(dataDefinition);
        given(dataDefinition.find()).willReturn(searchCriteria);

        searchMatchingChangeroverNorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup, productionLine);
        given(searchCriteria.uniqueResult()).willReturn(changeover);

        given(changeover.getStringField(NUMBER)).willReturn(changeoverNumber);

        // when
        hooks.checkUniqueNorms(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("lineChangeoverNorms.lineChangeoverNorm.notUnique", changeoverNumber);
    }

    @Test
    public void shouldReturnErrorWhenRequiredFieldForTechnologyIsNotFill() throws Exception {
        // given
        given(entity.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE)).willReturn("01forTechnology");
        given(entity.getBelongsToField(FROM_TECHNOLOGY)).willReturn(null);
        given(entity.getDataDefinition()).willReturn(dataDefinition);
        given(dataDefinition.getField(FROM_TECHNOLOGY)).willReturn(field);

        // when
        boolean result = hooks.checkRequiredField(dataDefinition, entity);

        // then
        Assert.isTrue(!result);
        verify(entity).addError(field, L_ERROR);
    }

    @Test
    public void shouldReturnErrorWhenRequiredFieldForTechnologyGroupIsNotFill() throws Exception {
        // given
        given(entity.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE)).willReturn("02forTechnologyGroup");
        given(entity.getBelongsToField(FROM_TECHNOLOGY_GROUP)).willReturn(fromTechnologyGroup);
        given(entity.getBelongsToField(TO_TECHNOLOGY_GROUP)).willReturn(null);

        given(entity.getDataDefinition()).willReturn(dataDefinition);
        given(dataDefinition.getField(TO_TECHNOLOGY_GROUP)).willReturn(field);

        // when
        boolean result = hooks.checkRequiredField(dataDefinition, entity);

        // then
        Assert.isTrue(!result);
        verify(entity).addError(field, L_ERROR);
    }

}
