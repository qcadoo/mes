package com.qcadoo.mes.productFlowThruDivision;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderMaterialAvailability {

    private static final int REQUIRED_QUANTITY_SCALE = 5;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public List<Entity> generateMaterialAvailabilityForOrder(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(order.getId()) || Objects.isNull(technology)) {
            return Collections.emptyList();
        }

        List<Entity> materialsAvailability = createMaterialAvailability(order);

        updateQuantityAndAvailability(materialsAvailability);

        return materialsAvailability;
    }

    public Map<Long, String> generateMaterialAvailabilityForOrders(Set<Long> ordersIds) {
        DataDefinition orderDataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        List<Entity> orders = orderDataDefinition.find().add(SearchRestrictions.in("id", ordersIds)).list().getEntities();
        Map<Long, Map<Entity, Set<Entity>>> ordersLocationsProducts = Maps.newHashMap();
        Map<Long, Map<Long, BigDecimal>> locationsProductsQuantities = Maps.newHashMap();
        Map<Long, String> ordersAvailabilities = Maps.newHashMap();
        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (order.getId() == null || technology == null) {
                continue;
            }

            Map<Long, Map<Long, List<Entity>>> groupedMaterials = getGroupedMaterials(order);

            for (Map.Entry<Long, Map<Long, List<Entity>>> productEntry : groupedMaterials.entrySet()) {
                Map<Long, List<Entity>> materialsForProduct = productEntry.getValue();

                prepareLocationsProductsQuantitiesForOrder(ordersLocationsProducts, locationsProductsQuantities, order, materialsForProduct);
            }
        }
        Map<Long, Map<Long, BigDecimal>> availableComponents = prepareAvailableComponents(ordersLocationsProducts);

        return prepareOrdersAvailabilities(ordersLocationsProducts, locationsProductsQuantities, ordersAvailabilities, availableComponents);
    }

    private void prepareLocationsProductsQuantitiesForOrder(Map<Long, Map<Entity, Set<Entity>>> ordersLocationsProducts, Map<Long, Map<Long, BigDecimal>> locationsProductsQuantities, Entity order, Map<Long, List<Entity>> materialsForProduct) {
        for (Map.Entry<Long, List<Entity>> warehouseEntry : materialsForProduct.entrySet()) {
            if (!warehouseEntry.getValue().isEmpty()) {
                Entity baseUsedMaterial = warehouseEntry.getValue().get(0);
                BigDecimal totalQuantity = numberService.setScaleWithDefaultMathContext(warehouseEntry.getValue().stream()
                        .map(m -> m.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY))
                        .reduce(BigDecimal.ZERO, BigDecimal::add), REQUIRED_QUANTITY_SCALE);
                Entity product = baseUsedMaterial.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
                Entity location = baseUsedMaterial
                        .getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);

                prepareLocationProducts(ordersLocationsProducts, order, product, location);

                prepareProductQuantities(locationsProductsQuantities, totalQuantity, product, location);
            }
        }
    }

    private void prepareLocationProducts(Map<Long, Map<Entity, Set<Entity>>> ordersLocationsProducts, Entity order, Entity product, Entity location) {
        Map<Entity, Set<Entity>> locationProducts = ordersLocationsProducts.get(order.getId());
        if (locationProducts != null) {
            Set<Entity> products = locationProducts.get(location);
            if (products != null) {
                products.add(product);
            } else {
                locationProducts.put(location, Sets.newHashSet(product));
            }
        } else {
            locationProducts = Maps.newHashMap();
            locationProducts.put(location, Sets.newHashSet(product));
            ordersLocationsProducts.put(order.getId(), locationProducts);
        }
    }

    private void prepareProductQuantities(Map<Long, Map<Long, BigDecimal>> locationsProductsQuantities, BigDecimal totalQuantity, Entity product, Entity location) {
        Map<Long, BigDecimal> productQuantities = locationsProductsQuantities.get(location.getId());
        if (productQuantities != null) {
            productQuantities.merge(product.getId(), totalQuantity, BigDecimal::add);
        } else {
            productQuantities = Maps.newHashMap();
            productQuantities.put(product.getId(), totalQuantity);
            locationsProductsQuantities.put(location.getId(), productQuantities);
        }
    }

    private Map<Long, String> prepareOrdersAvailabilities(Map<Long, Map<Entity, Set<Entity>>> ordersLocationsProducts, Map<Long, Map<Long, BigDecimal>> locationsProductsQuantities, Map<Long, String> ordersAvailabilities, Map<Long, Map<Long, BigDecimal>> availableComponents) {
        for (Map.Entry<Long, Map<Entity, Set<Entity>>> olp : ordersLocationsProducts.entrySet()) {
            Long orderId = olp.getKey();
            prepareOrderAvailability(locationsProductsQuantities, ordersAvailabilities, availableComponents, olp, orderId);
        }
        return ordersAvailabilities;
    }

    private void prepareOrderAvailability(Map<Long, Map<Long, BigDecimal>> locationsProductsQuantities, Map<Long, String> ordersAvailabilities, Map<Long, Map<Long, BigDecimal>> availableComponents, Map.Entry<Long, Map<Entity, Set<Entity>>> olp, Long orderId) {
        boolean fullExists = false;
        boolean partialExists = false;
        boolean noneExists = false;
        for (Map.Entry<Entity, Set<Entity>> lps : olp.getValue().entrySet()) {
            Entity location = lps.getKey();

            if (availableComponents.containsKey(location.getId())) {
                Map<Long, BigDecimal> availableComponentsInLocation = availableComponents.get(location.getId());
                for (Entity product : lps.getValue()) {
                    if (availableComponentsInLocation.containsKey(product.getId())) {
                        BigDecimal availableQuantity = availableComponentsInLocation.get(product.getId());
                        if (availableQuantity.compareTo(locationsProductsQuantities.get(location.getId()).get(product.getId())) >= 0) {
                            fullExists = true;
                        } else if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
                            noneExists = true;
                        } else {
                            partialExists = true;
                            break;
                        }
                    } else {
                        noneExists = true;
                    }
                }
            } else {
                noneExists = true;
            }
        }
        evaluateOrderAvailability(ordersAvailabilities, orderId, fullExists, partialExists, noneExists);
    }

    private void evaluateOrderAvailability(Map<Long, String> ordersAvailabilities, Long orderId, boolean fullExists, boolean partialExists, boolean noneExists) {
        if (partialExists || fullExists && noneExists) {
            ordersAvailabilities.put(orderId, AvailabilityOfMaterialAvailability.PARTIAL.getStrValue());
        } else if (fullExists) {
            ordersAvailabilities.put(orderId, AvailabilityOfMaterialAvailability.FULL.getStrValue());
        } else if (noneExists) {
            ordersAvailabilities.put(orderId, AvailabilityOfMaterialAvailability.NONE.getStrValue());
        }
    }

    private Map<Long, Map<Long, BigDecimal>> prepareAvailableComponents(Map<Long, Map<Entity, Set<Entity>>> ordersLocationsProducts) {
        Map<Entity, Set<Entity>> groupedMaterialAvailabilities = Maps.newHashMap();

        for (Map<Entity, Set<Entity>> lps : ordersLocationsProducts.values()) {
            for (Map.Entry<Entity, Set<Entity>> lp : lps.entrySet()) {
                Entity location = lp.getKey();

                if (groupedMaterialAvailabilities.containsKey(location)) {
                    groupedMaterialAvailabilities.get(location).addAll(lp.getValue());
                } else {
                    groupedMaterialAvailabilities.put(location, Sets.newHashSet(lp.getValue()));
                }
            }
        }
        Map<Long, Map<Long, BigDecimal>> availableComponents = Maps.newHashMap();

        for (Map.Entry<Entity, Set<Entity>> entry : groupedMaterialAvailabilities.entrySet()) {
            if (entry.getKey() != null) {
                Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                        Lists.newArrayList(entry.getValue()), entry.getKey());

                availableComponents.put(entry.getKey().getId(), availableQuantities);
            }
        }
        return availableComponents;
    }

    private Map<Long, Map<Long, List<Entity>>> getGroupedMaterials(Entity order) {
        return basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order)
                .stream()
                .filter(material -> material.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                        ProductionCountingQuantityRole.USED.getStringValue())
                        && material.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
                        ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue())).collect(
                        Collectors.groupingBy(
                                u -> ((Entity) u).getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId(),
                                Collectors.groupingBy(u -> ((Entity) u).getBelongsToField(
                                        ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION).getId())));
    }

    public List<Entity> generateAndSaveMaterialAvailabilityForOrder(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(order.getId()) || Objects.isNull(technology)) {
            return Lists.newArrayList();
        }

        deleteMaterialAvailability(order);

        List<Entity> materialsAvailability = createMaterialAvailability(order);

        updateQuantityAndAvailability(materialsAvailability);

        saveMaterialAvailability(materialsAvailability);

        return materialsAvailability;
    }

    private void deleteMaterialAvailability(final Entity order) {
        EntityList orderMaterialAvailability = order.getHasManyField(OrderFieldsPFTD.MATERIAL_AVAILABILITY);

        if (!orderMaterialAvailability.isEmpty()) {
            DataDefinition materialAvailabilityDD = getOrderMaterialAvailabilityDD();

            for (Entity materialAvailability : orderMaterialAvailability) {
                materialAvailabilityDD.delete(materialAvailability.getId());
            }
        }
    }

    private List<Entity> createMaterialAvailability(final Entity order) {
        List<Entity> materialsAvailability = createMaterialAvailabilityFromProductionCountingQuantities(order);

        order.setField(OrderFieldsPFTD.MATERIAL_AVAILABILITY, materialsAvailability);

        return materialsAvailability;
    }

    private List<Entity> createMaterialAvailabilityFromProductionCountingQuantities(final Entity order) {
        List<Entity> newOrderMaterialAvailability = Lists.newArrayList();

        DataDefinition orderMaterialAvailabilityDD = getOrderMaterialAvailabilityDD();

        Map<Long, Map<Long, List<Entity>>> groupedMaterials = getGroupedMaterials(order);

        for (Map.Entry<Long, Map<Long, List<Entity>>> productEntry : groupedMaterials.entrySet()) {
            Map<Long, List<Entity>> materialsForProduct = productEntry.getValue();

            for (Map.Entry<Long, List<Entity>> warehouseEntry : materialsForProduct.entrySet()) {
                if (!warehouseEntry.getValue().isEmpty()) {
                    Entity baseUsedMaterial = warehouseEntry.getValue().get(0);
                    BigDecimal totalQuantity = warehouseEntry.getValue().stream()
                            .map(productionCountingQuantity -> productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Entity product = baseUsedMaterial.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
                    Entity location = baseUsedMaterial
                            .getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);

                    Map<Long, Entity> batchesById = Maps.newHashMap();

                    for (Entity productionCountingQuantity : warehouseEntry.getValue()) {
                        for (Entity batch : productionCountingQuantity.getHasManyField(ProductionCountingQuantityFields.BATCHES)) {
                            batchesById.put(batch.getId(), batch);
                        }
                    }

                    newOrderMaterialAvailability.add(createMaterialAvailability(orderMaterialAvailabilityDD, product,
                            order, totalQuantity, location, batchesById.values()));
                }
            }
        }

        return newOrderMaterialAvailability;
    }

    private Entity createMaterialAvailability(final DataDefinition orderMaterialAvailabilityDD,
                                              final Entity product,
                                              final Entity order, final BigDecimal requiredQuantity, final Entity location,
                                              final Collection<Entity> batchesList) {
        Entity materialAvailability = orderMaterialAvailabilityDD.create();

        List<Entity> replacements = product.getDataDefinition().get(product.getId())
                .getHasManyField(ProductFields.SUBSTITUTE_COMPONENTS);

        String batches = batchesList.stream()
                .map(batch -> batch.getStringField(BatchFields.NUMBER))
                .collect(Collectors.joining(", "));
        String batchesId = batchesList.stream()
                .map(batch -> batch.getId().toString())
                .collect(Collectors.joining(","));

        materialAvailability.setField(MaterialAvailabilityFields.ORDER, order);
        materialAvailability.setField(MaterialAvailabilityFields.PRODUCT, product);
        materialAvailability.setField(MaterialAvailabilityFields.UNIT, product.getField(ProductFields.UNIT));
        materialAvailability.setField(MaterialAvailabilityFields.REQUIRED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(requiredQuantity, REQUIRED_QUANTITY_SCALE));
        materialAvailability.setField(MaterialAvailabilityFields.LOCATION, location);

        if (!replacements.isEmpty()) {
            materialAvailability.setField(MaterialAvailabilityFields.REPLACEMENT, true);
        }

        materialAvailability.setField(MaterialAvailabilityFields.BATCHES, batches);
        materialAvailability.setField(MaterialAvailabilityFields.BATCHES_ID, batchesId);
        materialAvailability.setField(MaterialAvailabilityFields.BATCHES_QUANTITY, materialFlowResourcesService.getBatchesQuantity(batchesList, product, location));

        return materialAvailability;
    }

    private void updateQuantityAndAvailability(final List<Entity> materialsAvailability) {
        Map<Long, Map<Long, BigDecimal>> availableComponents = prepareAvailableComponents(materialsAvailability);

        for (Entity materialAvailability : materialsAvailability) {
            Entity location = materialAvailability.getBelongsToField(MaterialAvailabilityFields.LOCATION);

            if (availableComponents.containsKey(location.getId())) {
                Entity product = materialAvailability.getBelongsToField(MaterialAvailabilityFields.PRODUCT);

                Map<Long, BigDecimal> availableComponentsInLocation = availableComponents.get(location.getId());

                if (availableComponentsInLocation.containsKey(product.getId())) {
                    BigDecimal availableQuantity = availableComponentsInLocation.get(product.getId());

                    if (availableQuantity.compareTo(materialAvailability
                            .getDecimalField(MaterialAvailabilityFields.REQUIRED_QUANTITY)) >= 0) {
                        materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
                                AvailabilityOfMaterialAvailability.FULL.getStrValue());
                    } else if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
                                AvailabilityOfMaterialAvailability.NONE.getStrValue());
                    } else {
                        materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
                                AvailabilityOfMaterialAvailability.PARTIAL.getStrValue());
                    }

                    materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, availableQuantity);
                } else {
                    materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
                            AvailabilityOfMaterialAvailability.NONE.getStrValue());
                    materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                }
            } else {
                materialAvailability.setField(MaterialAvailabilityFields.AVAILABILITY,
                        AvailabilityOfMaterialAvailability.NONE.getStrValue());
                materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
            }
        }
    }

    private void saveMaterialAvailability(final List<Entity> materialsAvailability) {
        DataDefinition orderMaterialAvailabilityDD = getOrderMaterialAvailabilityDD();

        for (Entity materialAvailability : materialsAvailability) {
            orderMaterialAvailabilityDD.save(materialAvailability);
        }
    }

    private Map<Long, Map<Long, BigDecimal>> prepareAvailableComponents(final List<Entity> materialAvailabilities) {
        Map<Entity, Set<Entity>> groupedMaterialAvailabilities = Maps.newHashMap();

        materialAvailabilities.forEach(materialAvailability -> {
            Entity location = materialAvailability.getBelongsToField(MaterialAvailabilityFields.LOCATION);

            if (groupedMaterialAvailabilities.containsKey(location)) {
                groupedMaterialAvailabilities.get(location).add(
                        materialAvailability.getBelongsToField(MaterialAvailabilityFields.PRODUCT));
            } else {
                groupedMaterialAvailabilities.put(location,
                        Sets.newHashSet(materialAvailability.getBelongsToField(MaterialAvailabilityFields.PRODUCT)));
            }
        });

        Map<Long, Map<Long, BigDecimal>> availability = Maps.newHashMap();

        for (Map.Entry<Entity, Set<Entity>> entry : groupedMaterialAvailabilities.entrySet()) {
            if (Objects.nonNull(entry.getKey())) {
                Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                        Lists.newArrayList(entry.getValue()), entry.getKey());

                availability.put(entry.getKey().getId(), availableQuantities);
            }
        }

        return availability;
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getOrderMaterialAvailabilityDD() {
        return dataDefinitionService.get(
                ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);
    }

}
