/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.timeNormsForOperations.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyValidatorsTest {

    private static final String SOME_UNIT = "someUnit";

    private TechnologyValidators technologyValidators;

    @Mock
    private Entity technology;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity techOpComponent;

    @Mock
    private Entity product;

    @Mock
    private Entity outputProduct;

    @Mock
    private Entity techInstanceOpComponent;

    @Mock
    private NormService normService;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyValidators = new TechnologyValidators();
        ReflectionTestUtils.setField(technologyValidators, "productQuantitiyService", productQuantitiesService);
    }

    @Test
    public void shouldAlwaysPassIfTheTechnologyIsInDraftState() {
        // given
        given(technology.getStringField("state")).willReturn("01draft");

        // when
        boolean isValid = technologyValidators.checkOperationOutputQuantities(dataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTocReturnFalseOnCreateIfUnitIsEmpty() {
        // given
        given(techOpComponent.getId()).willReturn(null);
        given(techOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(null);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInTechnologyMatch(dataDefinition, techOpComponent);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTocReturnTrueOnCreateIfUnitIsNotEmpty() {
        // given
        given(techOpComponent.getId()).willReturn(null);
        given(techOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(SOME_UNIT);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInTechnologyMatch(dataDefinition, techOpComponent);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTocReturnFalseOnUpdateIfUnitIsEmpty() {
        // given
        given(techOpComponent.getId()).willReturn(1L);
        given(techOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(null);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInTechnologyMatch(dataDefinition, techOpComponent);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTocReturnFalseOnUpdateIfUnitIsNotEmptyButDoNotMatchProduct() {
        // given
        given(techOpComponent.getId()).willReturn(1L);
        given(techOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(SOME_UNIT);
        given(productQuantitiesService.getOutputProductsFromOperationComponent(techOpComponent)).willReturn(outputProduct);
        given(outputProduct.getBelongsToField(TechnologyFields.PRODUCT)).willReturn(product);
        given(product.getStringField(ProductFields.UNIT)).willReturn("someAnoherUnit");

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInTechnologyMatch(dataDefinition, techOpComponent);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTocReturnTrueOnUpdateIfUnitIsNotEmptyAndMatchProduct() {
        // given
        given(techOpComponent.getId()).willReturn(1L);
        given(techOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(SOME_UNIT);
        given(productQuantitiesService.getOutputProductsFromOperationComponent(techOpComponent)).willReturn(outputProduct);
        given(outputProduct.getBelongsToField(TechnologyFields.PRODUCT)).willReturn(product);
        given(product.getStringField(ProductFields.UNIT)).willReturn(SOME_UNIT);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInTechnologyMatch(dataDefinition, techOpComponent);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTiocReturnFalseOnCreateIfUnitIsEmpty() {
        // given
        given(techInstanceOpComponent.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT))
                .willReturn(techOpComponent);

        given(techInstanceOpComponent.getId()).willReturn(null);
        given(techInstanceOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(null);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInInstanceTechnologyMatch(dataDefinition,
                techInstanceOpComponent);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTiocReturnTrueOnCreateIfUnitIsNotEmpty() {
        // given
        given(techInstanceOpComponent.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT))
                .willReturn(techOpComponent);

        given(techInstanceOpComponent.getId()).willReturn(null);
        given(techInstanceOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(
                SOME_UNIT);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInInstanceTechnologyMatch(dataDefinition,
                techInstanceOpComponent);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTiocReturnFalseOnUpdateIfUnitIsEmpty() {
        // given
        given(techInstanceOpComponent.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT))
                .willReturn(techOpComponent);

        given(techInstanceOpComponent.getId()).willReturn(1L);
        given(techInstanceOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(null);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInInstanceTechnologyMatch(dataDefinition,
                techInstanceOpComponent);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTiocReturnFalseOnUpdateIfUnitIsNotEmptyButDoNotMatchProduct() {
        // given
        given(techInstanceOpComponent.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT))
                .willReturn(techOpComponent);

        given(techInstanceOpComponent.getId()).willReturn(1L);
        given(techInstanceOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(
                SOME_UNIT);
        given(productQuantitiesService.getOutputProductsFromOperationComponent(techOpComponent)).willReturn(outputProduct);
        given(outputProduct.getBelongsToField(TechnologyFields.PRODUCT)).willReturn(product);
        given(product.getStringField(ProductFields.UNIT)).willReturn("someAnoherUnit");

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInInstanceTechnologyMatch(dataDefinition,
                techInstanceOpComponent);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldCheckUnitsForTiocReturnTrueOnUpdateIfUnitIsNotEmptyAndMatchProduct() {
        // given
        given(techInstanceOpComponent.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT))
                .willReturn(techOpComponent);

        given(techInstanceOpComponent.getId()).willReturn(1L);
        given(techInstanceOpComponent.getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT)).willReturn(
                SOME_UNIT);
        given(productQuantitiesService.getOutputProductsFromOperationComponent(techOpComponent)).willReturn(outputProduct);
        given(outputProduct.getBelongsToField(TechnologyFields.PRODUCT)).willReturn(product);
        given(product.getStringField(ProductFields.UNIT)).willReturn(SOME_UNIT);

        // when
        final boolean isValid = technologyValidators.checkIfUnitsInInstanceTechnologyMatch(dataDefinition,
                techInstanceOpComponent);

        // then
        assertTrue(isValid);
    }
}
