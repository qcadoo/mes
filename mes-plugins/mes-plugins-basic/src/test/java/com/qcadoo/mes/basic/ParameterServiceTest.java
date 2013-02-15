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
package com.qcadoo.mes.basic;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PARAMETER;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;

public class ParameterServiceTest {

    private ParameterService parameterService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        parameterService = new ParameterService();
        ReflectionTestUtils.setField(parameterService, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PARAMETER)).willReturn(parameterDD);
        given(parameterDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(Mockito.anyInt())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setCacheable(Mockito.anyBoolean())).willReturn(searchCriteriaBuilder);
    }

    @Test
    public void shouldReturnExistingParameterEntityId() throws Exception {
        // given
        Entity parameter = Mockito.mock(Entity.class);
        given(parameter.getId()).willReturn(13L);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(parameter);

        // when
        Long id = parameterService.getParameterId();

        // then
        assertEquals(Long.valueOf(13L), id);
    }

    @Test
    public void shouldReturnExistingParameterEntity() throws Exception {
        // given
        Entity parameter = Mockito.mock(Entity.class);
        given(parameter.getId()).willReturn(13L);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(parameter);
        given(parameter.isValid()).willReturn(true);

        // when
        Entity existingParameter = parameterService.getParameter();

        // then
        assertEquals(parameter, existingParameter);
    }

    @Test
    public void shouldReturnNewParameterEntity() throws Exception {
        // given
        Entity parameter = mock(Entity.class);
        given(parameter.isValid()).willReturn(true);
        given(parameterDD.create()).willReturn(parameter);

        Entity savedParameter = mock(Entity.class);
        given(savedParameter.isValid()).willReturn(true);
        given(savedParameter.getId()).willReturn(15L);

        given(parameterDD.save(parameter)).willReturn(savedParameter);

        // when
        Entity returnedParameter = parameterService.getParameter();

        // then
        verify(parameterDD).save(parameter);
        assertEquals(Long.valueOf(15L), returnedParameter.getId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNewParameterEntityIsNotValid() throws Exception {
        // given
        Entity parameter = mock(Entity.class);
        given(parameter.isValid()).willReturn(true);
        given(parameterDD.create()).willReturn(parameter);

        Entity savedParameter = mock(Entity.class);
        given(savedParameter.isValid()).willReturn(false);

        given(parameterDD.save(parameter)).willReturn(savedParameter);

        // when
        parameterService.getParameter();
    }

}
