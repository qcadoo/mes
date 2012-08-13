/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
    private Entity changeoverNorm, fromTechnology, toTechnology, productionLine, changeover, fromTechnologyGroup,
            toTechnologyGroup;

    @Mock
    private DataDefinition changeoverNormDD;

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

        given(SearchRestrictions.ne("id", changeoverNorm.getId())).willReturn(criterion);
        given(searchCriteria.add(criterion)).willReturn(searchCriteria);

        return searchCriteria;
    }

    @Test
    public void shouldAddErrorForEntityWhenNotUnique() {
        // given
        Long id = 1L;
        String changeoverNumber = "0002";

        given(changeoverNorm.getId()).willReturn(id);
        given(changeoverNorm.getBelongsToField(FROM_TECHNOLOGY)).willReturn(fromTechnology);
        given(changeoverNorm.getBelongsToField(TO_TECHNOLOGY)).willReturn(toTechnology);
        given(changeoverNorm.getBelongsToField(FROM_TECHNOLOGY_GROUP)).willReturn(fromTechnologyGroup);
        given(changeoverNorm.getBelongsToField(TO_TECHNOLOGY_GROUP)).willReturn(toTechnologyGroup);
        given(changeoverNorm.getBelongsToField(PRODUCTION_LINE)).willReturn(productionLine);
        given(
                dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                        LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)).willReturn(changeoverNormDD);
        given(changeoverNormDD.find()).willReturn(searchCriteria);

        searchMatchingChangeroverNorms(fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup, productionLine);
        given(searchCriteria.uniqueResult()).willReturn(changeover);

        given(changeover.getStringField(NUMBER)).willReturn(changeoverNumber);

        // when
        hooks.checkUniqueNorms(changeoverNormDD, changeoverNorm);

        // then
        verify(changeoverNorm).addGlobalError("lineChangeoverNorms.lineChangeoverNorm.notUnique", changeoverNumber);
    }

    @Test
    public void shouldReturnErrorWhenRequiredFieldForTechnologyIsNotFill() {
        // given
        given(changeoverNorm.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE)).willReturn("01forTechnology");
        given(changeoverNorm.getBelongsToField(FROM_TECHNOLOGY)).willReturn(null);
        given(changeoverNorm.getDataDefinition()).willReturn(changeoverNormDD);
        given(changeoverNormDD.getField(FROM_TECHNOLOGY)).willReturn(field);

        // when
        boolean result = hooks.checkRequiredField(changeoverNormDD, changeoverNorm);

        // then
        Assert.isTrue(!result);

        verify(changeoverNorm).addError(field, L_ERROR);
    }

    @Test
    public void shouldReturnErrorWhenRequiredFieldForTechnologyGroupIsNotFill() throws Exception {
        // given
        given(changeoverNorm.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE)).willReturn("02forTechnologyGroup");
        given(changeoverNorm.getBelongsToField(FROM_TECHNOLOGY_GROUP)).willReturn(fromTechnologyGroup);
        given(changeoverNorm.getBelongsToField(TO_TECHNOLOGY_GROUP)).willReturn(null);

        given(changeoverNorm.getDataDefinition()).willReturn(changeoverNormDD);
        given(changeoverNormDD.getField(TO_TECHNOLOGY_GROUP)).willReturn(field);

        // when
        boolean result = hooks.checkRequiredField(changeoverNormDD, changeoverNorm);

        // then
        Assert.isTrue(!result);

        verify(changeoverNorm).addError(field, L_ERROR);
    }

}
