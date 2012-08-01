package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.NUMBER;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(SearchRestrictions.ne("id", entity.getId())).thenReturn(criterion);
        when(searchCriteria.add(criterion)).thenReturn(searchCriteria);

        return searchCriteria;
    }

    @Test
    public void shouldAddErrorForEntityWhenNotUnique() throws Exception {
        // given
        Long id = 1L;
        String changeoverNumber = "0002";
        when(entity.getId()).thenReturn(id);
        when(entity.getBelongsToField(FROM_TECHNOLOGY)).thenReturn(fromTechnology);
        when(entity.getBelongsToField(TO_TECHNOLOGY)).thenReturn(toTechnology);
        when(entity.getBelongsToField(FROM_TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(entity.getBelongsToField(TO_TECHNOLOGY_GROUP)).thenReturn(toTechnologyGroup);
        when(entity.getBelongsToField(PRODUCTION_LINE)).thenReturn(productionLine);
        when(
                dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                        LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)).thenReturn(dataDefinition);
        when(dataDefinition.find()).thenReturn(searchCriteria);

        searchMatchingChangeroverNorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup, productionLine);
        when(searchCriteria.uniqueResult()).thenReturn(changeover);

        when(changeover.getStringField(NUMBER)).thenReturn(changeoverNumber);
        // when
        hooks.checkUniqueNorms(dataDefinition, entity);
        // then
        verify(entity).addGlobalError("lineChangeoverNorms.lineChangeoverNorm.notUnique", changeoverNumber);
    }

    @Test
    public void shouldReturnErrorWhenRequiredFieldForTechnologyIsNotFill() throws Exception {
        // given
        final String error = "lineChangeoverNorms.lineChangeoverNorm.fieldIsRequired";
        when(entity.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE)).thenReturn("01forTechnology");
        when(entity.getBelongsToField(FROM_TECHNOLOGY)).thenReturn(null);
        when(entity.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.getField(FROM_TECHNOLOGY)).thenReturn(field);
        // when
        boolean result = hooks.checkRequiredField(dataDefinition, entity);
        // then
        Assert.isTrue(!result);
        verify(entity).addError(field, error);
    }

    @Test
    public void shouldReturnErrorWhenRequiredFieldForTechnologyGroupIsNotFill() throws Exception {
        // given
        final String error = "lineChangeoverNorms.lineChangeoverNorm.fieldIsRequired";
        when(entity.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE)).thenReturn("02forTechnologyGroup");
        when(entity.getBelongsToField(FROM_TECHNOLOGY_GROUP)).thenReturn(fromTechnologyGroup);
        when(entity.getBelongsToField(TO_TECHNOLOGY_GROUP)).thenReturn(null);

        when(entity.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.getField(TO_TECHNOLOGY_GROUP)).thenReturn(field);
        // when
        boolean result = hooks.checkRequiredField(dataDefinition, entity);
        // then
        Assert.isTrue(!result);
        verify(entity).addError(field, error);
    }
}
