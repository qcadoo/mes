package com.qcadoo.mes.productFlowThruDivision.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
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
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
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

    public void createCumulatedInternalOutboundDocument(Entity order) {
        List<Entity> pcqs = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES);
        pcqs = pcqs.stream()
                .filter(p -> ProductionCountingQuantityRole.USED.getStringValue()
                        .equals(p.getStringField(ProductionCountingQuantityFields.ROLE))
                        && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()
                                .equals(p.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)))
                .collect(Collectors.toList());
        createInternalOutboundDocument(order, pcqs, true);
    }

    public void createInternalOutboundDocument(Entity order, List<Entity> productionCountingQuantities, boolean useUsedQuantity) {
        Multimap<Long, Entity> groupedPCQ = groupPCQByWarehouse(productionCountingQuantities);
        boolean errorsDisplayed = false;
        for (Long warehouseId : groupedPCQ.keySet()) {
            Entity locationFrom = getLocationDD().get(warehouseId);
            try {
                Entity document = createInternalOutboundDocumentForComponents(locationFrom, order, groupedPCQ.get(warehouseId),
                        useUsedQuantity);
                if (Objects.nonNull(document) && !document.isValid()) {
                    for (ErrorMessage error : document.getGlobalErrors()) {
                        if (error.getMessage().equalsIgnoreCase(L_ERROR_NOT_ENOUGH_RESOURCES)) {
                            order.addGlobalError(error.getMessage(), false, error.getVars());
                        } else if (!errorsDisplayed) {
                            order.addGlobalError(error.getMessage(), error.getVars());
                        }
                    }

                    if (!errorsDisplayed) {
                        order.addGlobalError(
                                "productFlowThruDivision.productionTracking.productionTrackingError.createInternalOutboundDocument");

                        errorsDisplayed = true;
                    }
                }
                if (errorsDisplayed) {
                    return;
                }
                updateProductionCountingQuantity(productionCountingQuantities);
                productionTrackingListenerServicePFTD.updateCostsForOrder(order);
            } catch (DocumentBuildException documentBuildException) {
                for (ErrorMessage error : documentBuildException.getEntity().getGlobalErrors()) {
                    if (error.getMessage().equalsIgnoreCase(L_ERROR_NOT_ENOUGH_RESOURCES)) {
                        order.addGlobalError(error.getMessage(), false, error.getVars());
                    } else if (!errorsDisplayed) {
                        order.addGlobalError(error.getMessage(), error.getVars());
                    }
                }

                if (!errorsDisplayed) {
                    order.addGlobalError(
                            "productFlowThruDivision.productionCountingQuantity.productionCountingQuantityError.createInternalOutboundDocument");
                }
                return;
            }
        }
    }

    private void updateProductionCountingQuantity(List<Entity> productionCountingQuantities) {
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

    @Transactional
    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
            final Collection<Entity> inProductsRecords, boolean useUsedQuantity) {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalOutboundBuilder.internalOutbound(locationFrom);

        HashSet<Entity> inProductsWithoutDuplicates = Sets.newHashSet();

        DataDefinition positionDD = getPositionDD();

        for (Entity inProductRecord : inProductsRecords) {
            Entity inProduct = inProductRecord.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

            if (!inProductsWithoutDuplicates.contains(inProduct)) {
                Entity position = preparePositionForInProduct(positionDD, inProductRecord, inProduct, useUsedQuantity);
                internalOutboundBuilder.addPosition(position);
            }

            inProductsWithoutDuplicates.add(inProduct);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        return internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD, final Entity inProductRecord,
            final Entity inProduct, boolean useUsedQuantity) {
        Entity position = positionDD.create();

        BigDecimal quantity = null;
        if (useUsedQuantity) {
            quantity = inProductRecord.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY);
        } else {
            quantity = inProductRecord.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY).subtract(BigDecimalUtils
                    .convertNullToZero(inProductRecord.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY)));
        }

        String unit = inProduct.getStringField(ProductFields.UNIT);

        position.setField(PositionFields.UNIT, unit);
        position.setField(PositionFields.GIVEN_UNIT, inProduct.getStringField(ProductFields.ADDITIONAL_UNIT));
        position.setField(PositionFields.PRODUCT, inProduct);
        position.setField(PositionFields.QUANTITY, quantity);

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
