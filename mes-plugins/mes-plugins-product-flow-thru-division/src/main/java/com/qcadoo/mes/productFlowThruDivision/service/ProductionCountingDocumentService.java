package com.qcadoo.mes.productFlowThruDivision.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.states.ProductionTrackingListenerServicePFTD;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;

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

    public void createCumulatedInternalOutboundDocument(Entity order) {

        List<Entity> productionTrackings = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingState.ACCEPTED.getStringValue()))
                .list().getEntities();

        List<Entity> trackingOperationProductInComponents = Lists.newArrayList();
        for (Entity productionTracking : productionTrackings) {
            if(productionTrackingListenerServicePFTD.notCreateDocumentsForIntermediateRecords(productionTracking)) {

                List<Entity> withoutIntermediateRecords = productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS)
                                .stream().filter(pin -> !Strings.isNullOrEmpty(pin.getStringField(TrackingOperationProductInComponentFields.TYPE_OF_MATERIAL))
                                && !pin.getStringField(TrackingOperationProductInComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()))
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createInternalOutboundDocumentPT(Entity order, List<Entity> trackingOperationProductInComponents) {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());
        Multimap<Long, Entity> groupedRecordInProducts = productionTrackingDocumentsHelper.groupAndFilterInProducts(order,
                trackingOperationProductInComponents);

        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            Entity locationFrom = getLocationDD().get(warehouseId);
            List<ProductionCountingQuantityHolder> entries = mapToHolderFromInProduct(groupedRecordInProducts.get(warehouseId));
            Entity document = createInternalOutboundDocumentOnCompletedOrder(locationFrom, order, entries);
            if (Objects.nonNull(document) && !document.isValid()) {
                throw new DocumentBuildException(document, document.getHasManyField(DocumentFields.POSITIONS));
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createInternalOutboundDocument(Entity order, List<Entity> productionCountingQuantities, boolean useUsedQuantity) {
        Multimap<Long, Entity> groupedPCQ = groupPCQByWarehouse(productionCountingQuantities);
        for (Long warehouseId : groupedPCQ.keySet()) {
            Entity locationFrom = getLocationDD().get(warehouseId);
            List<ProductionCountingQuantityHolder> entries = mapToHolder(groupedPCQ.get(warehouseId), useUsedQuantity);
            checkForEmptyPositions(useUsedQuantity, entries);
            Entity document = createInternalOutboundDocumentForComponents(locationFrom, order, entries, useUsedQuantity);
            if (Objects.nonNull(document) && !document.isValid()) {
                throw new DocumentBuildException(document, document.getHasManyField(DocumentFields.POSITIONS));
            }
        }
    }

    public void updateProductionCountingQuantity(List<Entity> productionCountingQuantities) {
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

    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
            final List<ProductionCountingQuantityHolder> inProductsRecords, boolean useUsedQuantity) {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

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

    public Entity createInternalOutboundDocumentOnCompletedOrder(final Entity locationFrom, final Entity order,
            final List<ProductionCountingQuantityHolder> inProductsRecords) {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

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

    private void checkForEmptyPositions(boolean useUsedQuantity, List<ProductionCountingQuantityHolder> entries) {
        for (ProductionCountingQuantityHolder entry : entries) {
            BigDecimal quantity = null;
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
                        "productFlowThruDivision.productionCountingQuantity.productionCountingQuantityError.emptyPosition",
                        entry.getProduct().getStringField(ProductFields.NUMBER));

                throw new DocumentBuildException(emptyDocumentForErrorHandling, Lists.newArrayList());
            }
        }
    }

    private List<ProductionCountingQuantityHolder> mapToHolder(Collection<Entity> pcqs, boolean useUsedQuantity) {
        List<ProductionCountingQuantityHolder> entities = Lists.newArrayList();
        for (Entity pcq : pcqs) {
            ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder(pcq, useUsedQuantity);
            if (entities.contains(holder)) {
                int index = entities.indexOf(holder);
                ProductionCountingQuantityHolder exist = entities.get(index);
                exist.setPlannedQuantity(
                        exist.getPlannedQuantity().add(pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)));
                exist.setUsedQuantity(exist.getUsedQuantity().add(
                        BigDecimalUtils.convertNullToZero(pcq.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY))));
            } else {
                entities.add(holder);
            }
        }
        return entities;
    }

    private List<ProductionCountingQuantityHolder> mapToHolderFromInProduct(Collection<Entity> inProducts) {
        List<ProductionCountingQuantityHolder> entities = Lists.newArrayList();
        for (Entity inProductRecord : inProducts) {
            if (inProductRecord.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES).isEmpty()) {
                Entity inProduct = inProductRecord.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
                ProductionCountingQuantityHolder holder = crateProductionCountingQuantityHolder(inProductRecord, inProduct);
                fillHolderList(entities, holder);
            } else {
                Entity inProduct = inProductRecord.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

                for (Entity usedBatch : inProductRecord.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES)) {
                    ProductionCountingQuantityHolder holder = preparePositionForUsedBatch(inProductRecord, inProduct, usedBatch);
                    fillHolderList(entities, holder);
                }
            }
        }
        return entities;
    }

    private void fillHolderList(List<ProductionCountingQuantityHolder> entities, ProductionCountingQuantityHolder holder) {
        if (entities.contains(holder)) {
            int index = entities.indexOf(holder);
            ProductionCountingQuantityHolder exist = entities.get(index);
            exist.setUsedQuantity(exist.getUsedQuantity().add(BigDecimalUtils.convertNullToZero(holder.getUsedQuantity())));
            exist.setGivenQuantity(exist.getUsedQuantity().add(BigDecimalUtils.convertNullToZero(holder.getGivenQuantity())));
        } else {
            entities.add(holder);
        }
    }

    private ProductionCountingQuantityHolder preparePositionForUsedBatch(Entity inProductRecord, Entity inProduct,
            final Entity usedBatch) {
        ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder();

        BigDecimal usedQuantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);
        BigDecimal givenQuantity = productionTrackingService.calculateGivenQuantity(inProductRecord, usedQuantity)
                .orElse(usedQuantity);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = inProduct.getStringField(ProductFields.UNIT);
        String givenUnit = inProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, inProduct)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        holder.setProductId(inProduct.getId());
        holder.setProduct(inProduct);
        holder.setUsedQuantity(usedQuantity);
        holder.setConversion(conversion);
        holder.setGivenQuantity(givenQuantity);
        holder.setGivenUnit(givenUnit);
        holder.setBatchId(usedBatch.getBelongsToField(UsedBatchFields.BATCH).getId());
        holder.setBatch(usedBatch.getBelongsToField(UsedBatchFields.BATCH));
        return holder;
    }

    private ProductionCountingQuantityHolder crateProductionCountingQuantityHolder(Entity inProductRecord, Entity inProduct) {
        ProductionCountingQuantityHolder holder = new ProductionCountingQuantityHolder();

        BigDecimal usedQuantity = inProductRecord.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
        BigDecimal givenQuantity = inProductRecord.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = inProduct.getStringField(ProductFields.UNIT);
        String givenUnit = inProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity) && Objects.nonNull(givenQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, inProduct)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        holder.setProductId(inProduct.getId());
        holder.setProduct(inProduct);
        holder.setUsedQuantity(usedQuantity);
        holder.setConversion(conversion);
        holder.setGivenQuantity(givenQuantity);
        holder.setGivenUnit(givenUnit);
        return holder;
    }

    private Entity preparePositionForInProductOnCompletedOrder(DataDefinition positionDD,
            ProductionCountingQuantityHolder inProductRecord) {
        Entity position = positionDD.create();
        Entity inProduct = inProductRecord.getProduct();
        String unit = inProduct.getStringField(ProductFields.UNIT);
        position.setField(PositionFields.UNIT, unit);
        position.setField(PositionFields.GIVEN_UNIT, inProductRecord.getGivenUnit());
        position.setField(PositionFields.PRODUCT, inProduct);
        position.setField(PositionFields.QUANTITY, inProductRecord.getUsedQuantity());
        position.setField(PositionFields.GIVEN_QUANTITY, inProductRecord.getGivenQuantity());
        position.setField(PositionFields.CONVERSION, inProductRecord.getConversion());
        position.setField(PositionFields.BATCH, inProductRecord.getBatch());
        return position;
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD,
            final ProductionCountingQuantityHolder inProductRecord, boolean useUsedQuantity) {
        Entity position = positionDD.create();

        BigDecimal quantity = null;
        if (useUsedQuantity) {
            quantity = inProductRecord.getUsedQuantity();
        } else {
            quantity = inProductRecord.getPlannedQuantity()
                    .subtract(BigDecimalUtils.convertNullToZero(inProductRecord.getUsedQuantity()));
        }

        Entity inProduct = inProductRecord.getProduct();
        String unit = inProduct.getStringField(ProductFields.UNIT);
        BigDecimal conversion = BigDecimal.ONE;

        position.setField(PositionFields.UNIT, unit);
        position.setField(PositionFields.GIVEN_UNIT, inProduct.getStringField(ProductFields.ADDITIONAL_UNIT));
        position.setField(PositionFields.PRODUCT, inProduct);
        position.setField(PositionFields.QUANTITY, quantity);
        position.setField(PositionFields.CONVERSION, conversion);
        position.setField(PositionFields.BATCH, inProductRecord.getBatch());

        return position;
    }

    private Multimap<Long, Entity> groupPCQByWarehouse(List<Entity> productionCountingQuantities) {
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

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

}
