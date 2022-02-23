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
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.AvailabilityOfMaterialAvailability;
import com.qcadoo.mes.productFlowThruDivision.constants.MaterialAvailabilityFields;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Entity> generateMaterialAvailabilityForOrder(Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (order.getId() == null || technology == null) {
            return Collections.EMPTY_LIST;
        }

        List<Entity> materialsAvailability = createMaterialAvailability(order, technology);

        updateQuantityAndAvailability(materialsAvailability);

        return materialsAvailability;
    }

    public List<Entity> generateAndSaveMaterialAvailabilityForOrder(Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (order.getId() == null || technology == null) {
            return Lists.newArrayList();
        }

        deleteMaterialAvailability(order);

        List<Entity> materialsAvailability = createMaterialAvailability(order, technology);

        updateQuantityAndAvailability(materialsAvailability);

        saveMaterialAvailability(materialsAvailability);

        return materialsAvailability;
    }

    private void saveMaterialAvailability(List<Entity> materialsAvailability) {
        DataDefinition orderMaterialAvailabilityDD = dataDefinitionService.get(
                ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);

        for (Entity materialAvailability : materialsAvailability) {
            orderMaterialAvailabilityDD.save(materialAvailability);
        }
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
            if (entry.getKey() != null) {
                Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                        Lists.newArrayList(entry.getValue()), entry.getKey());

                availability.put(entry.getKey().getId(), availableQuantities);
            }
        }

        return availability;
    }

    private void deleteMaterialAvailability(final Entity order) {
        EntityList orderMaterialAvailability = order.getHasManyField(OrderFieldsPFTD.MATERIAL_AVAILABILITY);

        if (!orderMaterialAvailability.isEmpty()) {
            DataDefinition materialAvailabilityDD = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                    ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);

            for (Entity materialAvailability : orderMaterialAvailability) {
                materialAvailabilityDD.delete(materialAvailability.getId());
            }
        }
    }

    private List<Entity> createMaterialAvailability(final Entity order, final Entity technology) {
        List<Entity> materialsAvailability;

        materialsAvailability = createMaterialAvailabilityFromProductionCountingQuantities(order);

        order.setField(OrderFieldsPFTD.MATERIAL_AVAILABILITY, materialsAvailability);

        return materialsAvailability;
    }


    private List<Entity> createMaterialAvailabilityFromProductionCountingQuantities(final Entity order) {
        List<Entity> newOrderMaterialAvailability = Lists.newArrayList();

        List<Entity> usedMaterials = basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order);

        DataDefinition orderMaterialAvailabilityDD = dataDefinitionService.get(
                ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);

        usedMaterials = usedMaterials
                .stream()
                .filter(material -> material.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                        ProductionCountingQuantityRole.USED.getStringValue())
                        && material.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
                                ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()))
                .collect(Collectors.toList());

        Map<Long, Map<Long, List<Entity>>> groupedMaterials = usedMaterials.stream().collect(
                Collectors.groupingBy(
                        u -> ((Entity) u).getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId(),
                        Collectors.groupingBy(u -> ((Entity) u).getBelongsToField(
                                ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION).getId())));

        for (Map.Entry<Long, Map<Long, List<Entity>>> productEntry : groupedMaterials.entrySet()) {
            Map<Long, List<Entity>> materialsForProduct = productEntry.getValue();

            for (Map.Entry<Long, List<Entity>> warehouseEntry : materialsForProduct.entrySet()) {
                if (!warehouseEntry.getValue().isEmpty()) {
                    Entity baseUsedMaterial = warehouseEntry.getValue().get(0);
                    BigDecimal totalQuantity = warehouseEntry.getValue().stream()
                            .map(m -> m.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Entity product = baseUsedMaterial.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
                    Entity location = baseUsedMaterial
                            .getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);

                    Map<Long, Entity> batchesById = Maps.newHashMap();
                    for (Entity pcq : warehouseEntry.getValue()) {
                        for (Entity batch : pcq.getHasManyField(ProductionCountingQuantityFields.BATCHES)) {
                            batchesById.put(batch.getId(), batch);
                        }
                    }



                    newOrderMaterialAvailability.add(createMaterialAvailabilityEntity(orderMaterialAvailabilityDD, product,
                            order, totalQuantity, location, batchesById.values()));
                }
            }
        }

        return newOrderMaterialAvailability;
    }

    private Entity createMaterialAvailabilityEntity(final DataDefinition orderMaterialAvailabilityDD, final Entity product,
                                                    final Entity order, final BigDecimal value, final Entity location, Collection<Entity> batchesList) {
        Entity materialAvailability = orderMaterialAvailabilityDD.create();

        List<Entity> replacements = product.getDataDefinition().get(product.getId())
                .getHasManyField(ProductFields.SUBSTITUTE_COMPONENTS);
        if (!replacements.isEmpty()) {
            materialAvailability.setField(MaterialAvailabilityFields.REPLACEMENT, true);
        }
        materialAvailability.setField(MaterialAvailabilityFields.ORDER, order);
        materialAvailability.setField(MaterialAvailabilityFields.PRODUCT, product);
        materialAvailability.setField(MaterialAvailabilityFields.UNIT, product.getField(ProductFields.UNIT));
        materialAvailability.setField(MaterialAvailabilityFields.REQUIRED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(value, REQUIRED_QUANTITY_SCALE));
        materialAvailability.setField(MaterialAvailabilityFields.LOCATION, location);
        String batches = batchesList.stream()
                .map(n -> n.getStringField(BatchFields.NUMBER))
                .collect(Collectors.joining( ", " ));
        String batchesId = batchesList.stream()
                .map(n -> n.getId().toString())
                .collect(Collectors.joining( "," ));
        materialAvailability.setField(MaterialAvailabilityFields.BATCHES, batches);
        materialAvailability.setField(MaterialAvailabilityFields.BATCHES_ID, batchesId);
        materialAvailability.setField("", order);

        return materialAvailability;
    }

}
