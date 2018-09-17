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
package com.qcadoo.mes.advancedGenealogy.hooks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.constants.BatchNumberUniqueness;
import com.qcadoo.mes.advancedGenealogy.constants.ParameterFieldsAG;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.testing.model.EntityTestUtils;

public class BatchModelValidatorsTest {

    private static final List<Entity> EMPTY_ENTITY_LIST = ImmutableList.of();

    private static final List<Entity> NON_EMPTY_ENTITY_LIST = ImmutableList.of(mock(Entity.class));

    private Entity batch;

    @Mock
    private DataDefinition batchDD;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private ParameterService parameterService;

    private BatchModelValidators batchModelValidators;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        batchModelValidators = new BatchModelValidators();
        ReflectionTestUtils.setField(batchModelValidators, "parameterService", parameterService);
        given(batchDD.find()).willReturn(searchCriteriaBuilder);

        batch = EntityTestUtils.mockEntity(batchDD);
    }

    private void stubNumberUniquenessParameter(final BatchNumberUniqueness uniqueness) {
        Entity parameter = EntityTestUtils.mockEntity();
        given(parameterService.getParameter()).willReturn(parameter);
        EntityTestUtils.stubStringField(parameter, ParameterFieldsAG.BATCH_NUMBER_UNIQUENESS, uniqueness.getStringValue());
    }

    private ArgumentCaptor<SearchCriterion> stubSearchResultsAndReturnCriteriaCaptor(final Collection<Entity> entities) {
        given(batchDD.find()).willReturn(searchCriteriaBuilder);
        SearchResult searchResult = mock(SearchResult.class);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchCriteriaBuilder.setMaxResults(anyInt())).willReturn(searchCriteriaBuilder);

        // stub .list() results
        given(searchResult.getTotalNumberOfEntities()).willReturn(entities.size());
        given(searchResult.getEntities()).willAnswer(new Answer<List<Entity>>() {

            @Override
            public List<Entity> answer(final InvocationOnMock ignored) throws Throwable {
                return Lists.newArrayList(entities);
            }
        });

        // stub unique result
        given(searchCriteriaBuilder.uniqueResult()).willAnswer(new Answer<Entity>() {

            @Override
            public Entity answer(final InvocationOnMock ignored) throws Throwable {
                return Iterables.getFirst(entities, null);
            }
        });

        // build argumentCaptor
        ArgumentCaptor<SearchCriterion> criterionCaptor = ArgumentCaptor.forClass(SearchCriterion.class);
        given(searchCriteriaBuilder.add(criterionCaptor.capture())).willReturn(searchCriteriaBuilder);
        return criterionCaptor;
    }

    private void assertValidationFail(final boolean result) {
        assertFalse(result);
        verify(batch).addError(Mockito.eq(batchDD.getField(BatchFields.NUMBER)), Mockito.anyString());
    }

    private void assertValidationSuccess(final boolean result) {
        assertTrue(result);
        verify(batch, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueIfUniquenessIsGloballyAndBatchNumberIsUnique() {
        // given
        stubNumberUniquenessParameter(BatchNumberUniqueness.GLOBALLY);
        stubSearchResultsAndReturnCriteriaCaptor(EMPTY_ENTITY_LIST);

        // when
        boolean result = batchModelValidators.checkIfBatchNumberIsUnique(batchDD, batch);

        // then
        assertValidationSuccess(result);
    }

    @Test
    public void shouldReturnFalseIfUniquenessIsGloballyAndBatchNumberIsNotUnique() {
        // given
        stubNumberUniquenessParameter(BatchNumberUniqueness.GLOBALLY);
        stubSearchResultsAndReturnCriteriaCaptor(NON_EMPTY_ENTITY_LIST);

        // when
        boolean result = batchModelValidators.checkIfBatchNumberIsUnique(batchDD, batch);

        // then
        assertValidationFail(result);
    }

    @Test
    public void shouldReturnTrueIfUniquenessIsSupplierAndBatchNumberIsUnique() {
        // given
        stubNumberUniquenessParameter(BatchNumberUniqueness.SUPPLIER);
        stubSearchResultsAndReturnCriteriaCaptor(EMPTY_ENTITY_LIST);

        // when
        boolean result = batchModelValidators.checkIfBatchNumberIsUnique(batchDD, batch);

        // then
        assertValidationSuccess(result);
    }

    @Test
    public void shouldReturnFalseIfUniquenessIsSupplierAndBatchNumberIsNotUnique() {
        // given
        stubNumberUniquenessParameter(BatchNumberUniqueness.SUPPLIER);
        stubSearchResultsAndReturnCriteriaCaptor(NON_EMPTY_ENTITY_LIST);

        // when
        boolean result = batchModelValidators.checkIfBatchNumberIsUnique(batchDD, batch);

        // then
        assertValidationFail(result);
    }

    @Test
    public void shouldReturnTrueIfUniquenessIsSupplierAndProductAndBatchNumberIsUnique() {
        // given
        stubNumberUniquenessParameter(BatchNumberUniqueness.SUPPLIER_AND_PRODUCT);
        stubSearchResultsAndReturnCriteriaCaptor(EMPTY_ENTITY_LIST);

        // when
        boolean result = batchModelValidators.checkIfBatchNumberIsUnique(batchDD, batch);

        // then
        assertValidationSuccess(result);
    }

    @Test
    public void shouldReturnFalseIfUniquenessIsSupplierAndProductAndBatchNumberIsNotUnique() {
        // given
        stubNumberUniquenessParameter(BatchNumberUniqueness.SUPPLIER_AND_PRODUCT);
        stubSearchResultsAndReturnCriteriaCaptor(NON_EMPTY_ENTITY_LIST);

        // when
        boolean result = batchModelValidators.checkIfBatchNumberIsUnique(batchDD, batch);

        // then
        assertValidationFail(result);
    }

}
