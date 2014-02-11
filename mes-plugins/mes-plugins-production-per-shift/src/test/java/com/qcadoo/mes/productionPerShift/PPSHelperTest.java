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
package com.qcadoo.mes.productionPerShift;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class PPSHelperTest {

    private PPSHelper ppsHelper;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent fieldComponent;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        ppsHelper = new PPSHelper();

        ReflectionTestUtils.setField(ppsHelper, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldReturnFalseWhenProgressTypeIsPlanned() throws Exception {
        // given
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(fieldComponent);
        given(fieldComponent.getFieldValue()).willReturn(PlannedProgressType.PLANNED.getStringValue());

        // when
        boolean result = ppsHelper.shouldHasCorrections(view);

        // then
        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenProgressTypeIsPlanned() throws Exception {
        // given
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(fieldComponent);
        given(fieldComponent.getFieldValue()).willReturn(PlannedProgressType.CORRECTED.getStringValue());

        // when
        boolean result = ppsHelper.shouldHasCorrections(view);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public final void shouldGetPpsForOrderReturnExistingPpsId() {
        // given
        Long givenOrderId = 1L;
        Long expectedPpsId = 50L;

        given(
                dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                        ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT)).willReturn(dataDefinition);
        SearchQueryBuilder searchCriteriaBuilder = mock(SearchQueryBuilder.class);
        given(dataDefinition.find("select id as ppsId from #productionPerShift_productionPerShift where order.id = :orderId"))
                .willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(Mockito.anyInt())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setLong(Mockito.anyString(), Mockito.eq(givenOrderId))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(entity);
        given(entity.getField("ppsId")).willReturn(expectedPpsId);

        // when
        final Long resultPpsId = ppsHelper.getPpsIdForOrder(givenOrderId);

        // then
        Assert.assertEquals(expectedPpsId, resultPpsId);
    }

    @Test
    public final void shouldGetPpsForOrderReturnNullIfPpsDoesNotExists() {
        // given
        Long givenOrderId = 1L;

        given(
                dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                        ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT)).willReturn(dataDefinition);
        SearchQueryBuilder searchCriteriaBuilder = mock(SearchQueryBuilder.class);
        given(dataDefinition.find("select id as ppsId from #productionPerShift_productionPerShift where order.id = :orderId"))
                .willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(Mockito.anyInt())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setLong(Mockito.anyString(), Mockito.eq(givenOrderId))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        final Long resultPpsId = ppsHelper.getPpsIdForOrder(givenOrderId);

        // then
        Assert.assertNull(resultPpsId);
    }

}