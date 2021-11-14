package com.qcadoo.mes.productionCounting.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionTrackingDocumentsHelper {

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_PRODUCT = "product";

    private static final String L_WAREHOUSE = "01warehouse";

    @Autowired
    private DataDefinitionService dataDefinitionService;

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
                if (TypeOfProductionRecording.CUMULATED.getStringValue()
                        .equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    if (!checkIfProductExists(groupedRecordInProducts, warehouse, product)) {
                        groupedRecordInProducts.put(warehouse.getId(), trackingOperationProductInComponent);
                    }
                } else {
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
                        .getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION);

                if (Objects.isNull(warehouse)) {
                    continue;
                }
            } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                    && L_WAREHOUSE.equals(
                            productionCountingQuantity.getStringField(ProductionCountingQuantityFields.PRODUCTION_FLOW))) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION);
            } else if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue()
                    .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
                warehouse = productionCountingQuantity
                        .getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION);
            } else {
                continue;
            }

            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            Entity trackingOperationProductOutComponent = findProductionRecordByProduct(trackingOperationProductOutComponents,
                    product);

            if (Objects.nonNull(trackingOperationProductOutComponent)) {
                groupedRecordOutProducts.put(warehouse.getId(), trackingOperationProductOutComponent);
            }
        }

        return groupedRecordOutProducts;
    }

    public Entity findProductionRecordByProduct(final List<Entity> trackingOperationProductComponents, final Entity product) {
        return trackingOperationProductComponents.stream().filter(trackingOperationProductComponent -> {
            BigDecimal usedQuantity = trackingOperationProductComponent.getDecimalField(L_USED_QUANTITY);

            return product.getId().equals(trackingOperationProductComponent.getBelongsToField(L_PRODUCT).getId())
                    && Objects.nonNull(usedQuantity) && BigDecimal.ZERO.compareTo(usedQuantity) < 0;
        }).findFirst().orElse(null);
    }

    public List<Long> findProductsWithInsufficientQuantity(final Multimap<Long, Entity> groupedRecordInProducts,
            final List<Entity> recordOutProducts) {
        List<Long> ids = Lists.newArrayList();

        Map<Entity, Map<Entity, BigDecimal>> productsNotInStock = findProductsNotInStock(groupedRecordInProducts);

        for (Entity warehouseFrom : productsNotInStock.keySet()) {
            for (Entity productNotInStock : productsNotInStock.get(warehouseFrom).keySet()) {
                boolean productInTrackingOperationProductOut = false;

                for (Entity recordOutProduct : recordOutProducts) {
                    if (productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId().equals(
                            recordOutProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId())) {
                        if (Objects.isNull(
                                recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))) {
                            ids.add(productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)
                                    .getId());
                        } else if (productNotInStock.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                .compareTo(
                                        recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
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

            Map<Long, BigDecimal> stockMap = getStock(groupedRecordInProducts, warehouseId, warehouse);

            for (Entity recordInProduct : groupedRecordInProducts.get(warehouseId)) {
                BigDecimal productStock = stockMap
                        .get(recordInProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId());

                if (Objects.isNull(productStock)) {
                    productsNotInStockQuantities.put(recordInProduct, BigDecimal.ZERO);
                } else if (productStock.compareTo(
                        recordInProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)) < 0) {
                    productsNotInStockQuantities.put(recordInProduct, productStock);
                }
            }

            if (!productsNotInStockQuantities.isEmpty()) {
                productsNotInStock.put(warehouse, productsNotInStockQuantities);
            }
        }

        return productsNotInStock;
    }

    private Map<Long, BigDecimal> getStock(final Multimap<Long, Entity> groupedRecordInProducts, final Long warehouseId,
            Entity warehouse) {
        return getQuantitiesForProductsAndLocation(groupedRecordInProducts.get(warehouseId).stream()
                .map(trackingOperationProductInComponent -> trackingOperationProductInComponent
                        .getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT))
                .collect(Collectors.toList()), warehouse);
    }

    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();

        if (products.size() > 0) {
            String hql = "SELECT p.id AS product, sum(r.quantity) AS quantity " + "FROM #materialFlowResources_resource as r "
                    + "JOIN r.product AS p " + "JOIN r.location AS l " + "GROUP BY p.id, l.id " + "HAVING p.id IN (:productIds) "
                    + "AND l.id = :locationId ";
            SearchQueryBuilder sqb = getResourceDD().find(hql);

            sqb.setParameter("locationId", location.getId());
            sqb.setParameterList("productIds", products.stream().map(Entity::getId).collect(Collectors.toList()));

            List<Entity> productsAndQuantities = sqb.list().getEntities();

            productsAndQuantities.forEach(productAndQuantity -> quantities.put((Long) productAndQuantity.getField("product"),
                    productAndQuantity.getDecimalField("quantity")));
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
