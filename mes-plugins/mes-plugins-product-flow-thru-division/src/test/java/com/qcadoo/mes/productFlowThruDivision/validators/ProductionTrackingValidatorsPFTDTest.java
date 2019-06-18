package com.qcadoo.mes.productFlowThruDivision.validators;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageHolder;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageHolderFactory;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class ProductionTrackingValidatorsPFTDTest {

    private ProductionTrackingValidatorsPFTD productionTrackingValidatorsPFTD;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Mock
    private NotEnoughResourcesErrorMessageHolderFactory notEnoughResourcesErrorMessageHolderFactory;

    @Mock
    private NotEnoughResourcesErrorMessageHolder errorMessageHolder;

    @Mock
    private DataDefinition locationDD;

    @Mock
    private Entity warehouse, recordInProduct1, recordInProduct2, recordOutProduct, product1, product2, product3, productionTracking;

    private Multimap<Long, Entity> groupedRecordInProducts = ArrayListMultimap.create();

    private List<Entity> recordOutProducts = Lists.newArrayList();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionTrackingValidatorsPFTD = new ProductionTrackingValidatorsPFTD();

        ReflectionTestUtils.setField(productionTrackingValidatorsPFTD, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(productionTrackingValidatorsPFTD, "materialFlowResourcesService",
                materialFlowResourcesService);
        ReflectionTestUtils.setField(productionTrackingValidatorsPFTD, "notEnoughResourcesErrorMessageHolderFactory", notEnoughResourcesErrorMessageHolderFactory);

        Long warehouseId = 1L;

        Long product1Id = 1L;

        Long product2Id = 2L;

        Long product3Id = 3L;

        groupedRecordInProducts.put(warehouseId, recordInProduct1);
        groupedRecordInProducts.put(warehouseId, recordInProduct2);

        recordOutProducts.add(recordOutProduct);

        given(notEnoughResourcesErrorMessageHolderFactory.create()).willReturn(errorMessageHolder);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION))
                .willReturn(locationDD);

        given(locationDD.get(warehouseId)).willReturn(warehouse);

        given(recordInProduct1.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)).willReturn(product1);
        given(recordInProduct2.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)).willReturn(product2);
        given(product1.getId()).willReturn(product1Id);
        given(product2.getId()).willReturn(product2Id);
        given(product3.getId()).willReturn(product3Id);

        Map<Long, BigDecimal> stockMap = Maps.newHashMap();
        stockMap.put(product1Id, BigDecimal.ONE);
        stockMap.put(product2Id, new BigDecimal(2));
        given(materialFlowResourcesService.getQuantitiesForProductsAndLocation(Lists.newArrayList(product1, product2), warehouse))
                .willReturn(stockMap);

        given(recordInProduct1.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(BigDecimal.ONE);
    }

    @Test
    public void shouldReturnErrorWhenNoResourcesAndProductNotInTrackingOperations() {
        // given
        given(recordInProduct2.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(BigDecimal.TEN);
        given(recordOutProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)).willReturn(product3);
        given(warehouse.getStringField(LocationFields.NAME)).willReturn("location");

        // when
        boolean enoughResources = productionTrackingValidatorsPFTD.checkResources(productionTracking, groupedRecordInProducts,
                recordOutProducts);

        // then
        assertEquals(false, enoughResources);
    }

    @Test
    public void shouldReturnErrorWhenNoResourcesAndQuantityInTrackingOperationsIsTooLow() {
        // given
        given(recordInProduct2.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(BigDecimal.TEN);
        given(recordOutProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)).willReturn(product2);
        given(recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(new BigDecimal(2));
        given(warehouse.getStringField(LocationFields.NAME)).willReturn("location");

        // when
        boolean enoughResources = productionTrackingValidatorsPFTD.checkResources(productionTracking, groupedRecordInProducts,
                recordOutProducts);

        // then
        assertEquals(false, enoughResources);
    }

    @Test
    public void shouldReturnNoErrorWhenResourcesExists() {
        // given
        given(recordInProduct2.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(BigDecimal.ONE);

        // when
        boolean enoughResources = productionTrackingValidatorsPFTD.checkResources(productionTracking, groupedRecordInProducts,
                recordOutProducts);

        // then
        assertEquals(true, enoughResources);
    }

    @Test
    public void shouldReturnNoErrorWhenResourcesNotExistsAndQuantityInTrackingOperationsIsOk() {
        // given
        given(recordInProduct2.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(BigDecimal.TEN);
        given(recordOutProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)).willReturn(product2);
        given(recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .willReturn(BigDecimal.TEN);

        // when
        boolean enoughResources = productionTrackingValidatorsPFTD.checkResources(productionTracking, groupedRecordInProducts,
                recordOutProducts);

        // then
        assertEquals(true, enoughResources);
    }
}