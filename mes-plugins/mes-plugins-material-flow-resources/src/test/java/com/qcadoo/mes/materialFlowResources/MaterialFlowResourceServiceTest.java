package com.qcadoo.mes.materialFlowResources;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.TRANSPORT;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.TIME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class MaterialFlowResourceServiceTest {

    private MaterialFlowResourcesService materialFlowResourceService;

    private static final long L_ID = 1L;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private NumberService numberService;

    @Mock
    private DataDefinition resourceDD;

    @Mock
    private Entity resource, resource1, resource2, location, product, transfer, locationFrom, locationTo;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    private BigDecimal quantity, resourceQuantity;

    @Mock
    private Date time;

    @Mock
    private List<Entity> resources;

    @Mock
    private Iterator<Entity> resourcesIterator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        materialFlowResourceService = new MaterialFlowResourcesServiceImpl();

        setField(materialFlowResourceService, "dataDefinitionService", dataDefinitionService);
        setField(materialFlowResourceService, "numberService", numberService);

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public void shouldReturnFalseWhenAreResourcesSufficientIfResourcesAreNull() {
        // given
        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        boolean result = materialFlowResourceService.areResourcesSufficient(location, product, quantity);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenAreResourcesSufficientIfResourcesArentNull() {
        // given
        quantity = BigDecimal.ONE;
        resourceQuantity = BigDecimal.ZERO;

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(resources);

        given(resourcesIterator.hasNext()).willReturn(true, true, false);
        given(resourcesIterator.next()).willReturn(resource1, resource2);

        given(resources.iterator()).willReturn(resourcesIterator);

        given(resource1.getDecimalField(QUANTITY)).willReturn(resourceQuantity);
        given(resource2.getDecimalField(QUANTITY)).willReturn(resourceQuantity);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        // when
        boolean result = materialFlowResourceService.areResourcesSufficient(location, product, quantity);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenAreResourcesSufficientIfResourcesArentNull() {
        // given
        quantity = BigDecimal.ONE;
        resourceQuantity = BigDecimal.ONE;

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(resources);

        given(resourcesIterator.hasNext()).willReturn(true, true, false);
        given(resourcesIterator.next()).willReturn(resource1, resource2);

        given(resources.iterator()).willReturn(resourcesIterator);

        given(resource1.getDecimalField(QUANTITY)).willReturn(resourceQuantity);
        given(resource2.getDecimalField(QUANTITY)).willReturn(resourceQuantity);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        // when
        boolean result = materialFlowResourceService.areResourcesSufficient(location, product, quantity);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnWhenManageResourcesAndTranserIsNull() {
        // given

        // when
        materialFlowResourceService.manageResources(null);

        // then
        verify(resource, never()).setField(Mockito.anyString(), Mockito.any());
        verify(resourceDD, never()).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldAddResoruceWhenManageResourcesAndTranserIsntNullAndTypeIsProduction() {
        // given
        quantity = BigDecimal.ONE;

        given(transfer.getStringField(TYPE)).willReturn(PRODUCTION.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.create()).willReturn(resource);
        given(resource.getDataDefinition()).willReturn(resourceDD);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resource, times(4)).setField(Mockito.anyString(), Mockito.any());
        verify(resourceDD).save(Mockito.any(Entity.class));
    }

    @Test
    public void shouldntUpdateResoruceWhenManageResourcesAndTranserIsntNullAndTypeIsConsumptionAndResourcesAreNull() {
        // given
        quantity = BigDecimal.ONE;

        given(transfer.getStringField(TYPE)).willReturn(CONSUMPTION.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resourceDD, never()).save(Mockito.any(Entity.class));

        verify(resourceDD, never()).delete(Mockito.anyLong());
    }

    @Test
    public void shouldUpdateResoruceAndDeleteResourceWhenManageResourcesAndTranserIsntNullAndTypeIsConsumptionAndResourcesArentNull() {
        // given
        quantity = BigDecimal.ONE;
        resourceQuantity = BigDecimal.ONE;

        given(transfer.getStringField(TYPE)).willReturn(CONSUMPTION.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(resources);

        given(resourcesIterator.hasNext()).willReturn(true, true, false);
        given(resourcesIterator.next()).willReturn(resource1, resource2);

        given(resources.iterator()).willReturn(resourcesIterator);

        given(resource1.getDecimalField(QUANTITY)).willReturn(resourceQuantity);
        given(resource2.getDecimalField(QUANTITY)).willReturn(resourceQuantity);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        given(resource1.getDataDefinition()).willReturn(resourceDD);
        given(resource1.getId()).willReturn(L_ID);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resourceDD, never()).save(Mockito.any(Entity.class));

        verify(resourceDD).delete(Mockito.anyLong());
    }

    @Test
    public void shouldUpdateResoruceWhenManageResourcesAndTranserIsntNullAndTypeIsConsumptionAndResourcesArentNull() {
        // given
        quantity = BigDecimal.ONE;
        resourceQuantity = BigDecimal.TEN;

        given(transfer.getStringField(TYPE)).willReturn(CONSUMPTION.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(resources);

        given(resourcesIterator.hasNext()).willReturn(true, true, false);
        given(resourcesIterator.next()).willReturn(resource1, resource2);

        given(resources.iterator()).willReturn(resourcesIterator);

        given(resource1.getDecimalField(QUANTITY)).willReturn(resourceQuantity);
        given(resource2.getDecimalField(QUANTITY)).willReturn(resourceQuantity);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        given(resource1.getDataDefinition()).willReturn(resourceDD);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resourceDD).save(Mockito.any(Entity.class));

        verify(resourceDD, never()).delete(Mockito.anyLong());
    }

    @Test
    public void shouldntUpdateResourceAndShouldAddResourceWhenManageResourcesAndTranserIsntNullAndTypeIsTransportAndResourcesAreNull() {
        // given
        quantity = BigDecimal.ONE;

        given(transfer.getStringField(TYPE)).willReturn(TRANSPORT.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        given(resourceDD.create()).willReturn(resource);
        given(resource.getDataDefinition()).willReturn(resourceDD);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resourceDD).save(Mockito.any(Entity.class));
        verify(resource, times(4)).setField(Mockito.anyString(), Mockito.any());

        verify(resourceDD, never()).delete(Mockito.anyLong());
    }

    @Test
    public void shouldUpdateResoruceAndDeleteResourceAndAddResoruceWhenManageResourcesAndTranserIsntNullAndTypeIsTransportAndResourcesArentNull() {
        // given
        quantity = BigDecimal.ONE;
        resourceQuantity = BigDecimal.ONE;

        given(transfer.getStringField(TYPE)).willReturn(TRANSPORT.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(resources);

        given(resourcesIterator.hasNext()).willReturn(true, true, false);
        given(resourcesIterator.next()).willReturn(resource1, resource2);

        given(resources.iterator()).willReturn(resourcesIterator);

        given(resource1.getDecimalField(QUANTITY)).willReturn(resourceQuantity);
        given(resource2.getDecimalField(QUANTITY)).willReturn(resourceQuantity);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        given(resource1.getDataDefinition()).willReturn(resourceDD);
        given(resource1.getId()).willReturn(L_ID);

        given(resourceDD.create()).willReturn(resource);
        given(resource.getDataDefinition()).willReturn(resourceDD);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resourceDD).save(Mockito.any(Entity.class));
        verify(resource, times(4)).setField(Mockito.anyString(), Mockito.any());

        verify(resourceDD).delete(Mockito.anyLong());
    }

    @Test
    public void shouldUpdateResoruceAndAddResourceWhenManageResourcesAndTranserIsntNullAndTypeIsTransferAndResourcesArentNull() {
        // given
        quantity = BigDecimal.ONE;
        resourceQuantity = BigDecimal.TEN;

        given(transfer.getStringField(TYPE)).willReturn(TRANSPORT.getStringValue());
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transfer.getBelongsToField(PRODUCT)).willReturn(product);
        given(transfer.getDecimalField(QUANTITY)).willReturn(quantity);
        given(transfer.getField(TIME)).willReturn(time);

        given(
                dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowResourcesConstants.MODEL_RESOURCE)).willReturn(resourceDD);
        given(resourceDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderAscBy(Mockito.anyString())).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        given(resourcesIterator.hasNext()).willReturn(true, true, false);
        given(resourcesIterator.next()).willReturn(resource1, resource2);

        given(resources.iterator()).willReturn(resourcesIterator);

        given(resource1.getDecimalField(QUANTITY)).willReturn(resourceQuantity);
        given(resource2.getDecimalField(QUANTITY)).willReturn(resourceQuantity);

        given(numberService.getMathContext()).willReturn(MathContext.DECIMAL64);

        given(resource1.getDataDefinition()).willReturn(resourceDD);
        given(resource1.getId()).willReturn(L_ID);

        given(resourceDD.create()).willReturn(resource);
        given(resource.getDataDefinition()).willReturn(resourceDD);

        // when
        materialFlowResourceService.manageResources(transfer);

        // then
        verify(resourceDD).save(Mockito.any(Entity.class));
        verify(resource, times(4)).setField(Mockito.anyString(), Mockito.any());

        verify(resourceDD, never()).delete(Mockito.anyLong());
    }
}
