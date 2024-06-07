/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.states;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.materialFlowResources.service.DocumentStateChangeService;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productFlowThruDivision.OrderMaterialAvailability;
import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.mes.productFlowThruDivision.realProductionCost.RealProductionCostService;
import com.qcadoo.mes.productFlowThruDivision.reservation.OrderReservationsService;
import com.qcadoo.mes.productFlowThruDivision.service.ProductionCountingDocumentService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderStatesListenerServicePFTD {

    private static final Logger LOG = LoggerFactory.getLogger(OrderStatesListenerServicePFTD.class);

    private static final String L_ACCEPT_INBOUND_DOCUMENT_ERROR = "productFlowThruDivision.productionTracking.completeOrderError.acceptInboundDocument";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderMaterialsCostDataGenerator orderMaterialsCostDataGenerator;

    @Autowired
    private ProductionTrackingListenerServicePFTD productionTrackingListenerServicePFTD;

    @Autowired
    private RealProductionCostService realProductionCostService;

    @Autowired
    private OrderMaterialAvailability orderMaterialAvailability;

    @Autowired
    private ProductionCountingDocumentService productionCountingDocumentService;

    @Autowired
    private DocumentStateChangeService documentStateChangeService;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private OrderReservationsService orderReservationsService;

    public void clearReservations(final StateChangeContext stateChangeContext) {
        orderReservationsService.clearReservationsForOrder(stateChangeContext.getOwner());
    }

    public void acceptInboundDocumentsForOrder(final StateChangeContext stateChangeContext) {
        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);

        if (ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
            Entity order = stateChangeContext.getOwner();

            String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);

            boolean isNominalProductCost = Objects.nonNull(priceBasedOn)
                    && priceBasedOn.equals(PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue())
                    || !Strings.isNullOrEmpty(order.getStringField(OrderFields.ADDITIONAL_FINAL_PRODUCTS));

            if (!isNominalProductCost) {
                order = updateCostsInOrder(order);

                productionTrackingListenerServicePFTD.updateCostsForOrder(order);

                fillRealProductionCost(order);

                stateChangeContext.setOwner(order);
            }

            Either<String, Void> result = tryAcceptInboundDocumentsFor(order, isNominalProductCost);

            if (result.isLeft()) {
                stateChangeContext.addMessage(result.getLeft(), StateMessageType.FAILURE);
            }
        }
    }

    public Entity updateCostsInOrder(final Entity order) {
        List<Entity> orderMaterialsCosts = orderMaterialsCostDataGenerator.generateUpdatedMaterialsListFor(order);

        order.setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, orderMaterialsCosts);

        return order.getDataDefinition().save(order);
    }

    public void fillRealProductionCost(final Entity order) {
        order.setField("productPricePerUnit", realProductionCostService.calculateRealProductionCost(order));
    }

    @Transactional(/* isolation = Isolation.READ_UNCOMMITTED */)
    private Either<String, Void> tryAcceptInboundDocumentsFor(final Entity order, boolean isNominalProductCost) {
        updateDocumentQuantities(order);

        DataDefinition documentDD = getDocumentDD();

        SearchResult searchResult = documentDD.find().add(SearchRestrictions.belongsTo(DocumentFieldsPFTD.ORDER, order))
                .add(SearchRestrictions.eq(DocumentFields.TYPE, DocumentType.INTERNAL_INBOUND.getStringValue())).list();

        Optional<BigDecimal> price;

        if (isNominalProductCost) {
            price = Optional.ofNullable(getNominalCost(order.getBelongsToField(OrderFields.PRODUCT)));
        } else {
            price = Optional.ofNullable(realProductionCostService.calculateRealProductionCost(order));
        }

        for (Entity document : searchResult.getEntities()) {
            fillInDocumentPositionsPrice(document, order.getBelongsToField(OrderFields.PRODUCT), price);

            if (DocumentState.of(document) == DocumentState.ACCEPTED) {
                LOG.warn("Document {} already accepted.", document.getId());
                continue;
            }

            Entity acceptedDocument = acceptInboundDocument(document);

            if (!acceptedDocument.isValid()) {
                documentStateChangeService.buildFailureStateChange(acceptedDocument.getId());

                for (ErrorMessage error : acceptedDocument.getGlobalErrors()) {
                    order.addGlobalError(error.getMessage(), error.getVars());
                }

                order.addGlobalError(L_ACCEPT_INBOUND_DOCUMENT_ERROR);

                return Either.left(L_ACCEPT_INBOUND_DOCUMENT_ERROR);
            }
        }

        Either<String, Void> documentsForNotUsedMaterials = createDocumentsForNotUsedMaterials(order);

        if (documentsForNotUsedMaterials.isLeft()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return documentsForNotUsedMaterials;
    }

    private BigDecimal getNominalCost(final Entity product) {
        BigDecimal nominalCost = BigDecimalUtils.convertNullToZero(product.getDecimalField("nominalCost"));
        Entity currency = product.getBelongsToField("nominalCostCurrency");

        if (Objects.nonNull(currency) && CurrencyService.PLN.equals(currencyService.getCurrencyAlphabeticCode())
                && !CurrencyService.PLN.equals(currency.getStringField(CurrencyFields.ALPHABETIC_CODE))) {
            nominalCost = currencyService.getConvertedValue(nominalCost, currency);
        }

        return nominalCost;
    }

    private void fillInDocumentPositionsPrice(final Entity document, final Entity orderProduct, final Optional<BigDecimal> price) {
        Preconditions.checkNotNull(orderProduct, "Can't find order product.");
        Preconditions.checkNotNull(orderProduct.getId(), "Can't find order product.");

        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            if (orderProduct.getId().equals(position.getBelongsToField(PositionFields.PRODUCT).getId())) {
                position.setField(PositionFields.PRICE, price.orElse(BigDecimal.ZERO));

                if (DocumentState.of(document) == DocumentState.ACCEPTED) {
                    updatePriceInResource(position, price.orElse(BigDecimal.ZERO));
                }

                position.getDataDefinition().save(position);
            }
        }
    }

    private Either<String, Void> createDocumentsForNotUsedMaterials(final Entity order) {
        List<Entity> productionCountingQuantities = getUniqueProductionCountingQuantitiesForOrder(order);
        MultiMap groupedProductionCountingQuantities = groupProductionCountingQuantitiesByField(productionCountingQuantities,
                ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);

        for (Object inLocation : groupedProductionCountingQuantities.keySet()) {
            if (Objects.nonNull(inLocation)) {
                List<Entity> list = Lists.newArrayList();

                list.addAll((Collection<Entity>) groupedProductionCountingQuantities.get(inLocation));

                MultiMap groupedByOutputLocation = groupProductionCountingQuantitiesByField(list,
                        ProductionCountingQuantityFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);

                for (Object outLocation : groupedByOutputLocation.keySet()) {
                    if (Objects.nonNull(outLocation)) {
                        Either<String, Void> isValid = createTransferDocumentsForUnusedMaterials((Entity) outLocation,
                                (Entity) inLocation, getProductsAndQuantitiesMap(groupedByOutputLocation, order), order);

                        if (isValid.isLeft()) {
                            return isValid;
                        }
                    }
                }
            }
        }

        return Either.right(null);
    }

    private Map<Entity, BigDecimal> getProductsAndQuantitiesMap(final MultiMap groupedProductQuantities, final Entity order) {
        Map<Entity, BigDecimal> map = Maps.newHashMap();

        for (Object pcq : groupedProductQuantities.values()) {
            Entity product = ((Entity) pcq).getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

            BigDecimal quantityTaken = getProductQuantityTakenForOrder(product.getId(), order.getId());
            BigDecimal quantityUsed = getProductQuantityUsedInOrder(product.getId(), order.getId());

            BigDecimal diff = quantityTaken.subtract(quantityUsed);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                map.put(product, diff);
            }
        }

        return map;
    }

    private Either<String, Void> createTransferDocumentsForUnusedMaterials(final Entity locationFrom, final Entity locationTo,
                                                                           final Map<Entity, BigDecimal> products, final Entity order) {
        DocumentBuilder document = documentManagementService.getDocumentBuilder().transfer(locationFrom, locationTo);

        if (products.isEmpty()) {
            return Either.right(null);
        }

        for (Map.Entry<Entity, BigDecimal> entry : products.entrySet()) {
            Entity product = entry.getKey();
            BigDecimal quantity = entry.getValue();

            if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal givenQuantity = quantity;
                String givenUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
                BigDecimal conversion = BigDecimal.ONE;

                if (!StringUtils.isEmpty(givenUnit)) {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(
                            product.getStringField(ProductFields.UNIT), searchCriteriaBuilder -> searchCriteriaBuilder
                                    .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                    if (unitConversions.isDefinedFor(givenUnit)) {
                        givenQuantity = unitConversions.convertTo(quantity, givenUnit);
                        conversion = unitConversions.asUnitToConversionMap().get(givenUnit);
                    }
                } else {
                    givenUnit = product.getStringField(ProductFields.UNIT);
                }

                Entity position = document.createPosition(product, quantity, givenQuantity, givenUnit, conversion, null, null,
                        null, null, null);

                document.addPosition(position);
            }
        }

        Entity acceptedDocument = acceptTransferDocument(document, order);

        if (!acceptedDocument.isValid()) {
            for (ErrorMessage error : acceptedDocument.getGlobalErrors()) {
                order.addGlobalError(error.getMessage(), error.getVars());
            }

            order.addGlobalError(L_ACCEPT_INBOUND_DOCUMENT_ERROR);

            return Either.left(L_ACCEPT_INBOUND_DOCUMENT_ERROR);
        }

        return Either.right(null);
    }

    private Entity acceptTransferDocument(DocumentBuilder document, final Entity order) {
        document.setAccepted();

        document.setField(DocumentFields.TIME, new Date());
        document.setField(DocumentFieldsPFTD.ORDER, order);

        return document.build();
    }

    private MultiMap groupProductionCountingQuantitiesByField(final List<Entity> productionCountingQuantities, final String field) {
        MultiMap map = new MultiHashMap();

        for (Entity pcq : productionCountingQuantities) {
            Entity entity = pcq.getBelongsToField(field);

            map.put(entity, pcq);
        }

        return map;
    }

    private List<Entity> getUniqueProductionCountingQuantitiesForOrder(final Entity order) {
        Map<Entity, Entity> quantities = Maps.newHashMap();

        SearchCriteriaBuilder scb = getProductionCountingQuantityDD().find();

        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()));

        for (Entity pcq : scb.list().getEntities()) {
            quantities.put(pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT), pcq);
        }

        return Lists.newArrayList(quantities.values());
    }

    private BigDecimal getProductQuantityTakenForOrder(final Long productId, final Long orderId) {
        String hql = "SELECT SUM(issue.issueQuantity) AS totalQuantity "
                + "FROM #productFlowThruDivision_issue issue JOIN issue.warehouseIssue AS warehouseIssue "
                + "WHERE warehouseIssue.order = :order_id AND issue.product = :product_id AND issue.issued = :issued "
                + "GROUP BY warehouseIssue.order, issue.product, issue.issued";

        SearchQueryBuilder searchQueryBuilder = getIssueDD().find(hql);

        searchQueryBuilder.setLong("order_id", orderId);
        searchQueryBuilder.setLong("product_id", productId);
        searchQueryBuilder.setBoolean("issued", true);

        Entity result = searchQueryBuilder.setMaxResults(1).uniqueResult();

        return Objects.nonNull(result) ? result.getDecimalField("totalQuantity") : BigDecimal.ZERO;
    }


    private BigDecimal getProductQuantityUsedInOrder(final Long productId, final Long orderId) {
        String hql = "SELECT opic.usedQuantity AS quantity "
                + "FROM #productionCounting_trackingOperationProductInComponent opic "
                + "JOIN opic.productionTracking AS pt WHERE pt.state != '04corrected' "
                + "AND pt.order = :order_id AND opic.product = :product_id AND opic.usedQuantity IS NOT NULL "
                + "GROUP BY pt.order, opic.product, opic.usedQuantity";

        SearchQueryBuilder scb = getTrackingOperationProductInComponentDD().find(hql);

        scb.setLong("order_id", orderId);
        scb.setLong("product_id", productId);

        List<Entity> results = scb.list().getEntities();

        return results.stream().map(entity -> entity.getDecimalField("quantity")).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }


    private void updatePriceInResource(final Entity position, final BigDecimal price) {
        if (StringUtils.isNotEmpty(position.getStringField(PositionFields.RESOURCE_RECEIPT_DOCUMENT))) {
            Entity resource = getResourceDD().get(Long.valueOf(position.getStringField(PositionFields.RESOURCE_RECEIPT_DOCUMENT)));

            if (Objects.nonNull(resource)) {
                resource.setField(ResourceFields.PRICE, price);

                resource.getDataDefinition().save(resource);
            }
        }
    }

    private Entity acceptInboundDocument(final Entity document) {
        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        document.setField(DocumentFields.TIME, new Date());

        Entity savedDocument = document.getDataDefinition().save(document);

        if (savedDocument.isValid()) {
            resourceManagementService.createResourcesForReceiptDocuments(savedDocument);
        }

        return savedDocument;
    }

    private void updateDocumentQuantities(final Entity order) {
        List<Entity> productionTrackings = getProductionTrackingDD().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED))
                .list().getEntities();

        List<Entity> trackingOperationProductOutComponents = Lists.newArrayList();

        for (Entity productionTracking : productionTrackings) {
            trackingOperationProductOutComponents.addAll(Lists.newArrayList(productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS)));
        }

        Multimap<Long, Entity> groupedRecordOutProducts = productionTrackingDocumentsHelper
                .fillFromBPCProductOut(trackingOperationProductOutComponents, order, true);

        for (Long warehouseId : groupedRecordOutProducts.keySet()) {
            Entity locationTo = getLocationDD().get(warehouseId);

            List<Entity> finalProductRecord = Lists.newArrayList();

            Collection<Entity> intermediateRecords = Lists.newArrayList();

            for (Entity trackingOperationProductOutComponent : groupedRecordOutProducts.get(warehouseId)) {
                if (isFinalProductOrWasteForOrder(trackingOperationProductOutComponent)) {
                    finalProductRecord.add(trackingOperationProductOutComponent);
                } else {
                    intermediateRecords.add(trackingOperationProductOutComponent);
                }
            }

            Entity existingInboundDocument = getDocumentDD().find()
                    .add(SearchRestrictions.belongsTo(DocumentFieldsPFTD.ORDER, order))
                    .add(SearchRestrictions.belongsTo(DocumentFields.LOCATION_TO, locationTo))
                    .add(SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()))
                    .add(SearchRestrictions.eq(DocumentFields.TYPE, DocumentType.INTERNAL_INBOUND.getStringValue()))
                    .setMaxResults(1).uniqueResult();

            if (Objects.nonNull(existingInboundDocument)) {
                if (Objects.nonNull(finalProductRecord)) {
                    productionTrackingListenerServicePFTD.updateInternalInboundDocumentForFinalProducts(order, existingInboundDocument,
                            finalProductRecord, true, true);
                } else {
                    productionTrackingListenerServicePFTD.updateInternalInboundDocumentForFinalProducts(order, existingInboundDocument,
                            intermediateRecords, false, true);
                }
            }
        }
    }

    public void checkMaterialAvailability(final StateChangeContext stateChangeContext) {
        Entity order = stateChangeContext.getOwner();

        StateEnum targetState = stateChangeContext.getStateEnumValue(stateChangeContext.getDescriber().getTargetStateFieldName());

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        String momentOfValidation = parameterService.getParameter().getStringField(ParameterFieldsPFTD.MOMENT_OF_VALIDATION);

        String message = "productFlowThruDivision.order.state.accept.error.missingComponents";
        String messageShort = "productFlowThruDivision.order.state.accept.error.missingComponentsShort";

        if (MomentOfValidation.ORDER_STARTING.getStrValue().equals(momentOfValidation)) {
            message = "productFlowThruDivision.order.state.inProgress.error.missingComponents";
            messageShort = "productFlowThruDivision.order.state.inProgress.error.missingComponentsShort";
        }

        if (order.getField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS) != null
                && !order.getBooleanField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS) && technology != null
                && (OrderState.ACCEPTED == targetState
                && MomentOfValidation.ORDER_ACCEPTANCE.getStrValue().equals(momentOfValidation)
                || OrderState.IN_PROGRESS == targetState
                && MomentOfValidation.ORDER_STARTING.getStrValue().equals(momentOfValidation))) {

            List<Entity> entries = orderMaterialAvailability.generateMaterialAvailabilityForOrder(order);
            List<Entity> notAvailableProducts = entries.stream()
                    .filter(en -> !AvailabilityOfMaterialAvailability.FULL.getStrValue()
                            .equals(en.getStringField(MaterialAvailabilityFields.AVAILABILITY)))
                    .map(entity -> entity.getBelongsToField(MaterialAvailabilityFields.PRODUCT)).collect(Collectors.toList());

            if (!notAvailableProducts.isEmpty()) {
                String missingProductNames = StringUtils.join(notAvailableProducts.stream()
                        .map(product -> product.getStringField(ProductFields.NAME)).collect(Collectors.toList()), ", ");

                if (missingProductNames.length() < 255) {
                    stateChangeContext.addMessage(message, StateMessageType.FAILURE, false, missingProductNames);
                } else {
                    stateChangeContext.addMessage(messageShort, StateMessageType.FAILURE, false);
                }
            }
        }
    }

    public void checkOrderProductResourceReservationsInfo(final StateChangeContext stateChangeContext) {
        Entity order = stateChangeContext.getOwner();
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        List<Entity> tocs = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        Optional<Entity> any = tocs.stream().map(toc -> toc.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).filter(o -> o.getBooleanField(OperationFields.RESERVATION_RAW_MATERIAL_RESOURCE_REQUIRED)).findAny();
        any.ifPresent(entity -> {
            stateChangeContext.addMessage("productFlowThruDivision.orderProductResourceReservationDs.checkOrderProductResourceReservationsInfo", StateMessageType.INFO, false);
        });
    }

    public void validateOrderProductResourceReservations(final StateChangeContext stateChangeContext) {
        Entity order = stateChangeContext.getOwner();

        List<Entity> usedMaterialsFromProductionCountingQuantities = basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order, true);

        for (Entity pcq : usedMaterialsFromProductionCountingQuantities) {

            Entity operation = pcq.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT).getBelongsToField(TechnologyOperationComponentFields.OPERATION);
            if (operation.getBooleanField(OperationFields.RESERVATION_RAW_MATERIAL_RESOURCE_REQUIRED)
                    && Objects.nonNull(pcq.getHasManyField("orderProductResourceReservations"))
                    && pcq.getHasManyField("orderProductResourceReservations").isEmpty()) {
                stateChangeContext.addMessage("productFlowThruDivision.orderProductResourceReservationDs.checkOrderProductResourceReservationsInfo", StateMessageType.FAILURE, false);
                return;
            }
        }

    }

    public void createCumulatedInternalOutboundDocument(final StateChangeContext stateChangeContext) {
        Entity order = stateChangeContext.getOwner();

        productionCountingDocumentService.createCumulatedInternalOutboundDocument(order);
    }

    private boolean isFinalProductOrWasteForOrder(final Entity toc) {
        return toc.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue()) ||
                toc.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue()) ||
                toc.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue());
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }

    private DataDefinition getProductionTrackingDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING);
    }

    private DataDefinition getTrackingOperationProductInComponentDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT);
    }

    private DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

    private DataDefinition getIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_ISSUE);
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

}
