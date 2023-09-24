package com.qcadoo.mes.productionCounting.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.UserService;

@Service
public class ProductionTrackingDocumentsHelper {

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_PRODUCT = "product";

    private static final String L_WAREHOUSE = "01warehouse";

    private static final String L_WITHIN_THE_PROCESS = "02withinTheProcess";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private UserService userService;

    public Multimap<Long, Entity> groupAndFilterInProducts(final Entity order, final List<Entity> trackingOperationProductInComponents) {
        Multimap<Long, Entity> groupedRecordInProducts = ArrayListMultimap.create();

        SearchCriteriaBuilder scb = getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order)).add(SearchRestrictions
                        .eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.USED.getStringValue()));

        List<Entity> productionCountingQuantities = scb.list().getEntities();

        for (Entity topic : trackingOperationProductInComponents) {
            BigDecimal usedQuantity = topic.getDecimalField(L_USED_QUANTITY);

            if (Objects.nonNull(usedQuantity) && BigDecimal.ZERO.compareTo(usedQuantity) < 0) {
                Entity product = topic.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
                Entity productionTracking = topic.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING);
                Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                Either<Boolean, Entity> eitherWarehouse = getWarehouseForProduct(productionCountingQuantities, toc, product);

                if (eitherWarehouse.isRight()) {
                    Entity warehouse = eitherWarehouse.getRight();

                    if (Objects.isNull(warehouse)) {
                        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder();

                        Entity emptyDocumentForErrorHandling = documentBuilder.createDocument(userService.getCurrentUserEntity());

                        emptyDocumentForErrorHandling.setNotValid();
                        emptyDocumentForErrorHandling.addGlobalError(
                                "productFlowThruDivision.productionCountingQuantity.productionCountingQuantityError.warehouseNotSet",
                                product.getStringField(ProductFields.NUMBER));

                        throw new DocumentBuildException(emptyDocumentForErrorHandling, Lists.newArrayList());
                    }

                    groupedRecordInProducts.put(warehouse.getId(), topic);
                }
            }
        }

        return groupedRecordInProducts;
    }

    private Either<Boolean, Entity> getWarehouseForProduct(List<Entity> productionCountingQuantities, Entity toc, Entity product) {
        if (Objects.nonNull(toc)) {
            Optional<Entity> maybeProductionCountingQuantity = productionCountingQuantities.stream().filter(
                            pcq -> Objects.nonNull(pcq.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT))
                                    && pcq.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT).getId()
                                    .equals(toc.getId()))
                    .filter(pcq -> pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId()
                            .equals(product.getId()))
                    .findFirst();

            if (maybeProductionCountingQuantity.isPresent()) {
                Entity productionCountingQuantity = maybeProductionCountingQuantity.get();

                return getWarehouseFromPCQ(productionCountingQuantity);
            }
        } else {
            Optional<Entity> maybeProductionCountingQuantity = productionCountingQuantities.stream()
                    .filter(pcq -> pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId()
                            .equals(product.getId()))
                    .findFirst();

            if (maybeProductionCountingQuantity.isPresent()) {
                Entity productionCountingQuantity = maybeProductionCountingQuantity.get();

                return getWarehouseFromPCQ(productionCountingQuantity);
            }
        }

        return Either.right(null);
    }

    private Either<Boolean, Entity> getWarehouseFromPCQ(Entity productionCountingQuantity) {
        if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
            return Either.right(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.COMPONENTS_LOCATION));
        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                && L_WAREHOUSE
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.PRODUCTION_FLOW))) {
            return Either.right(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION));
        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                && L_WITHIN_THE_PROCESS
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.PRODUCTION_FLOW))) {
            return Either.left(Boolean.TRUE);
        }

        return Either.right(null);
    }

    public final Multimap<Long, Entity> fillFromBPCProductIn(final List<Entity> trackingOperationProductInComponents,
                                                             final Entity order, final Entity technologyOperationComponent, final boolean withComponents) {
        SearchCriteriaBuilder scb = getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order)).add(SearchRestrictions
                        .eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.USED.getStringValue()));

        if (Objects.nonNull(technologyOperationComponent)) {
            scb = scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent));
        }

        List<Entity> productionCountingQuantities = scb.list().getEntities();

        Multimap<Long, Entity> groupedRecordInProducts = ArrayListMultimap.create();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            Entity warehouse;

            if (withComponents && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()
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

            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            Entity trackingOperationProductInComponent = findProductionRecordByProduct(trackingOperationProductInComponents,
                    product);

            if (Objects.nonNull(trackingOperationProductInComponent)) {
                if (!checkIfProductExists(groupedRecordInProducts, warehouse, product)) {
                    groupedRecordInProducts.put(warehouse.getId(), trackingOperationProductInComponent);
                }
            }
        }

        return groupedRecordInProducts;
    }

    private boolean checkIfProductExists(final Multimap<Long, Entity> groupedRecordInProducts, final Entity warehouse,
                                         final Entity product) {
        return groupedRecordInProducts.get(warehouse.getId()).stream()
                .anyMatch(trackingOperationProductInComponent -> trackingOperationProductInComponent
                        .getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId().equals(product.getId()));
    }

    public Multimap<Long, Entity> fillFromBPCProductOut(final List<Entity> trackingOperationProductOutComponents,
                                                        final Entity order, final boolean withWaste) {
        List<Entity> productionCountingQuantities = getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order)).add(SearchRestrictions
                        .eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .list().getEntities();

        Multimap<Long, Entity> groupedRecordOutProducts = ArrayListMultimap.create();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            Entity warehouse;

            if (withWaste && ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
                warehouse = productionCountingQuantity
                        .getBelongsToField(ProductionCountingQuantityFields.WASTE_RECEPTION_WAREHOUSE);
                if (Objects.isNull(warehouse)) {
                    continue;
                }
            } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                    && L_WAREHOUSE.equals(
                    productionCountingQuantity.getStringField(ProductionCountingQuantityFields.PRODUCTION_FLOW))) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION);
            } else if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                    || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
                warehouse = productionCountingQuantity
                        .getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION);
            } else {
                continue;
            }

            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            List<Entity> filteredTrackingOperationProductOutComponents = findProductionRecordsByProduct(trackingOperationProductOutComponents,
                    product);

            if (Objects.nonNull(filteredTrackingOperationProductOutComponents) && !filteredTrackingOperationProductOutComponents.isEmpty()) {
                groupedRecordOutProducts.putAll(warehouse.getId(), filteredTrackingOperationProductOutComponents);
            }
        }

        return groupedRecordOutProducts;
    }

    public List<Entity> findProductionRecordsByProduct(final List<Entity> trackingOperationProductComponents, final Entity product) {
        return trackingOperationProductComponents.stream().filter(trackingOperationProductComponent -> {
            BigDecimal usedQuantity = trackingOperationProductComponent.getDecimalField(L_USED_QUANTITY);

            return product.getId().equals(trackingOperationProductComponent.getBelongsToField(L_PRODUCT).getId())
                    && Objects.nonNull(usedQuantity) && BigDecimal.ZERO.compareTo(usedQuantity) < 0;
        }).collect(Collectors.toList());
    }

    public Entity findProductionRecordByProduct(final List<Entity> trackingOperationProductComponents, final Entity product) {
        return trackingOperationProductComponents.stream().filter(trackingOperationProductComponent -> {
            BigDecimal usedQuantity = trackingOperationProductComponent.getDecimalField(L_USED_QUANTITY);

            return product.getId().equals(trackingOperationProductComponent.getBelongsToField(L_PRODUCT).getId())
                    && Objects.nonNull(usedQuantity) && BigDecimal.ZERO.compareTo(usedQuantity) < 0;
        }).findFirst().orElse(null);
    }

    public List<Long> findProductsWithInsufficientQuantity(final Multimap<Long, Entity> groupedRecordInProducts,
                                                           final List<Entity> trackingOperationProductOutComponents) {
        List<Long> ids = Lists.newArrayList();

        Map<Entity, Map<Entity, BigDecimal>> productsNotInStock = findProductsNotInStock(groupedRecordInProducts);

        for (Entity warehouseFrom : productsNotInStock.keySet()) {
            for (Entity productNotInStock : productsNotInStock.get(warehouseFrom).keySet()) {
                boolean productInTrackingOperationProductOut = false;

                for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
                    if (productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId().equals(
                            trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId())) {
                        if (Objects.isNull(
                                trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY))) {
                            ids.add(productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)
                                    .getId());
                        } else if (productNotInStock.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                .compareTo(
                                        trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY)
                                                .add(productsNotInStock.get(warehouseFrom).get(productNotInStock))) > 0) {
                            ids.add(productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)
                                    .getId());
                        }

                        productInTrackingOperationProductOut = true;

                        break;
                    }
                }

                if (!productInTrackingOperationProductOut) {
                    ids.add(productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId());
                }
            }
        }

        return ids;
    }

    private Map<Entity, Map<Entity, BigDecimal>> findProductsNotInStock(final Multimap<Long, Entity> groupedRecordInProducts) {
        Map<Entity, Map<Entity, BigDecimal>> productsNotInStock = Maps.newHashMap();

        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            Map<Entity, BigDecimal> productsNotInStockQuantities = Maps.newHashMap();

            Entity warehouse = getLocationDD().get(warehouseId);

            Map<Long, Map<String, BigDecimal>> stockMap = getStock(groupedRecordInProducts, warehouseId, warehouse);

            for (Entity trackingOperationProductInComponent : groupedRecordInProducts.get(warehouseId)) {
                Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
                List<Entity> usedBatches = trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);

                Map<String, BigDecimal> batchQuantities = stockMap.get(product.getId());

                if (Objects.isNull(batchQuantities)) {
                    productsNotInStockQuantities.put(trackingOperationProductInComponent, BigDecimal.ZERO);
                } else {
                    if (usedBatches.isEmpty()) {
                        BigDecimal productStock = batchQuantities.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

                        if (productStock.compareTo(
                                trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)) < 0) {
                            productsNotInStockQuantities.put(trackingOperationProductInComponent, productStock);
                        }
                    } else {
                        usedBatches.forEach(usedBatch -> {
                            String batchNumber = usedBatch.getBelongsToField(UsedBatchFields.BATCH).getStringField(BatchFields.NUMBER);
                            BigDecimal quantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);

                            BigDecimal productStock = batchQuantities.get(batchNumber);

                            if (productStock.compareTo(quantity) < 0) {
                                BigDecimal usedQuantity = productsNotInStockQuantities.get(trackingOperationProductInComponent);

                                if (Objects.isNull(usedQuantity)) {
                                    usedQuantity = BigDecimal.ZERO;
                                }

                                usedQuantity = usedQuantity.add(productStock);

                                productsNotInStockQuantities.put(trackingOperationProductInComponent, usedQuantity);
                            }
                        });
                    }
                }
            }

            if (!productsNotInStockQuantities.isEmpty()) {
                productsNotInStock.put(warehouse, productsNotInStockQuantities);
            }
        }

        return productsNotInStock;
    }

    private Map<Long, Map<String, BigDecimal>> getStock(final Multimap<Long, Entity> groupedRecordInProducts, final Long warehouseId,
                                                        Entity warehouse) {
        return getQuantitiesForProductsAndLocation(Lists.newArrayList(groupedRecordInProducts.get(warehouseId)), warehouse);
    }

    public Map<Long, Map<String, BigDecimal>> getQuantitiesForProductsAndLocation(final List<Entity> trackingOperationProductInComponents, final Entity location) {
        Map<Long, Map<String, BigDecimal>> quantities = Maps.newHashMap();

        if (trackingOperationProductInComponents.size() > 0) {
            StringBuilder hql = new StringBuilder();

            hql.append("SELECT p.id AS productId, SUM(r.quantity) AS quantity, b.number AS batchNumber ");
            hql.append("FROM #materialFlowResources_resource AS r ");
            hql.append("JOIN r.product AS p ");
            hql.append("JOIN r.location AS l ");
            hql.append("LEFT JOIN r.batch AS b ");
            hql.append("GROUP BY p.id, l.id, b.number ");
            hql.append("HAVING p.id IN (:productIds) ");
            hql.append("AND l.id = :locationId ");

            SearchQueryBuilder sqb = getResourceDD().find(hql.toString());

            sqb.setParameter("locationId", location.getId());
            sqb.setParameterList("productIds", trackingOperationProductInComponents.stream().map(trackingOperationProductInComponent -> trackingOperationProductInComponent
                    .getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId()).collect(Collectors.toList()));

            List<Entity> productsAndQuantities = sqb.list().getEntities();

            productsAndQuantities.forEach(productAndQuantity -> {
                Long productId = (Long) productAndQuantity.getField("productId");
                String batchNumber = productAndQuantity.getStringField("batchNumber");

                if (Objects.isNull(batchNumber)) {
                    batchNumber = "";
                }

                Map<String, BigDecimal> batchQuantities = quantities.get(productId);

                if (Objects.isNull(batchQuantities)) {
                    batchQuantities = Maps.newHashMap();
                }

                batchQuantities.put(batchNumber, productAndQuantity.getDecimalField("quantity"));

                quantities.put(productId, batchQuantities);
            });
        }

        return quantities;
    }

    private DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

}
