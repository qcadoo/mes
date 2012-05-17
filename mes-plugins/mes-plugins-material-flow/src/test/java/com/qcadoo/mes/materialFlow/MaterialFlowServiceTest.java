/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.materialFlow;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.internal.DefaultEntity;

public class MaterialFlowServiceTest {

    private MaterialFlowService materialFlowService;

    private DataDefinitionService dataDefinitionService;

    private DataDefinition transferDataCorrection;

    private DataDefinition transfer;

    private DataDefinition dataDefStockAreas;

    private SearchResult resultTo;

    private SearchResult resultFrom;

    private NumberService numberService;

    private MathContext mathContext;

    private static final String STOCKAREAS = "1";

    private static final String PRODUCT = "1";

    private static final Date FORDATE = new Date();

    @Before
    public void init() {
        materialFlowService = new MaterialFlowService();
        dataDefinitionService = mock(DataDefinitionService.class);
        transferDataCorrection = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        dataDefStockAreas = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        transfer = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        numberService = mock(NumberService.class);

        setField(materialFlowService, "dataDefinitionService", dataDefinitionService);
        setField(materialFlowService, "numberService", numberService);

        mathContext = MathContext.DECIMAL64;
        when(numberService.getMathContext()).thenReturn(mathContext);

        resultTo = mock(SearchResult.class);
        resultFrom = mock(SearchResult.class);

        given(dataDefinitionService.get("materialFlow", "stockCorrection")).willReturn(transferDataCorrection);
        given(dataDefinitionService.get("materialFlow", "transfer")).willReturn(transfer);
        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_STOCK_AREAS))
                .willReturn(dataDefStockAreas);
    }

    @Test
    public void shouldCalculateShouldBeForOnlyTransfers() {
        Long stockAreasId = Long.valueOf(STOCKAREAS);
        given(dataDefStockAreas.find().add(SearchRestrictions.eq("number", STOCKAREAS)).uniqueResult().getId()).willReturn(
                Long.valueOf(stockAreasId));

        Long productId = Long.valueOf(PRODUCT);
        given(
                transferDataCorrection.find().add(SearchRestrictions.eq("stockAreas.id", stockAreasId))
                        .add(SearchRestrictions.eq("product.id", productId)).addOrder(SearchOrders.desc("stockCorrectionDate"))
                        .setMaxResults(1).uniqueResult()).willReturn(null);
        given(
                transfer.find().add(SearchRestrictions.eq("stockAreasTo.id", stockAreasId))
                        .add(SearchRestrictions.eq("product.id", productId)).add(SearchRestrictions.le("time", FORDATE)).list())
                .willReturn(resultTo);
        given(
                transfer.find().add(SearchRestrictions.eq("stockAreasFrom.id", stockAreasId))
                        .add(SearchRestrictions.eq("product.id", productId)).add(SearchRestrictions.le("time", FORDATE)).list())
                .willReturn(resultFrom);

        List<Entity> list = new ArrayList<Entity>();
        Entity entity = new DefaultEntity(transfer);
        Entity entity2 = new DefaultEntity(transfer);
        entity.setField("quantity", new BigDecimal(1000));
        entity2.setField("quantity", new BigDecimal(500));
        list.add(entity);
        list.add(entity2);

        given(resultTo.getEntities()).willReturn(list);

        assertEquals(new BigDecimal(1500), materialFlowService.calculateShouldBeInStockArea(stockAreasId, PRODUCT, FORDATE));
    }

    @Test
    public void shouldCalculateShouldBeForOnlyStockCorrections() {
        Entity entity = new DefaultEntity(transferDataCorrection);
        entity.setField("stockCorrectionDate", new Date(0));
        entity.setField("found", new BigDecimal(1000));

        Long stockAreasId = Long.valueOf(STOCKAREAS);
        given(dataDefStockAreas.find().add(SearchRestrictions.eq("number", STOCKAREAS)).uniqueResult().getId()).willReturn(
                Long.valueOf(stockAreasId));
        Long productId = Long.valueOf(PRODUCT);
        given(
                transferDataCorrection.find().add(SearchRestrictions.eq("stockAreas.id", stockAreasId))
                        .add(SearchRestrictions.eq("product.id", productId)).addOrder(SearchOrders.desc("stockCorrectionDate"))
                        .setMaxResults(1).uniqueResult()).willReturn(entity);

        given(transfer.find(Mockito.anyString()).list()).willReturn(resultTo);

        assertEquals(new BigDecimal(1000), materialFlowService.calculateShouldBeInStockArea(stockAreasId, PRODUCT, FORDATE));
    }

    @Test
    public void shouldCalculateBothForTransferAndCorrection() {
        Date date = new Date(100);
        String lastCorrectionDate = date.toString();
        given(
                transfer.find().add(SearchRestrictions.eq("stockAreasTo.id", Long.valueOf(STOCKAREAS)))
                        .add(SearchRestrictions.eq("product.id", Long.valueOf(PRODUCT)))
                        .add(SearchRestrictions.le("time", FORDATE)).add(SearchRestrictions.gt("time", lastCorrectionDate))
                        .list()).willReturn(resultTo);
        given(
                transfer.find().add(SearchRestrictions.eq("stockAreasFrom.id", Long.valueOf(STOCKAREAS)))
                        .add(SearchRestrictions.eq("product.id", Long.valueOf(PRODUCT)))
                        .add(SearchRestrictions.le("time", FORDATE)).add(SearchRestrictions.gt("time", lastCorrectionDate))
                        .list()).willReturn(resultFrom);

        Entity entity = new DefaultEntity(transferDataCorrection);
        entity.setField("stockCorrectionDate", new Date(100));
        entity.setField("found", new BigDecimal(1000));

        Long stockAreasId = Long.valueOf(STOCKAREAS);
        given(dataDefStockAreas.find().add(SearchRestrictions.eq("number", STOCKAREAS)).uniqueResult().getId()).willReturn(
                Long.valueOf(stockAreasId));
        Long productId = Long.valueOf(PRODUCT);
        given(
                transferDataCorrection.find().add(SearchRestrictions.eq("stockAreas.id", stockAreasId))
                        .add(SearchRestrictions.eq("product.id", productId)).addOrder(SearchOrders.desc("stockCorrectionDate"))
                        .setMaxResults(1).uniqueResult()).willReturn(entity);

        List<Entity> transferList = new ArrayList<Entity>();
        Entity entity2 = new DefaultEntity(transfer);
        Entity entity3 = new DefaultEntity(transfer);
        entity2.setField("quantity", new BigDecimal(1000));
        entity3.setField("quantity", new BigDecimal(500));
        transferList.add(entity2);
        transferList.add(entity3);

        given(resultTo.getEntities()).willReturn(transferList);

        assertEquals(new BigDecimal(2500), materialFlowService.calculateShouldBeInStockArea(stockAreasId, PRODUCT, FORDATE));
    }
}