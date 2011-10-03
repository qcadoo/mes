package com.qcadoo.mes.materialFlow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.internal.DefaultEntity;

public class MaterialFlowServiceTest {

	private MaterialFlowService materialFlowService;
	
	private DataDefinitionService dataDefinitionService;
	private DataDefinition transferDataCorrection;
    private DataDefinition transfer;
	private SearchResult resultDataCorrection;
	private SearchResult resultTo;
	private SearchResult resultFrom;
	
	private static final String stockAreas = "stockAreas";
	private static final String product = "product";
	private static final String forDate = "date";
	
	@Before
	public void init() {
		materialFlowService = new MaterialFlowService();
		dataDefinitionService = mock(DataDefinitionService.class);
		transferDataCorrection = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
		transfer = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
		
		setField(materialFlowService, "dataDefinitionService", dataDefinitionService);
		
		resultDataCorrection = mock(SearchResult.class);
		resultTo = mock(SearchResult.class);
		resultFrom = mock(SearchResult.class);
		
		given(dataDefinitionService.get("materialFlow", "stockCorrection"))
			.willReturn(transferDataCorrection);
		given(dataDefinitionService.get("materialFlow", "transfer"))
			.willReturn(transfer);
		given(transferDataCorrection.find(Mockito.anyString()).list())
	        .willReturn(resultDataCorrection);
	}
	
	@Test
	public void shouldCalculateShouldBeForOnlyTransfers() {
		given(transfer.find(
                "where stockAreasTo = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate + "'")
                .list())
                .willReturn(resultTo);
		given(transfer.find(
                "where stockAreasFrom = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate + "'")
                .list())
                .willReturn(resultFrom);
		
		List<Entity> list = new ArrayList<Entity>();
		Entity entity = new DefaultEntity(transfer);
		Entity entity2 = new DefaultEntity(transfer);
		entity.setField("quantity", new BigDecimal(1000));
		entity2.setField("quantity", new BigDecimal(500));
		list.add(entity);
		list.add(entity2);
		
		given(resultTo.getEntities()).willReturn(list);
		
		assertEquals(new BigDecimal(1500), materialFlowService.calculateShouldBe(stockAreas, product, forDate));
	}
	
	@Test
	public void shouldCalculateShouldBeForOnlyStockCorrections() {
		List<Entity> list = new ArrayList<Entity>();
		Entity entity = new DefaultEntity(transferDataCorrection);
		entity.setField("stockCorrectionDate", new Date(0));
		entity.setField("found", new BigDecimal(1000));
		list.add(entity);
		
		given(resultDataCorrection.getEntities()).willReturn(list);
		
		given(transfer.find(Mockito.anyString())
                .list())
                .willReturn(resultTo);
		
		assertEquals(new BigDecimal(1000), materialFlowService.calculateShouldBe(stockAreas, product, forDate));
	}
	
	@Test
	public void shouldCalculateBothForTransferAndCorrection() {
		Date date = new Date(100);
		String lastCorrectionDate = date.toString();
		given(transfer.find(
                "where stockAreasTo = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate
                + "' and date > '" + lastCorrectionDate + "'").list())
                .willReturn(resultTo);
		given(transfer.find(
                "where stockAreasFrom = '" + stockAreas + "' and product = '" + product + "' and date <= '" + forDate
                + "' and date > '" + lastCorrectionDate + "'").list())
                .willReturn(resultFrom);
		
		List<Entity> list = new ArrayList<Entity>();
		Entity entity = new DefaultEntity(transferDataCorrection);
		entity.setField("stockCorrectionDate", new Date(100));
		entity.setField("found", new BigDecimal(1000));
		list.add(entity);
		
		given(resultDataCorrection.getEntities()).willReturn(list);
		
		List<Entity> transferList = new ArrayList<Entity>();
		Entity entity2 = new DefaultEntity(transfer);
		Entity entity3 = new DefaultEntity(transfer);
		entity2.setField("quantity", new BigDecimal(1000));
		entity3.setField("quantity", new BigDecimal(500));
		transferList.add(entity2);
		transferList.add(entity3);
		
		given(resultTo.getEntities()).willReturn(transferList);
		
		assertEquals(new BigDecimal(2500), materialFlowService.calculateShouldBe(stockAreas, product, forDate));
	}
}
