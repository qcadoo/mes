package com.qcadoo.mes.productionCounting.utils;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.TechnologyFieldsO;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductionTrackingDocumentsHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionTrackingDocumentsHelper.class);

    public static final String PRODUCTS_INPUT_LOCATION = "productsInputLocation";

    public static final String COMPONENTS_LOCATION = "componentsLocation";

    public static final String WITHIN_THE_PROCESS = "02withinTheProcess";

    public static final String PRODUCTION_FLOW = "productionFlow";

    public static final String PRODUCTS_FLOW_LOCATION = "productsFlowLocation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    public void fillProductsOutFromSet(Multimap<Long, Entity> groupedRecordOutProducts) {
        Map<Long, Entity> productsToRemove = Maps.newHashMap();

        for (Long warehouseId : groupedRecordOutProducts.keySet()) {
            List<Entity> recordOutProducts = new ArrayList<>();
            recordOutProducts.addAll(groupedRecordOutProducts.get(warehouseId));

            for (Entity recordOutProduct : recordOutProducts) {
                List<Entity> setEntities = dataDefinitionService
                        .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_SET_TRACKING_OPERATION_PRODUCT_IN_COMPONENTS).find()
                        .add(SearchRestrictions.belongsTo("trackingOperationProductOutComponent", recordOutProduct)).list()
                        .getEntities();

                if (!setEntities.isEmpty()) {
                    for (Entity setProduct : setEntities) {
                        Entity newRecordProduct = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT).create();
                        Entity product = setProduct.getBelongsToField("product");
                        BigDecimal quantity = setProduct.getDecimalField("quantityFromSets");
                        newRecordProduct.setField(TrackingOperationProductOutComponentFields.PRODUCT, product);
                        newRecordProduct.setField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY, quantity);
                        newRecordProduct.setField(TrackingOperationProductOutComponentFields.GIVEN_UNIT,
                                product.getStringField(ProductFields.UNIT));
                        newRecordProduct.setField(TrackingOperationProductOutComponentFields.USED_QUANTITY, quantity);
                        groupedRecordOutProducts.put(warehouseId, newRecordProduct);
                    }

                    productsToRemove.put(warehouseId, recordOutProduct);
                }
            }
        }

        for (Map.Entry<Long, Entity> entry : productsToRemove.entrySet()) {
            groupedRecordOutProducts.remove(entry.getKey(), entry.getValue());
        }
    }

    public void fillProductsInFromSet(Multimap<Long, Entity> groupedRecordInProducts) {
        Map<Long, Entity> productsToRemove = Maps.newHashMap();

        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            List<Entity> recordInProducts = new ArrayList<>();
            recordInProducts.addAll(groupedRecordInProducts.get(warehouseId));

            for (Entity recordInProduct : recordInProducts) {
                List<Entity> setEntities = dataDefinitionService
                        .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_SET_TECHNOLOGY_IN_COMPONENTS).find()
                        .add(SearchRestrictions.belongsTo("trackingOperationProductInComponent", recordInProduct)).list()
                        .getEntities();

                if (!setEntities.isEmpty()) {
                    for (Entity setProduct : setEntities) {
                        Entity newRecordProduct = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT).create();
                        Entity product = setProduct.getBelongsToField("product");
                        BigDecimal quantity = setProduct.getDecimalField("quantityFromSets");
                        newRecordProduct.setField(TrackingOperationProductInComponentFields.PRODUCT, product);
                        newRecordProduct.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, quantity);
                        newRecordProduct.setField(TrackingOperationProductInComponentFields.GIVEN_UNIT,
                                product.getStringField(ProductFields.UNIT));
                        newRecordProduct.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, quantity);
                        groupedRecordInProducts.put(warehouseId, newRecordProduct);
                    }

                    productsToRemove.put(warehouseId, recordInProduct);
                }
            }
        }

        for (Map.Entry<Long, Entity> entry : productsToRemove.entrySet()) {
            groupedRecordInProducts.remove(entry.getKey(), entry.getValue());
        }
    }

    public void fillFromBPCProductIn(Multimap<Long, Entity> groupedRecordInProducts, List<Entity> recordInProducts, Entity order) {
        List<Entity> entities = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue())).list().getEntities();

        for (Entity entity : entities) {
            Entity warehouse = entity.getBelongsToField(COMPONENTS_LOCATION);

            if (warehouse == null) {
                continue;
            }

            Entity product = entity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            Entity productionRecord = findProductionRecordByProduct(recordInProducts, product);

            if (productionRecord != null && !groupedRecordInProducts.containsEntry(warehouse.getId(), productionRecord)) {

                groupedRecordInProducts.put(warehouse.getId(), productionRecord);
            }
        }
    }

    public void fillFromBPCProductOut(Multimap<Long, Entity> groupedRecordOutProducts, List<Entity> recordOutProducts,
            Entity order) {
        List<Entity> entities = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue())).list().getEntities();

        for (Entity entity : entities) {
            Entity warehouse = entity.getBelongsToField(PRODUCTS_INPUT_LOCATION);

            if (warehouse == null) {
                continue;
            }

            Entity product = entity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            Entity productionRecord = findProductionRecordByProduct(recordOutProducts, product);

            if (productionRecord != null && !groupedRecordOutProducts.containsEntry(warehouse.getId(), productionRecord)) {
                groupedRecordOutProducts.put(warehouse.getId(), productionRecord);
            }
        }
    }

    public Entity findProductionRecordByProduct(final List<Entity> productionRecords, final Entity product) {
        return Iterables.find(productionRecords, new Predicate<Entity>() {

            @Override
            public boolean apply(Entity productionRecord) {
                BigDecimal usedQuantity = productionRecord
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                return product.getId().equals(
                        productionRecord.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId())
                        && usedQuantity != null && BigDecimal.ZERO.compareTo(usedQuantity) < 0;
            }
        }, null);
    }

    public Multimap<Long, Entity> groupRecordOutProductsByLocation(final List<Entity> recordProducts, final Entity technology) {
        DataDefinition operationProductInComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);

        SearchResult searchResult = operationProductInComponentDD.find()
                .createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "c", JoinType.INNER)
                .createAlias(OperationProductOutComponentFields.PRODUCT, "p", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("c." + TechnologyOperationComponentFields.TECHNOLOGY, technology)).list();

        Multimap<Long, Entity> groupedProductionRecords = ArrayListMultimap.create();

        for (Entity operationProductOutComponent : searchResult.getEntities()) {
            Optional<Entity> warehouse = Optional.absent();

            if (!technologyService.isFinalProduct(operationProductOutComponent)) {
                Entity productionCountingQuantity = findProductionCountingQuantity(operationProductOutComponent, technology);

                if (productionCountingQuantity == null) {
                    continue;
                }

                String productionFlow = productionCountingQuantity.getStringField(PRODUCTION_FLOW);

                if (WITHIN_THE_PROCESS.equals(productionFlow)) {
                    continue;
                }

                warehouse = Optional.fromNullable(productionCountingQuantity.getBelongsToField(PRODUCTS_FLOW_LOCATION));

                if (!warehouse.isPresent()) {
                    continue;
                }
            }

            if (!warehouse.isPresent()) {
                warehouse = Optional.fromNullable(operationProductOutComponent.getBelongsToField(PRODUCTS_INPUT_LOCATION));
            }

            if (!warehouse.isPresent()) {
                LOGGER.warn("Warehouse should not be empty in OPOC entity when plugin productFlowThruDivision is enabled.");

                continue;
            }

            Entity product = operationProductOutComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            Entity productionRecord = findProductionRecordByProduct(recordProducts, product);

            if (productionRecord != null) {
                groupedProductionRecords.put(warehouse.get().getId(), productionRecord);
            }
        }

        return groupedProductionRecords;
    }

    public Multimap<Long, Entity> groupRecordInProductsByWarehouse(final List<Entity> recordProducts, final Entity technology) {
        DataDefinition operationProductInComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        SearchResult searchResult = operationProductInComponentDD.find()
                .createAlias(OperationProductInComponentFields.OPERATION_COMPONENT, "c", JoinType.INNER)
                .createAlias(OperationProductInComponentFields.PRODUCT, "p", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("c." + TechnologyOperationComponentFields.TECHNOLOGY, technology)).list();

        Multimap<Long, Entity> groupedProductionRecords = ArrayListMultimap.create();

        for (Entity operationProductInComponent : searchResult.getEntities()) {
            Entity productionCountingQuantity = findProductionCountingQuantity(operationProductInComponent, technology);

            if (productionCountingQuantity == null) {
                continue;
            }

            String productionFlow = productionCountingQuantity.getStringField(PRODUCTION_FLOW);

            if (WITHIN_THE_PROCESS.equals(productionFlow)) {
                continue;
            }

            Optional<Entity> warehouse = Optional.fromNullable(productionCountingQuantity
                    .getBelongsToField(PRODUCTS_FLOW_LOCATION));

            if (!warehouse.isPresent()) {
                LOGGER.warn("Warehouse should not be empty in OPIC entity when plugin productFlowThruDivision is enabled.");

                continue;
            }

            Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            Entity productionRecord = findProductionRecordByProduct(recordProducts, product);

            if (productionRecord != null) {
                groupedProductionRecords.put(warehouse.get().getId(), productionRecord);
            }
        }

        return groupedProductionRecords;
    }

    public List<Long> findProductsWithInsufficientQuantity(final Entity productionTracking,
            final Multimap<Long, Entity> groupedRecordInProducts, final List<Entity> recordOutProducts) {
        boolean enoughResources = true;
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
                            ids.add(productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId());
                        } else if (productNotInStock.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                .compareTo(
                                        recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                                .add(productsNotInStock.get(warehouseFrom).get(productNotInStock))) > 0) {
                            ids.add(productNotInStock.getBelongsToField(
                                    TrackingOperationProductInComponentFields.PRODUCT).getId());
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

    private Map<Entity, Map<Entity, BigDecimal>> findProductsNotInStock(Multimap<Long, Entity> groupedRecordInProducts) {
        DataDefinition locationDD = dataDefinitionService.get("materialFlow", "location");

        Map<Entity, Map<Entity, BigDecimal>> productsNotInStock = Maps.newHashMap();
        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            Map<Entity, BigDecimal> productsNotInStockQuantities = Maps.newHashMap();
            Entity warehouseFrom = locationDD.get(warehouseId);
            Map<Long, BigDecimal> stockMap = getStock(groupedRecordInProducts, warehouseId, warehouseFrom);
            for (Entity recordInProduct : groupedRecordInProducts.get(warehouseId)) {
                BigDecimal productStock = stockMap.get(
                        recordInProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId());
                if (Objects.isNull(productStock)) {
                    productsNotInStockQuantities.put(recordInProduct, BigDecimal.ZERO);
                } else if (productStock.compareTo(recordInProduct
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)) < 0) {
                    productsNotInStockQuantities.put(recordInProduct, productStock);
                }
            }
            if (!productsNotInStockQuantities.isEmpty()) {
                productsNotInStock.put(warehouseFrom, productsNotInStockQuantities);
            }
        }
        return productsNotInStock;
    }

    private Map<Long, BigDecimal> getStock(Multimap<Long, Entity> groupedRecordInProducts, Long warehouseId, Entity warehouseFrom) {
        return getQuantitiesForProductsAndLocation(
                groupedRecordInProducts.get(warehouseId).stream()
                        .map(p -> p.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT))
                        .collect(Collectors.toList()), warehouseFrom);
    }

    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();
        if (products.size() > 0) {
            DataDefinition resourceDD = dataDefinitionService.get("materialFlowResources", "resource");
            StringBuilder sb = new StringBuilder();
            sb.append("select p.id as product, sum(r.quantity) as quantity ");
            sb.append("from #materialFlowResources_resource as r ");
            sb.append("join r.product as p ");
            sb.append("join r.location as l ");
            sb.append("group by p.id, l.id ");
            sb.append("having p.id in (:productIds) ");
            sb.append("and l.id = :locationId ");

            SearchQueryBuilder sqb = resourceDD.find(sb.toString());
            sqb.setParameter("locationId", location.getId());
            sqb.setParameterList("productIds", products.stream().map(product -> product.getId()).collect(Collectors.toList()));
            List<Entity> productsAndQuantities = sqb.list().getEntities();

            productsAndQuantities.stream().forEach(
                    productAndQuantity -> quantities.put((Long) productAndQuantity.getField("product"),
                            productAndQuantity.getDecimalField("quantity")));
        }
        return quantities;
    }

    private Entity findProductionCountingQuantity(final Entity operationProductComponent, final Entity technology) {
        Entity order = technology.getHasManyField(TechnologyFieldsO.ORDERS).find().setMaxResults(1).uniqueResult();
        Entity toc = operationProductComponent.getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
        Entity product = operationProductComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

        return dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

}
