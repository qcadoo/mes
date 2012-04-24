/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class ParameterServiceTest {

    @Test
    public void shouldReturnExistingParameterEntityId() throws Exception {
        // given
        Entity parameter = Mockito.mock(Entity.class);
        given(parameter.getId()).willReturn(13L);

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).uniqueResult()).willReturn(parameter);

        ParameterService parameterService = new ParameterService();
        setField(parameterService, "dataDefinitionService", dataDefinitionService);

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

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).uniqueResult()).willReturn(parameter);

        ParameterService parameterService = new ParameterService();
        setField(parameterService, "dataDefinitionService", dataDefinitionService);

        // when
        Entity existingParameter = parameterService.getParameter();

        // then
        assertEquals(parameter, existingParameter);
    }

    @Test
    public void shouldReturnNewParameterEntity() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        CurrencyService currencyService = mock(CurrencyService.class);
        Entity newParameter = mock(Entity.class);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)).willReturn(
                dataDefinition);
        given(dataDefinition.find().setMaxResults(1).uniqueResult()).willReturn(null);
        given(dataDefinition.create()).willReturn(newParameter);

        Entity savedEntity = mock(Entity.class);
        given(savedEntity.getId()).willReturn(15L);

        given(dataDefinition.find().setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        given(dataDefinition.save(newParameter)).willReturn(savedEntity);
        Entity currency = mock(Entity.class);
        given(currencyService.getCurrentCurrency()).willReturn(currency);

        ParameterService parameterService = new ParameterService();

        setField(parameterService, "dataDefinitionService", dataDefinitionService);
        setField(parameterService, "currencyService", currencyService);
        // when
        Entity returnedParameter = parameterService.getParameter();

        // then
        verify(dataDefinition).save(newParameter);
        verify(newParameter).setField("currency", currency);
        assertEquals(Long.valueOf(15L), returnedParameter.getId());
    }

    @Test
    public void shouldReturnNewGenealogyAttributeId() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        CurrencyService currencyService = mock(CurrencyService.class);

        Entity newParameter = mock(Entity.class);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)).willReturn(
                dataDefinition);
        given(dataDefinition.find().setMaxResults(1).uniqueResult()).willReturn(null);
        given(dataDefinition.create()).willReturn(newParameter);

        Entity savedEntity = mock(Entity.class);
        given(savedEntity.getId()).willReturn(15L);

        given(dataDefinition.find().setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        given(dataDefinition.save(newParameter)).willReturn(savedEntity);
        Entity currency = mock(Entity.class);
        given(currencyService.getCurrentCurrency()).willReturn(currency);

        ParameterService parameterService = new ParameterService();
        setField(parameterService, "dataDefinitionService", dataDefinitionService);
        setField(parameterService, "currencyService", currencyService);
        // when
        Long id = parameterService.getParameterId();

        // then
        verify(dataDefinition).save(newParameter);
        verify(newParameter).setField("currency", currency);
        assertEquals(Long.valueOf(15L), id);
    }

}
