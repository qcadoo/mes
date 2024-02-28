package com.qcadoo.mes.productFlowThruDivision.service;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.TrackingProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.reservation.OrderReservationsService;
import com.qcadoo.mes.productFlowThruDivision.states.ProductionTrackingListenerServicePFTD;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductionCountingDocumentService {

    private static final String L_WAREHOUSE = "01warehouse";

    private static final String L_ERROR_NOT_ENOUGH_RESOURCES = "materialFlow.error.position.quantity.notEnoughResources";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionTrackingListenerServicePFTD productionTrackingListenerServicePFTD;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private OrderReservationsService orderReservationsService;

    public void createCumulatedInternalOutboundDocument(final Entity order) {
        List<Entity> productionTrackings = getProductionTrackingDD().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingState.ACCEPTED.getStringValue()))
                .list().getEntities();

        List<Entity> trackingOperationProductInComponents = Lists.newArrayList();

        for (Entity productionTracking : productionTrackings) {
            if (productionTrackingListenerServicePFTD.notCreateDocumentsForIntermediateRecords(productionTracking)) {
                List<Entity> withoutIntermediateRecords = productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS)
                        .stream().filter(ProductionCountingDocumentService::filterIntermediates)
                        .collect(Collectors.toList());

                trackingOperationProductInComponents.addAll(withoutIntermediateRecords);
            } else {
                trackingOperationProductInComponents.addAll(
                        productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS));
            }
        }

        try {
            createInternalOutboundDocumentPT(order, trackingOperationProductInComponents);

            if (order.isValid()) {
                productionTrackingListenerServicePFTD.updateCostsForOrder(order);
            }
        } catch (DocumentBuildException documentBuildException) {
            boolean errorsDisplayed = true;

            for (ErrorMessage error : documentBuildException.getEntity().getGlobalErrors()) {
                if (error.getMessage().equalsIgnoreCase(L_ERROR_NOT_ENOUGH_RESOURCES)) {
                    order.addGlobalError(error.getMessage(), false, error.getVars());
                } else {
                    errorsDisplayed = false;
                    order.addGlobalError(error.getMessage(), error.getVars());
                }
            }

            if (!errorsDisplayed) {
                order.addGlobalError(
                        "productFlowThruDivision.productionCountingQuantity.productionCountingQuantityError.createInternalOutboundDocument");
            }
        }
    }

    private static boolean filterIntermediates(final Entity trackingOperationProductInComponent) {
        return !Strings.isNullOrEmpty(trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.TYPE_OF_MATERIAL))
                && !trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createInternalOutboundDocumentPT(final Entity order, final List<Entity> trackingOperationProductInComponents) {
        Multimap<Long, Entity> groupedRecordInProducts = productionTrackingDocumentsHelper.groupAndFilterInProducts(order,
                trackingOperationProductInComponents);

        if (checkIfProductsAvailableInStock(order, groupedRecordInProducts)) {
            for (Long locationId : groupedRecordInProducts.keySet()) {
                List<ProductionCountingQuantityHolder> entries = mapToHolderFromInProduct(groupedRecordInProducts.get(locationId));
                Entity locationFrom = getLocationDD().get(locationId);

                Entity document = createInternalOutboundDocumentOnCompletedOrder(locationFrom, order, entries);

                if (Objects.nonNull(document) && !document.isValid()) {
                    throw new DocumentBuildException(document, document.getHasManyField(DocumentFields.POSITIONS));
                }
            }
        }
    }

    public boolean checkIfProductsAvailableInStock(final Entity entity,
                                                   final Multimap<Long, Entity> groupedRecordInProducts) {
        DataDefinition locationDD = getLocationDD();

        for (Long locationId : groupedRecordInProducts.keySet()) {
            List<Entity> trackingOperationProductInComponents = (List<Entity>) groupedRecordInProducts.get(locationId);
            Entity location = locationDD.get(locationId);

            Map<Long, Map<String, BigDecimal>> productAndQuantities =
                    productionTrackingDocumentsHelper.getQuantitiesForProductsAndLocation(trackingOperationProductInComponents, location);

            checkIfResourcesAreSufficient(entity, productAndQuantities, trackingOperationProductInComponents, location);
        }

        return entity.isValid();
    }

    private boolean checkIfResourcesAreSufficient(final Entity entity,
                                                  final Map<Long, Map<String, BigDecimal>> productAndQuantities,
                                                  final Collection<Entity> trackingOperationProductInComponents,
                                                  final Entity location) {
        List<String> errorProducts = Lists.newArrayList();

        StringBuilder errorMessage = new StringBuilder();

        String locationNumber = location.getStringField(LocationFields.NUMBER);

        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
            List<Entity> usedBatches = trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);

            Map<String, BigDecimal> batchQuantities = productAndQuantities.get(product.getId());

            if (Objects.isNull(batchQuantities)) {
                errorProducts.add(product.getStringField(ProductFields.NUMBER));
            } else {
                if (usedBatches.isEmpty()) {
                    BigDecimal quantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                    BigDecimal availableQuantity = batchQuantities.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

                    if (Objects.isNull(availableQuantity) || quantity.compareTo(availableQuantity) > 0) {
                        errorProducts.add(product.getStringField(ProductFields.NUMBER));
                    }
                } else {
                    usedBatches.forEach(usedBatch -> {
                        String batchNumber = usedBatch.getBelongsToField(UsedBatchFields.BATCH).getStringField(BatchFields.NUMBER);
                        BigDecimal quantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);

                        BigDecimal availableQuantity = batchQuantities.get(batchNumber);

                        if (Objects.isNull(availableQuantity) || quantity.compareTo(availableQuantity) > 0) {
                            errorProducts.add(product.getStringField(ProductFields.NUMBER));
                        }
                    });
                }
            }
        }

        if (errorProducts.isEmpty()) {
            return true;
        }

        errorMessage.append(errorProducts.stream().distinct().collect(Collectors.joining(", ")));

        if (errorMessage.length() + locationNumber.length() < 255) {
            entity.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false,
                    errorMessage.toString(), locationNumber);
        } else {
            errorProducts.forEach(errorProduct ->
                    entity.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false,
                            errorProduct, locationNumber)
            );
        }

        return false;
    }

    private List<ProductionCountingQuantityHolder> mapToHolderFromInProduct(final Collection<Entity> trackingOperationProductInComponents) {
        List<ProductionCountingQuantityHolder> entities = Lists.newArrayList();

        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            List<Entity> usedBatches = trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);
            List<Entity> resourceReservations = trackingOperationProductInComponent.getHasManyField("resourceReservations");

            if (!resourceReservations.isEmpty()) {
                for (Entity resourceReservation : resourceReservations) {
                    Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

                    ProductionCountingQuantityHolder holder = preparePositionForResourceReservation(trackingOperationProductInComponent, product, resourceReservation);

                    orderReservationsService.updateReservationOnDocumentCreation(resourceReservation);

                    fillHolderList(entities, holder);
                }

                BigDecimal usedQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                BigDecimal sumUsedResourceQuantity = resourceReservations.
                        stream()
                        .map(rr -> rr.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY))
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal rest = usedQuantity.subtract(sumUsedResourceQuantity, numberService.getMathContext());

                if (rest.compareTo(BigDecimal.ZERO) > 0) {
                    Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

                    ProductionCountingQuantityHolder holder = createProductionCountingQuantityForQuantityHolder(trackingOperationProductInComponent, product, rest);

                    fillHolderList(entities, holder);
                }
            } else if (!usedBatches.isEmpty()) {
                Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

                for (Entity usedBatch : trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES)) {
                    ProductionCountingQuantityHolder holder = preparePositionForUsedBatch(trackingOperationProductInComponent, product, usedBatch);

                    fillHolderList(entities, holder);
                }
            } else {
                Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

                ProductionCountingQuantityHolder holder = createProductionCountingQuantityHolder(trackingOperationProductInComponent, product);

                fillHolderList(entities, holder);
            }
        }

        return entities;
    }

    private void fillHolderList(final List<ProductionCountingQuantityHolder> entities, final ProductionCountingQuantityHolder holder) {
        if (entities.contains(holder)) {
            int index = entities.indexOf(holder);

            ProductionCountingQuantityHolder exist = entities.get(index);

            exist.setUsedQuantity(exist.getUsedQuantity().add(BigDecimalUtils.convertNullToZero(holder.getUsedQuantity())));
            exist.setGivenQuantity(exist.getUsedQuantity().add(BigDecimalUtils.convertNullToZero(holder.getGivenQuantity())));
        } else {
            entities.add(holder);
        }
    }

    private ProductionCountingQuantityHolder createProductionCountingQuantityHolder(final Entity trackingOperationProductInComponent,
                                                                                    final Entity product) {
        BigDecimal usedQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
        BigDecimal givenQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = product.getStringField(ProductFields.UNIT);
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity) && Objects.nonNull(givenQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder();

        holder.setProductId(product.getId());
        holder.setProduct(product);
        holder.setUsedQuantity(usedQuantity);
        holder.setConversion(conversion);
        holder.setGivenQuantity(givenQuantity);
        holder.setGivenUnit(givenUnit);

        return holder;
    }

    private ProductionCountingQuantityHolder createProductionCountingQuantityForQuantityHolder(final Entity trackingOperationProductInComponent,
                                                                                               final Entity product,
                                                                                               final BigDecimal usedQuantity) {
        BigDecimal givenQuantity;
        BigDecimal conversion = BigDecimal.ONE;
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);
        String unit = product.getStringField(ProductFields.UNIT);

        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(givenUnit)) {
            givenQuantity = unitConversions.convertTo(usedQuantity, givenUnit, BigDecimal.ROUND_FLOOR);

            conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
        } else {
            givenQuantity = usedQuantity;
        }

        ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder();

        holder.setProductId(product.getId());
        holder.setProduct(product);
        holder.setUsedQuantity(usedQuantity);
        holder.setConversion(conversion);
        holder.setGivenQuantity(givenQuantity);
        holder.setGivenUnit(givenUnit);

        return holder;
    }

    private ProductionCountingQuantityHolder preparePositionForResourceReservation(final Entity trackingOperationProductInComponent,
                                                                                   final Entity product,
                                                                                   final Entity trackingProductResourceReservation) {
        Entity orderProductResourceReservationBT = trackingProductResourceReservation.getBelongsToField(TrackingProductResourceReservationFields.ORDER_PRODUCT_RESOURCE_RESERVATION);
        Entity orderProductResourceReservation = orderProductResourceReservationBT.getDataDefinition().get(orderProductResourceReservationBT.getId());
        Entity resourceBT = orderProductResourceReservation.getBelongsToField(OrderProductResourceReservationFields.RESOURCE);
        Entity resource = resourceBT.getDataDefinition().get(resourceBT.getId());

        BigDecimal usedQuantity = trackingProductResourceReservation.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY);
        BigDecimal givenQuantity = productionTrackingService.calculateGivenQuantity(trackingOperationProductInComponent, usedQuantity)
                .orElse(usedQuantity);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = product.getStringField(ProductFields.UNIT);
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder();

        holder.setProductId(product.getId());
        holder.setProduct(product);
        holder.setUsedQuantity(usedQuantity);
        holder.setConversion(conversion);
        holder.setGivenQuantity(givenQuantity);
        holder.setGivenUnit(givenUnit);
        holder.setResourceId(resource.getId());
        holder.setResource(resource);

        return holder;
    }

    private ProductionCountingQuantityHolder preparePositionForUsedBatch(final Entity trackingOperationProductInComponent,
                                                                         final Entity product, final Entity usedBatch) {
        BigDecimal usedQuantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);
        BigDecimal givenQuantity = productionTrackingService.calculateGivenQuantity(trackingOperationProductInComponent, usedQuantity)
                .orElse(usedQuantity);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = product.getStringField(ProductFields.UNIT);
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder();

        holder.setProductId(product.getId());
        holder.setProduct(product);
        holder.setUsedQuantity(usedQuantity);
        holder.setConversion(conversion);
        holder.setGivenQuantity(givenQuantity);
        holder.setGivenUnit(givenUnit);
        holder.setBatchId(usedBatch.getBelongsToField(UsedBatchFields.BATCH).getId());
        holder.setBatch(usedBatch.getBelongsToField(UsedBatchFields.BATCH));

        return holder;
    }

    public Entity createInternalOutboundDocumentOnCompletedOrder(final Entity locationFrom, final Entity order,
                                                                 final List<ProductionCountingQuantityHolder> inProductsRecords) {
        Entity user = getUserDD().get(securityService.getCurrentUserId());

        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalOutboundBuilder.internalOutbound(locationFrom);

        DataDefinition positionDD = getPositionDD();

        for (ProductionCountingQuantityHolder inProductRecord : inProductsRecords) {
            Entity position = preparePositionForInProductOnCompletedOrder(positionDD, inProductRecord);

            internalOutboundBuilder.addPosition(position);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        return internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();
    }

    private Entity preparePositionForInProductOnCompletedOrder(final DataDefinition positionDD,
                                                               final ProductionCountingQuantityHolder inProductRecord) {
        Entity product = inProductRecord.getProduct();
        String unit = product.getStringField(ProductFields.UNIT);

        Entity position = positionDD.create();

        position.setField(PositionFields.UNIT, unit);
        position.setField(PositionFields.GIVEN_UNIT, inProductRecord.getGivenUnit());
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, inProductRecord.getUsedQuantity());
        position.setField(PositionFields.GIVEN_QUANTITY, inProductRecord.getGivenQuantity());
        position.setField(PositionFields.CONVERSION, inProductRecord.getConversion());
        position.setField(PositionFields.BATCH, inProductRecord.getBatch());
        position.setField(PositionFields.RESOURCE, inProductRecord.getResource());

        return position;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createInternalOutboundDocument(final Entity order, final List<Entity> productionCountingQuantities,
                                               final boolean useUsedQuantity) {
        Multimap<Long, Entity> groupedPCQ = groupPCQByWarehouse(productionCountingQuantities);
        for (Long warehouseId : groupedPCQ.keySet()) {
            List<ProductionCountingQuantityHolder> entries = mapToHolder(groupedPCQ.get(warehouseId), useUsedQuantity);
            Entity locationFrom = getLocationDD().get(warehouseId);

            checkForEmptyPositions(entries, useUsedQuantity);

            Entity document = createInternalOutboundDocumentForComponents(locationFrom, order, entries, useUsedQuantity);

            if (Objects.nonNull(document) && !document.isValid()) {
                throw new DocumentBuildException(document, document.getHasManyField(DocumentFields.POSITIONS));
            }
        }
    }

    private Multimap<Long, Entity> groupPCQByWarehouse(final List<Entity> productionCountingQuantities) {
        Multimap<Long, Entity> groupedRecordInProducts = ArrayListMultimap.create();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            Entity warehouse;

            if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.COMPONENTS_LOCATION);
            } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                    && L_WAREHOUSE.equals(
                    productionCountingQuantity.getStringField(ProductionCountingQuantityFields.PRODUCTION_FLOW))) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION);
            } else {
                continue;
            }

            groupedRecordInProducts.put(warehouse.getId(), productionCountingQuantity);
        }

        return groupedRecordInProducts;
    }

    private List<ProductionCountingQuantityHolder> mapToHolder(final Collection<Entity> productionCountingQuantities,
                                                               final boolean useUsedQuantity) {
        List<ProductionCountingQuantityHolder> entities = Lists.newArrayList();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder(productionCountingQuantity, useUsedQuantity);

            if (entities.contains(holder)) {
                int index = entities.indexOf(holder);

                ProductionCountingQuantityHolder exist = entities.get(index);

                exist.setPlannedQuantity(
                        exist.getPlannedQuantity().add(productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)));
                exist.setUsedQuantity(exist.getUsedQuantity().add(
                        BigDecimalUtils.convertNullToZero(productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY))));
            } else {
                entities.add(holder);
            }
        }

        return entities;
    }

    private void checkForEmptyPositions(final List<ProductionCountingQuantityHolder> entries, final boolean useUsedQuantity) {
        for (ProductionCountingQuantityHolder entry : entries) {
            BigDecimal quantity;

            if (useUsedQuantity) {
                quantity = entry.getUsedQuantity();
            } else {
                quantity = entry.getPlannedQuantity().subtract(BigDecimalUtils.convertNullToZero(entry.getUsedQuantity()));
            }

            if (BigDecimal.ZERO.compareTo(quantity) >= 0) {
                DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder();

                Entity emptyDocumentForErrorHandling = documentBuilder.createDocument(userService.getCurrentUserEntity());

                emptyDocumentForErrorHandling.setNotValid();
                emptyDocumentForErrorHandling.addGlobalError(
                        "productFlowThruDivision.productionCountingQuantity.productionCountingQuantityError.emptyPosition", false,
                        entry.getProduct().getStringField(ProductFields.NUMBER));

                throw new DocumentBuildException(emptyDocumentForErrorHandling, Lists.newArrayList());
            }
        }
    }

    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
                                                              final List<ProductionCountingQuantityHolder> inProductsRecords,
                                                              final boolean useUsedQuantity) {
        Entity user = getUserDD().get(securityService.getCurrentUserId());

        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalOutboundBuilder.internalOutbound(locationFrom);

        DataDefinition positionDD = getPositionDD();

        for (ProductionCountingQuantityHolder inProductRecord : inProductsRecords) {
            Entity position = preparePositionForInProduct(positionDD, inProductRecord, useUsedQuantity);

            internalOutboundBuilder.addPosition(position);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        return internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD,
                                               final ProductionCountingQuantityHolder inProductRecord,
                                               final boolean useUsedQuantity) {
        BigDecimal quantity;

        if (useUsedQuantity) {
            quantity = inProductRecord.getUsedQuantity();
        } else {
            quantity = inProductRecord.getPlannedQuantity()
                    .subtract(BigDecimalUtils.convertNullToZero(inProductRecord.getUsedQuantity()));
        }

        Entity product = inProductRecord.getProduct();
        String unit = product.getStringField(ProductFields.UNIT);
        BigDecimal conversion = BigDecimal.ONE;

        Entity position = positionDD.create();

        position.setField(PositionFields.UNIT, unit);
        position.setField(PositionFields.GIVEN_UNIT, product.getStringField(ProductFields.ADDITIONAL_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, quantity);
        position.setField(PositionFields.CONVERSION, conversion);
        position.setField(PositionFields.BATCH, inProductRecord.getBatch());

        return position;
    }

    public void updateProductionCountingQuantity(final List<Entity> productionCountingQuantities) {
        for (Entity productionCountingQuantity : productionCountingQuantities) {
            BigDecimal quantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)
                    .subtract(BigDecimalUtils.convertNullToZero(
                            productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY)));
            BigDecimal usedQuantity = quantity.add(BigDecimalUtils.convertNullToZero(
                    productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY)));

            productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, usedQuantity);

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        }
    }

    private DataDefinition getProductionTrackingDD() {
        return dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING);
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getUserDD() {
        return dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER);
    }

}
