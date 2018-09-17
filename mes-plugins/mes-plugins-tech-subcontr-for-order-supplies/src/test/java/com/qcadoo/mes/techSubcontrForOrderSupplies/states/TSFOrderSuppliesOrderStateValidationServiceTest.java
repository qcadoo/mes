/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.techSubcontrForOrderSupplies.states;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class TSFOrderSuppliesOrderStateValidationServiceTest {
/*
    private TSFOrderSuppliesOrderStateValidationService tSFOrderSuppliesOrderStateValidationService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private StateChangeContext stateChangeContext;

    @Mock
    private DataDefinition technologyOperationComponentDD;

    @Mock
    private Entity order, technology;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> entities;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SearchRestrictions.class);

        tSFOrderSuppliesOrderStateValidationService = new TSFOrderSuppliesOrderStateValidationService();

        ReflectionTestUtils.setField(tSFOrderSuppliesOrderStateValidationService, "dataDefinitionService", dataDefinitionService);

        given(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)).willReturn(technologyOperationComponentDD);
    }

    @Test
    public void shouldAddErrorWhenValidatorOnAcceptedIfAreSubcontractedOperations() {
        // given
        given(stateChangeContext.getOwner()).willReturn(order);

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        given(technologyOperationComponentDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(entities);
        given(entities.isEmpty()).willReturn(false);

        // when
        tSFOrderSuppliesOrderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext).addFieldValidationError(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void shouldntAddErrorWhenValidatorOnAcceptedIfArentSubcontractedOperations() {
        // given
        given(stateChangeContext.getOwner()).willReturn(order);

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        given(technologyOperationComponentDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(entities);
        given(entities.isEmpty()).willReturn(true);

        // when
        tSFOrderSuppliesOrderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void shouldntAddErrorWhenValidatorOnAcceptedIfForEach() {
        // given
        given(stateChangeContext.getOwner()).willReturn(order);

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());

        // when
        tSFOrderSuppliesOrderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.anyString());
    }
*/
}
