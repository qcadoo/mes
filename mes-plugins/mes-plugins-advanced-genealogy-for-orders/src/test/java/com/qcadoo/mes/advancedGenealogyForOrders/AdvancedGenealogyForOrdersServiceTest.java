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
package com.qcadoo.mes.advancedGenealogyForOrders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.qcadoo.mes.advancedGenealogyForOrders.constants.AdvancedGenealogyForOrdersConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.types.TreeType;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;

public class AdvancedGenealogyForOrdersServiceTest {

    private AdvancedGenealogyForOrdersService advancedGenealogyForOrdersService;

    private Entity trackingRecord;

    private DataDefinition trackingRecordDD;

    private DataDefinitionService dataDefinitionService;

    private EntityTreeUtilsService entityTreeUtilsService;

    @Before
    public final void init() {
        trackingRecord = mock(Entity.class);
        trackingRecordDD = mock(DataDefinition.class);
        dataDefinitionService = mock(DataDefinitionService.class);

        when(trackingRecord.getDataDefinition()).thenReturn(trackingRecordDD);
        when(trackingRecordDD.getName()).thenReturn("trackingRecord");
        when(dataDefinitionService.get(Mockito.anyString(), Mockito.anyString())).thenReturn(trackingRecordDD);

        entityTreeUtilsService = mock(EntityTreeUtilsService.class);

        advancedGenealogyForOrdersService = new AdvancedGenealogyForOrdersService();
        setField(advancedGenealogyForOrdersService, "dataDefinitionService", dataDefinitionService);
        setField(advancedGenealogyForOrdersService, "entityTreeUtilsService", entityTreeUtilsService);
    }

    @Test
    public final void shouldReturnTrueIfEntityIsTrackingRecordForOrdersType() throws Exception {
        // given
        when(trackingRecord.getStringField("entityType")).thenReturn("02forOrder");

        // when
        boolean result = AdvancedGenealogyForOrdersService.isTrackingRecordForOrder(trackingRecord);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfEntityIsNotTrackingRecordForOrdersType() throws Exception {
        // given
        when(trackingRecord.getStringField("entityType")).thenReturn("01simple");

        // when
        boolean result = AdvancedGenealogyForOrdersService.isTrackingRecordForOrder(trackingRecord);

        // then
        Assert.assertFalse(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void shouldThrowExceptionIfGivenEntityIsNotTrackingRecordType() throws Exception {
        // given
        when(trackingRecordDD.getName()).thenReturn("someWrongEntityTypeName");

        // when
        AdvancedGenealogyForOrdersService.isTrackingRecordForOrder(trackingRecord);
    }

    @Test
    public final void shouldBuildCorrectGenealogyProductInComponentList() throws Exception {
        // given

        Entity technologyOperationComponent = mock(Entity.class);
        Entity productInComponent = mock(Entity.class);
        Entity genealogyProductInComponent = mock(Entity.class);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        DataDefinition genealogyProductInComponentDD = mock(DataDefinition.class);

        EntityTree operationsTree = mock(EntityTree.class);
        EntityList inputProductsList = mock(EntityList.class);

        @SuppressWarnings("unchecked")
        Iterator<Entity> orderOperationsTreeIterator = mock(Iterator.class);
        @SuppressWarnings("unchecked")
        Iterator<Entity> inputProductsIterator = mock(Iterator.class);

        when(technologyOperationComponent.getHasManyField("operationProductInComponents")).thenReturn(null, inputProductsList);

        when(
                dataDefinitionService.get(AdvancedGenealogyForOrdersConstants.PLUGIN_IDENTIFIER,
                        AdvancedGenealogyForOrdersConstants.MODEL_PRODUCT_IN_COMPONENT))
                .thenReturn(genealogyProductInComponentDD);
        when(genealogyProductInComponentDD.create()).thenReturn(genealogyProductInComponent);

        // mock orderOperationsTreeIterator behavior
        when(orderOperationsTreeIterator.hasNext()).thenReturn(true, true, true, false);
        when(orderOperationsTreeIterator.next()).thenReturn(technologyOperationComponent, technologyOperationComponent,
                technologyOperationComponent);
        when(operationsTree.size()).thenReturn(3);

        // mock inputProductsIterator behavior
        when(inputProductsIterator.hasNext()).thenReturn(true, true, false);
        when(inputProductsIterator.next()).thenReturn(productInComponent, productInComponent);
        when(inputProductsList.size()).thenReturn(2);

        when(inputProductsList.iterator()).thenReturn(inputProductsIterator);
        when(operationsTree.iterator()).thenReturn(orderOperationsTreeIterator);

        when(genealogyProductInComponent.getBelongsToField("technologyOperationComponent")).thenReturn(
                technologyOperationComponent);
        when(technologyOperationComponent.getStringField(TreeType.NODE_NUMBER_FIELD)).thenReturn("1.");

        when(genealogyProductInComponent.getBelongsToField("productInComponent")).thenReturn(productInComponent);
        when(productInComponent.getBelongsToField("product")).thenReturn(product);
        when(product.getStringField("name")).thenReturn("someProductName");

        Entity order = mock(Entity.class);
        when(trackingRecord.getBelongsToField("order")).thenReturn(order);
        when(order.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(technology);
        when(order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS)).thenReturn(
                operationsTree);

        when(entityTreeUtilsService.getSortedEntities(operationsTree)).thenReturn(operationsTree);

        ArgumentCaptor<String> stringArgCaptor = ArgumentCaptor.forClass(String.class);

        // when
        advancedGenealogyForOrdersService.buildProductInComponentList(order);

        // then
        verify(genealogyProductInComponent, Mockito.atLeastOnce()).setField(Mockito.eq("technologyOperationComponent"),
                stringArgCaptor.capture());
        Assert.assertEquals(technologyOperationComponent, stringArgCaptor.getValue());

        verify(genealogyProductInComponent, Mockito.atLeastOnce()).setField(Mockito.eq("productInComponent"),
                stringArgCaptor.capture());
        Assert.assertEquals(productInComponent, stringArgCaptor.getValue());

    }
}
