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
package com.qcadoo.mes.supplyNegotiations.hooks;

import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.FARTHEST_LIMIT_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.DUE_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.NEGOTIATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class NegotiationProductHooksTest {

    private NegotiationProductHooks negotiationProductHooks;

    @Mock
    private DataDefinition negotiationProductDD, negotiationDD;

    @Mock
    private Entity negotiationProduct, negotiation;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private Date dueDate;

    @Mock
    private Date farthestLimitDate;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        negotiationProductHooks = new NegotiationProductHooks();

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfNegotiationProductAlreadyExists() throws Exception {
        // given
        given(negotiationProductDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(negotiationProduct);

        // when
        boolean result = negotiationProductHooks.checkIfNegotiationProductAlreadyExists(negotiationProductDD, negotiationProduct);

        // then
        assertFalse(result);

        verify(negotiationProduct).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfNegotiationProductAlreadyExists() throws Exception {
        // given
        given(negotiationProductDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        boolean result = negotiationProductHooks.checkIfNegotiationProductAlreadyExists(negotiationProductDD, negotiationProduct);

        // then
        assertTrue(result);

        verify(negotiationProduct, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldntUpdateFarthestLimitDate() {
        // given
        given(negotiationProduct.getBelongsToField(NEGOTIATION)).willReturn(negotiation);
        given(negotiationProduct.getField(DUE_DATE)).willReturn(dueDate);
        given(negotiation.getField(FARTHEST_LIMIT_DATE)).willReturn(farthestLimitDate);

        // when
        negotiationProductHooks.updateFarestLimitDate(negotiationProductDD, negotiationProduct);

        // then
        verify(negotiation, never()).setField(Mockito.anyString(), Mockito.any());
        verify(negotiationDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldUpdateFarthestLimitDate() {
        // given
        given(negotiationProduct.getBelongsToField(NEGOTIATION)).willReturn(negotiation);
        given(negotiationProduct.getField(DUE_DATE)).willReturn(dueDate);
        given(negotiation.getField(FARTHEST_LIMIT_DATE)).willReturn(null);

        given(negotiation.getDataDefinition()).willReturn(negotiationDD);

        // when
        negotiationProductHooks.updateFarestLimitDate(negotiationProductDD, negotiationProduct);

        // then
        verify(negotiation).setField(Mockito.anyString(), Mockito.any());
        verify(negotiationDD).save(Mockito.any(Entity.class));
    }

}
