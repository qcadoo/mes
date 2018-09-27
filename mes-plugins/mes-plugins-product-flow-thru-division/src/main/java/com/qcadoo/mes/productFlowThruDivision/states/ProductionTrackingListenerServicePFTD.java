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
package com.qcadoo.mes.productFlowThruDivision.states;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithQuantityAndCost;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.validators.ProductionTrackingValidatorsPFTD;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.PriceBasedOn;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public final class ProductionTrackingListenerServicePFTD {

    private static final String L_ERROR_NOT_ENOUGH_RESOURCES = "materialFlow.error.position.quantity.notEnoughResources";

    public static final String L_USER = "user";

    @Autowired
    private CostNormsForMaterialsService costNormsForMaterialsService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    @Autowired
    private ProductionTrackingValidatorsPFTD productionTrackingValidatorsPFTD;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    public Entity onAccept(final Entity productionTracking, final String sourceState) {
        boolean isCorrection = productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION);
        if (!isCorrection && !ProductionTrackingStateStringValues.CORRECTED.equals(sourceState)) {
            createWarehouseDocuments(productionTracking);
        }

        return productionTracking;
    }

    public void createWarehouseDocuments(final Entity productionTracking) {
        DataDefinition locationDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        List<Entity> recordOutProducts = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        Multimap<Long, Entity> groupedRecordOutProducts = productionTrackingDocumentsHelper.groupRecordOutProductsByLocation(
                recordOutProducts, technology);

        productionTrackingDocumentsHelper.fillFromBPCProductOut(groupedRecordOutProducts, recordOutProducts, order);
        productionTrackingDocumentsHelper.fillProductsOutFromSet(groupedRecordOutProducts);

        List<Entity> recordInProducts = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        Multimap<Long, Entity> groupedRecordInProducts = productionTrackingDocumentsHelper.groupRecordInProductsByWarehouse(recordInProducts, technology);

        productionTrackingDocumentsHelper.fillFromBPCProductIn(groupedRecordInProducts, recordInProducts, order);
        productionTrackingDocumentsHelper.fillProductsInFromSet(groupedRecordInProducts);

        if (!productionTrackingValidatorsPFTD.checkResources(productionTracking, groupedRecordInProducts, recordOutProducts)) {
            return;
        }

        for (Long warehouseId : groupedRecordOutProducts.keySet()) {
            Entity locationTo = locationDD.get(warehouseId);
            Entity inboundDocument = createOrUpdateInternalInboundDocumentForFinalProducts(locationTo, order,
                    groupedRecordOutProducts.get(warehouseId), productionTracking.getBelongsToField(L_USER));

            if (inboundDocument != null && !inboundDocument.isValid()) {
                for (ErrorMessage error : inboundDocument.getGlobalErrors()) {
                    productionTracking.addGlobalError(error.getMessage(), error.getVars());
                }

                productionTracking
                        .addGlobalError("productFlowThruDivision.productionTracking.productionTrackingError.createInternalInboundDocument");

                return;
            }
        }

        TransactionAspectSupport.currentTransactionStatus().flush();

        boolean errorsDisplayed = false;
        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            Entity warehouseFrom = locationDD.get(warehouseId);
            Entity outboundDocument = createInternalOutboundDocumentForComponents(warehouseFrom, order,
                    groupedRecordInProducts.get(warehouseId), productionTracking.getBelongsToField(L_USER));

            if (outboundDocument != null && !outboundDocument.isValid()) {
                for (ErrorMessage error : outboundDocument.getGlobalErrors()) {
                    if (error.getMessage().equalsIgnoreCase(L_ERROR_NOT_ENOUGH_RESOURCES)) {
                        productionTracking.addGlobalError(error.getMessage(), false, error.getVars());
                    } else {
                        if (!errorsDisplayed) {
                            productionTracking.addGlobalError(error.getMessage(), error.getVars());
                        }
                    }
                }

                if (!errorsDisplayed) {
                    productionTracking
                            .addGlobalError("productFlowThruDivision.productionTracking.productionTrackingError.createInternalOutboundDocument");

                    errorsDisplayed = true;
                }
            }
        }

        if (errorsDisplayed) {
            return;
        }

        updateCostsForOrder(order);

    }

    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
            final Collection<Entity> inProductsRecords, Entity user) {
        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);
        internalOutboundBuilder.internalOutbound(locationFrom);

        HashSet<Entity> inProductsWithoutDuplicates = Sets.newHashSet();

        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);

        for (Entity inProductRecord : inProductsRecords) {
            Entity inProduct = inProductRecord.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

            if (!inProductsWithoutDuplicates.contains(inProduct)) {
                Entity position = positionDD.create();
                BigDecimal usedQuantity = inProductRecord
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
                BigDecimal givenQuantity = inProductRecord
                        .getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);
                BigDecimal conversion = BigDecimal.ONE;
                String unit = inProduct.getStringField(ProductFields.UNIT);
                String givenUnit = inProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

                if (usedQuantity != null && givenQuantity != null) {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                            searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                                    UnitConversionItemFieldsB.PRODUCT, inProduct)));

                    if (unitConversions.isDefinedFor(givenUnit)) {
                        conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
                    }
                }

                position.setField(PositionFields.GIVEN_UNIT,
                        inProductRecord.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
                position.setField(PositionFields.PRODUCT, inProduct);
                position.setField(PositionFields.QUANTITY, usedQuantity);
                position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
                position.setField(PositionFields.CONVERSION, conversion);
                internalOutboundBuilder.addPosition(position);
            }

            inProductsWithoutDuplicates.add(inProduct);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);
        Entity document = internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();

        return document;
    }

    private Entity createOrUpdateInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
            final Collection<Entity> outProductsRecords, Entity user) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);
        boolean isNominalProductCost = priceBasedOn != null ? priceBasedOn.equals(PriceBasedOn.NOMINAL_PRODUCT_COST
                .getStringValue()) : false;
        List<Entity> finalProductRecord = null;
        Collection<Entity> intermediateRecords = Lists.newArrayList();

        for (Entity outProductRecord : outProductsRecords) {
            if (isFinalProductForOrder(order,
                    outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT))) {
                finalProductRecord = Lists.newArrayList(outProductRecord);
            } else {
                intermediateRecords.add(outProductRecord);
            }
        }

        if (isNominalProductCost) {
            if (finalProductRecord != null) {
                Entity inboundForFinalProduct = createInternalInboundDocumentForFinalProducts(locationTo, order,
                        finalProductRecord, true, user);

                if (inboundForFinalProduct != null && !inboundForFinalProduct.isValid() || intermediateRecords.isEmpty()) {
                    return inboundForFinalProduct;
                }
            }

            return createInternalInboundDocumentForFinalProducts(locationTo, order, intermediateRecords, true, user);
        } else {
            Entity existingInboundDocument = documentDD.find().add(SearchRestrictions.belongsTo(DocumentFieldsPFTD.ORDER, order))
                    .add(SearchRestrictions.belongsTo(DocumentFields.LOCATION_TO, locationTo))
                    .add(SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()))
                    .add(SearchRestrictions.eq(DocumentFields.TYPE, DocumentType.INTERNAL_INBOUND.getStringValue()))
                    .setMaxResults(1).uniqueResult();

            if (existingInboundDocument != null) {
                if (finalProductRecord != null) {
                    Entity inboundForFinalProduct = updateInternalInboundDocumentForFinalProducts(existingInboundDocument,
                            finalProductRecord);

                    if (inboundForFinalProduct != null && !inboundForFinalProduct.isValid() || intermediateRecords.isEmpty()) {
                        return inboundForFinalProduct;
                    }
                }

                return createInternalInboundDocumentForFinalProducts(locationTo, order, intermediateRecords, user);
            } else {
                if (finalProductRecord != null) {
                    Entity inboundForFinalProduct = createInternalInboundDocumentForFinalProducts(locationTo, order,
                            finalProductRecord, user);

                    if (inboundForFinalProduct != null && !inboundForFinalProduct.isValid() || intermediateRecords.isEmpty()) {
                        return inboundForFinalProduct;
                    }
                }

                return createInternalInboundDocumentForFinalProducts(locationTo, order, intermediateRecords, user);
            }
        }
    }

    private Entity updateInternalInboundDocumentForFinalProducts(final Entity existingInboundDocument,
            final Collection<Entity> outProductsRecords) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
        List<Entity> positions = Lists.newArrayList(existingInboundDocument.getHasManyField(DocumentFields.POSITIONS));

        for (Entity outProductRecord : outProductsRecords) {
            Entity outProduct = outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
            Optional<BigDecimal> usedQuantity = Optional.fromNullable(outProductRecord
                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));

            Optional<BigDecimal> givenQuantity = Optional.fromNullable(outProductRecord
                    .getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY));
            Optional<String> givenUnit = Optional.fromNullable(outProductRecord
                    .getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));

            Entity existingPosition;

            if (givenUnit.isPresent()) {
                existingPosition = filterPositionByProductAndGivenUnit(positions, outProduct, givenUnit);
            } else {
                existingPosition = filterPositionByProduct(positions, outProduct);
            }

            if (existingPosition != null) {
                Optional<BigDecimal> quantity = Optional.fromNullable(existingPosition.getDecimalField(PositionFields.QUANTITY));
                Optional<BigDecimal> givenQuantityFromPositiion = Optional.fromNullable(existingPosition
                        .getDecimalField(PositionFields.GIVEN_QUANTITY));

                existingPosition.setField(PositionFields.QUANTITY,
                        quantity.or(BigDecimal.ZERO).add(usedQuantity.or(BigDecimal.ZERO)));

                if (givenQuantity.isPresent()) {
                    existingPosition.setField(PositionFields.GIVEN_QUANTITY,
                            givenQuantity.or(BigDecimal.ZERO).add(givenQuantityFromPositiion.or(BigDecimal.ZERO)));
                }

                existingPosition.setField(PositionFields.GIVEN_UNIT, givenUnit.get());
            } else {
                Entity position = positionDD.create();
                position.setField(PositionFields.PRODUCT, outProduct);
                position.setField(PositionFields.QUANTITY, usedQuantity.get());
                BigDecimal conversion = BigDecimal.ONE;

                String unit = outProduct.getStringField(ProductFields.UNIT);

                if (givenQuantity.isPresent()) {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                            searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                                    UnitConversionItemFieldsB.PRODUCT, outProduct)));

                    if (unitConversions.isDefinedFor(givenUnit.get())) {
                        conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit.get()));
                    }

                    position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity.get());
                }

                position.setField(PositionFields.GIVEN_UNIT, givenUnit.get());
                position.setField(PositionFields.CONVERSION, conversion);
                positions.add(position);
            }
        }

        existingInboundDocument.setField(DocumentFields.POSITIONS, positions);

        return existingInboundDocument.getDataDefinition().save(existingInboundDocument);
    }

    private Entity filterPositionByProduct(final List<Entity> existingPositions, final Entity outProduct) {
        return Iterables.find(existingPositions, new Predicate<Entity>() {

            @Override
            public boolean apply(Entity position) {
                return outProduct.getId().equals(position.getBelongsToField(PositionFields.PRODUCT).getId());
            }
        });
    }

    private Entity filterPositionByProductAndGivenUnit(final List<Entity> existingPositions, final Entity outProduct,
            final Optional<String> givenUnit) {
        for (Entity position : existingPositions) {
            if (outProduct.getId().equals(position.getBelongsToField(PositionFields.PRODUCT).getId())
                    && !StringUtils.isEmpty(position.getStringField(PositionFields.GIVEN_UNIT))
                    && position.getStringField(PositionFields.GIVEN_UNIT).equals(givenUnit.get())) {
                return position;
            }
        }

        return null;
    }

    public Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
            final Collection<Entity> outProductsRecords, Entity user) {
        return createInternalInboundDocumentForFinalProducts(locationTo, order, outProductsRecords, false, user);
    }

    private Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
            final Collection<Entity> outProductsRecords, final boolean isBasedOnNominalCost, Entity user) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
        DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);
        internalInboundBuilder.internalInbound(locationTo);
        boolean isFinalProduct = false;

        Entity productionTracking = null;
        for (Entity outProductRecord : outProductsRecords) {
            Entity outProduct = outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            if (productionTracking == null) {
                productionTracking = outProductRecord
                        .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            }
            if (isFinalProductForOrder(order, outProduct)) {
                isFinalProduct = true;
            }

            Entity position = positionDD.create();
            BigDecimal usedQuantity = outProductRecord.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal givenQuantity = outProductRecord
                    .getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);
            BigDecimal conversion = BigDecimal.ONE;
            String unit = outProduct.getStringField(ProductFields.UNIT);
            String givenUnit = outProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

            if (usedQuantity != null && givenQuantity != null) {

                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                                UnitConversionItemFieldsB.PRODUCT, outProduct)));

                if (unitConversions.isDefinedFor(givenUnit)) {
                    conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
                }
            }

            position.setField(PositionFields.PRODUCT, outProduct);
            position.setField(PositionFields.QUANTITY, usedQuantity);
            position.setField(PositionFields.CONVERSION, conversion);
            position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);

            position.setField(PositionFields.GIVEN_UNIT,
                    outProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT));

            if (isBasedOnNominalCost) {
                BigDecimal nominalCost = outProduct.getDecimalField("nominalCost") != null ? outProduct
                        .getDecimalField("nominalCost") : BigDecimal.ZERO;
                position.setField(PositionFields.PRICE, nominalCost);

            }
            position.setField(PositionFields.PRODUCTION_DATE, new Date());

            internalInboundBuilder.addPosition(position);
        }

        internalInboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        if (OrderState.of(order) == OrderState.COMPLETED || !isFinalProduct || isBasedOnNominalCost
                || (productionTracking != null && orderClosingHelper.orderShouldBeClosed(productionTracking))) {
            internalInboundBuilder.setAccepted();
        }

        return internalInboundBuilder.buildWithEntityRuntimeException();
    }

    private boolean isFinalProductForOrder(final Entity order, final Entity product) {
        return order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId());
    }

    public void updateCostsForOrder(final Entity order) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);

        SearchQueryBuilder searchQueryBuilder = positionDD
                .find("SELECT pr.id AS product, SUM(p.quantity) AS quantity, SUM(p.quantity * p.price) AS price "
                        + "FROM #materialFlowResources_position p JOIN p.document AS d join p.product AS pr "
                        + "WHERE d.order = :order_id AND d.type = :type "
                        + "GROUP BY d.order, d.type, pr.id");

        searchQueryBuilder.setLong("order_id", order.getId());
        searchQueryBuilder.setString("type", DocumentType.INTERNAL_OUTBOUND.getStringValue());
        SearchResult result = searchQueryBuilder.list();

        List<ProductWithQuantityAndCost> productsWithQuantitiesAndCosts = Lists.newArrayList();

        for (Entity costsForProduct : result.getEntities()) {
            Long product = (Long) costsForProduct.getField(PositionFields.PRODUCT);
            BigDecimal quantity = costsForProduct.getDecimalField(PositionFields.QUANTITY);
            BigDecimal cost = costsForProduct.getDecimalField(PositionFields.PRICE);
            productsWithQuantitiesAndCosts.add(new ProductWithQuantityAndCost(product, quantity, cost));
        }

        List<Entity> updatedCosts = costNormsForMaterialsService.updateCostsForProductInOrder(order,
                productsWithQuantitiesAndCosts);

        order.setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, updatedCosts);
    }

    public boolean isOperationProductComponentToRegister(OperationProductComponentHolder operationProductComponentHolder,
            Entity product, Entity toc) {
        if (operationProductComponentHolder.isEntityTypeSame(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)) {
            Entity opic = getOperationComponentForProductAndToc(product, toc,
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

            if (opic != null
                    && opic.getStringField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW).equals(
                            ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue())) {
                return false;
            } else if (opic == null && flowWithinTheProcess(operationProductComponentHolder)) {
                return false;
            }
        } else if (operationProductComponentHolder.isEntityTypeSame(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT)) {
            Entity opoc = getOperationComponentForProductAndToc(product, toc,
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            if (opoc != null
                    && opoc.getStringField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW).equals(
                            ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean flowWithinTheProcess(OperationProductComponentHolder operationProductComponentHolder) {
        if (operationProductComponentHolder.getProductionCountingQuantityId() != null) {
            Entity entity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).get(
                    operationProductComponentHolder.getProductionCountingQuantityId());

            if (entity.getStringField(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW).equals(
                    ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue())) {
                return true;
            }
        }
        return false;
    }

    private Entity getOperationComponentForProductAndToc(final Entity product, final Entity toc, final String hasManyName) {
        return toc.getHasManyField(hasManyName).find().add(SearchRestrictions.belongsTo("product", product)).setMaxResults(1)
                .uniqueResult();
    }

}
