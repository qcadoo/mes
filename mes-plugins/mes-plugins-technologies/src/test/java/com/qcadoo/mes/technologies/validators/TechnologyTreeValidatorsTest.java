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
package com.qcadoo.mes.technologies.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class TechnologyTreeValidatorsTest {

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    private TechnologyTreeValidators technologyTreeValidators;

    @Mock
    private TechnologyTreeValidationService technologyTreeValidationService;

    @Mock
    private DataDefinition techDataDefinition, tocDataDefinition, opicDataDefinition, opocDataDefinition;

    @Mock
    private Entity technology, existingTechnology, toc, product, opic, opoc;

    @Mock
    private EntityTree tree;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyTreeValidators = new TechnologyTreeValidators();

        ReflectionTestUtils
                .setField(technologyTreeValidators, "technologyTreeValidationService", technologyTreeValidationService);

        given(technology.getDataDefinition()).willReturn(techDataDefinition);
        given(techDataDefinition.getName()).willReturn(TechnologiesConstants.MODEL_TECHNOLOGY);

        given(toc.getDataDefinition()).willReturn(tocDataDefinition);
        given(tocDataDefinition.getName()).willReturn(TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        given(toc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY)).willReturn(technology);

        given(opic.getDataDefinition()).willReturn(opicDataDefinition);
        given(opicDataDefinition.getName()).willReturn(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        given(opic.getBelongsToField(L_OPERATION_COMPONENT)).willReturn(toc);

        given(opoc.getDataDefinition()).willReturn(opocDataDefinition);
        given(opocDataDefinition.getName()).willReturn(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
        given(opoc.getBelongsToField(L_OPERATION_COMPONENT)).willReturn(toc);
    }

    @Test
    public void shouldAddMessagesCorrectly() {
        // given
        String messageKey = "technologies.technology.validate.global.error.subOperationsProduceTheSameProductThatIsConsumed";
        String parentNode = "1.";
        String productName = "name";
        String productNumber = "abc123";

        Long techId = 1L;
        given(technology.getStringField("state")).willReturn("02accepted");
        given(technology.getId()).willReturn(techId);
        given(techDataDefinition.get(techId)).willReturn(technology);
        given(technology.getTreeField("operationComponents")).willReturn(tree);

        given(product.getStringField("name")).willReturn(productName);
        given(product.getStringField("number")).willReturn(productNumber);

        Map<String, Set<Entity>> nodesMap = Maps.newHashMap();
        Set<Entity> productSet = Sets.newHashSet();
        productSet.add(product);
        nodesMap.put("1.", productSet);

        given(technologyTreeValidationService.checkConsumingTheSameProductFromManySubOperations(tree)).willReturn(nodesMap);

        // when
        technologyTreeValidators.checkConsumingTheSameProductFromManySubOperations(techDataDefinition, technology);

        // then
        Mockito.verify(technology).addGlobalError(messageKey, parentNode, productName, productNumber);
    }

    @Test
    public final void shouldInvalidateAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldInvalidateAlreadyAcceptedInactiveTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(technology.isActive()).willReturn(false);
        given(existingTechnology.isActive()).willReturn(false);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldInvalidateAlreadyAcceptedActiveTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(technology.isActive()).willReturn(true);
        given(existingTechnology.isActive()).willReturn(true);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldValidateAcceptedTechnologyDuringEntityActivation() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(technology.isActive()).willReturn(true);
        given(existingTechnology.isActive()).willReturn(false);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateAcceptedTechnologyDuringEntityDeactivation() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(technology.isActive()).willReturn(false);
        given(existingTechnology.isActive()).willReturn(true);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateJustCreatedTechnology() {
        // given
        given(technology.getId()).willReturn(null);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateTechnologyDuringAccepting() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateTechnologyDuringWithdrawing() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.OUTDATED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateTechnologyDraft() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateTechnologyIfNotChanged() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(technology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(techDataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateTocBelongingToDraftTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(tocDataDefinition, toc);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateUnchangedTocBelongingToAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        given(toc.getId()).willReturn(202L);
        given(tocDataDefinition.get(202L)).willReturn(toc);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(tocDataDefinition, toc);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldInvalidateChangedTocBelongingToAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        given(toc.getId()).willReturn(202L);
        final Entity existingToc = mock(Entity.class);
        given(tocDataDefinition.get(202L)).willReturn(existingToc);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(tocDataDefinition, toc);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldValidateOpicBelongingToDraftTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(opicDataDefinition, opic);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateOpocBelongingToDraftTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.DRAFT.getStringValue());

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(opocDataDefinition, opoc);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateUnchangedOpicBelongingToAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        given(opic.getId()).willReturn(303L);
        given(opicDataDefinition.get(303L)).willReturn(opic);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(opicDataDefinition, opic);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldValidateUnchangedOpocBelongingToAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        given(opoc.getId()).willReturn(404L);
        given(opocDataDefinition.get(404L)).willReturn(opoc);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(opocDataDefinition, opoc);

        // then
        assertTrue(isValid);
    }

    @Test
    public final void shouldInvalidateChangedOpicBelongingToAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        given(opic.getId()).willReturn(505L);
        final Entity existingOpic = mock(Entity.class);
        given(opicDataDefinition.get(505L)).willReturn(existingOpic);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(opicDataDefinition, opic);

        // then
        assertFalse(isValid);
    }

    @Test
    public final void shouldInvalidateChangedOpocBelongingToAlreadyAcceptedTechnology() {
        // given
        given(technology.getId()).willReturn(101L);
        given(techDataDefinition.get(101L)).willReturn(existingTechnology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());
        given(existingTechnology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyState.ACCEPTED.getStringValue());

        given(opoc.getId()).willReturn(606L);
        final Entity existingOpoc = mock(Entity.class);
        given(opocDataDefinition.get(606L)).willReturn(existingOpoc);

        // when
        final boolean isValid = technologyTreeValidators.invalidateIfBelongsToAcceptedTechnology(opocDataDefinition, opoc);

        // then
        assertFalse(isValid);
    }
}
