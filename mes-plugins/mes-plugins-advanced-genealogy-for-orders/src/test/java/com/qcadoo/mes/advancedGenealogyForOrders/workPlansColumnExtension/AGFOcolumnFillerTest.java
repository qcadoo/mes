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
package com.qcadoo.mes.advancedGenealogyForOrders.workPlansColumnExtension;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class AGFOcolumnFillerTest {

    private List<Entity> orders;

    private String batch1number, batch2number, batch3number;

    private static final String BATCH_COLUMN = "batchNumbers";

    @Mock
    private Entity operProdComp1, operProdComp2;

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        batch1number = "batch1";
        batch2number = "batch2";
        batch3number = "batch3";

        Entity order = mock(Entity.class);
        orders = asList(order);

        Entity trackingRecord = mock(Entity.class);
        EntityList trackingRecords = mockEntityList(asList(trackingRecord));
        when(order.getHasManyField("trackingRecords")).thenReturn(trackingRecords);

        Entity genProdComp1 = mock(Entity.class);
        Entity genProdComp2 = mock(Entity.class);

        when(genProdComp1.getBelongsToField("productInComponent")).thenReturn(operProdComp1);
        when(genProdComp2.getBelongsToField("productInComponent")).thenReturn(operProdComp2);

        EntityList genProdComps = mockEntityList(asList(genProdComp1, genProdComp2));
        when(trackingRecord.getHasManyField("genealogyProductInComponents")).thenReturn(genProdComps);

        Entity batchComp1 = mock(Entity.class);
        Entity batchComp2 = mock(Entity.class);
        Entity batchComp3 = mock(Entity.class);

        EntityList prodInBatches1 = mockEntityList(asList(batchComp1, batchComp2));
        when(genProdComp1.getHasManyField("productInBatches")).thenReturn(prodInBatches1);

        EntityList prodInBatches2 = mockEntityList(asList(batchComp3));
        when(genProdComp2.getHasManyField("productInBatches")).thenReturn(prodInBatches2);

        Entity batch1 = mock(Entity.class);
        Entity batch2 = mock(Entity.class);
        Entity batch3 = mock(Entity.class);

        when(batchComp1.getBelongsToField("batch")).thenReturn(batch1);
        when(batchComp2.getBelongsToField("batch")).thenReturn(batch2);
        when(batchComp3.getBelongsToField("batch")).thenReturn(batch3);

        when(batch1.getStringField("number")).thenReturn(batch1number);
        when(batch2.getStringField("number")).thenReturn(batch2number);
        when(batch3.getStringField("number")).thenReturn(batch3number);
    }

    @Test
    public void shouldReturnCorrectBatches() {
        // given
        AGFOcolumnFiller columnFiller = new AGFOcolumnFiller();

        // when
        Map<Entity, Map<String, String>> values = columnFiller.getValues(orders);

        // then
        assertEquals(2, values.keySet().size());
        assertEquals(batch1number + ", " + batch2number, values.get(operProdComp1).get(BATCH_COLUMN));
        assertEquals(batch3number, values.get(operProdComp2).get(BATCH_COLUMN));
    }
}
