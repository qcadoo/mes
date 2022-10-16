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
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.*;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

@Service
public class OperationalTaskMaterialAvailability {


    private static final int REQUIRED_QUANTITY_SCALE = 5;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;


    public List<Entity> generateAndSaveMaterialAvailability(final Entity operationalTask) {
        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        deleteMaterialAvailability(operationalTask);

        List<Entity> materialsAvailability = createMaterialAvailability(order, operationalTask);

        updateQuantityAndAvailability(materialsAvailability);

        saveMaterialAvailability(materialsAvailability);

        return materialsAvailability;
    }

    private List<Entity> createMaterialAvailability(final Entity order, final Entity operationalTask) {
        List<Entity> materialsAvailability = createMaterialAvailabilityFromProductionCountingQuantities(order, operationalTask);

        operationalTask.setField(OperationalTaskFieldsPFTD.MATERIAL_AVAILABILITY, materialsAvailability);

        return materialsAvailability;
    }

    private List<Entity> createMaterialAvailabilityFromProductionCountingQuantities(final Entity order, final Entity operationalTask) {
        List<Entity> newOrderMaterialAvailability = Lists.newArrayList();

        DataDefinition materialAvailabilityDD = getOperationalTaskMaterialAvailabilityDD();

        Map<Long, Map<Long, List<Entity>>> groupedMaterials = getGroupedMaterials(order, operationalTask);

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
                    String typOfMaterial = warehouseEntry.getValue().stream().findFirst().get().getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
                    for (Entity productionCountingQuantity : warehouseEntry.getValue()) {
                        for (Entity batch : productionCountingQuantity.getHasManyField(ProductionCountingQuantityFields.BATCHES)) {
                            batchesById.put(batch.getId(), batch);
                        }
                    }

                    newOrderMaterialAvailability.add(createMaterialAvailability(materialAvailabilityDD, product, typOfMaterial,
                            operationalTask, totalQuantity, location, batchesById.values()));
                }
            }
        }

        return newOrderMaterialAvailability;
    }

    private Entity createMaterialAvailability(final DataDefinition orderMaterialAvailabilityDD,
                                              final Entity product, final String typeOfMaterial,
                                              final Entity operationalTask, final BigDecimal requiredQuantity, final Entity location,
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

        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.OPERATIONAL_TASK, operationalTask);
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.TYPE_OF_MATERIAL, typeOfMaterial);
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.PRODUCT, product);
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.UNIT, product.getField(ProductFields.UNIT));
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.REQUIRED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(requiredQuantity, REQUIRED_QUANTITY_SCALE));
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.LOCATION, location);

        if (!replacements.isEmpty()) {
            materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.REPLACEMENT, true);
        }

        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.BATCHES, batches);
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.BATCHES_ID, batchesId);
        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.BATCHES_QUANTITY, getBatchesQuantity(batchesList, product, location));

        return materialAvailability;
    }

    private void updateQuantityAndAvailability(final List<Entity> materialsAvailability) {
        Map<Long, Map<Long, BigDecimal>> availableComponents = prepareAvailableComponents(materialsAvailability);

        for (Entity materialAvailability : materialsAvailability) {
            Entity location = materialAvailability.getBelongsToField(OperationalTaskMaterialAvailabilityFields.LOCATION);

            if (availableComponents.containsKey(location.getId())) {
                Entity product = materialAvailability.getBelongsToField(OperationalTaskMaterialAvailabilityFields.PRODUCT);

                Map<Long, BigDecimal> availableComponentsInLocation = availableComponents.get(location.getId());

                if (availableComponentsInLocation.containsKey(product.getId())) {
                    BigDecimal availableQuantity = availableComponentsInLocation.get(product.getId());

                    if (availableQuantity.compareTo(materialAvailability
                            .getDecimalField(OperationalTaskMaterialAvailabilityFields.REQUIRED_QUANTITY)) >= 0) {
                        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABILITY,
                                AvailabilityOfMaterialAvailability.FULL.getStrValue());
                    } else if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABILITY,
                                AvailabilityOfMaterialAvailability.NONE.getStrValue());
                    } else {
                        materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABILITY,
                                AvailabilityOfMaterialAvailability.PARTIAL.getStrValue());
                    }

                    materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABLE_QUANTITY, availableQuantity);
                } else {
                    materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABILITY,
                            AvailabilityOfMaterialAvailability.NONE.getStrValue());
                    materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                }
            } else {
                materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABILITY,
                        AvailabilityOfMaterialAvailability.NONE.getStrValue());
                materialAvailability.setField(OperationalTaskMaterialAvailabilityFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
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
            if (Objects.nonNull(entry.getKey())) {
                Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                        Lists.newArrayList(entry.getValue()), entry.getKey());

                availability.put(entry.getKey().getId(), availableQuantities);
            }
        }

        return availability;
    }

    private void deleteMaterialAvailability(final Entity operationalTask) {
        EntityList operationalTaskMaterialAvailability = operationalTask.getHasManyField(OrderFieldsPFTD.MATERIAL_AVAILABILITY);

        if (!operationalTaskMaterialAvailability.isEmpty()) {
            DataDefinition materialAvailabilityDD = getOperationalTaskMaterialAvailabilityDD();

            for (Entity materialAvailability : operationalTaskMaterialAvailability) {
                materialAvailabilityDD.delete(materialAvailability.getId());
            }
        }
    }

    private Map<Long, Map<Long, List<Entity>>> getGroupedMaterials(Entity order, Entity operationalTask) {
        Entity toc = operationalTask.getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        return basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order)
                .stream()
                .filter(material -> material.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT).getId().equals(toc.getId())
                        && material.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                        ProductionCountingQuantityRole.USED.getStringValue())
                        && material.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
                        ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()))
                .collect(
                        Collectors.groupingBy(
                                u -> ((Entity) u).getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId(),
                                Collectors.groupingBy(u -> ((Entity) u).getBelongsToField(
                                        ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION).getId())));
    }


    private BigDecimal getBatchesQuantity(final Collection<Entity> batches, final Entity product,
                                          final Entity location) {
        BigDecimal batchesQuantity = BigDecimal.ZERO;

        if (!batches.isEmpty()) {
            SearchCriteriaBuilder searchCriteriaBuilder =
                    getResourceDD().find()
                            .createAlias(ResourceFields.PRODUCT, ResourceFields.PRODUCT, JoinType.LEFT)
                            .createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.LEFT)
                            .createAlias(ResourceFields.BATCH, ResourceFields.BATCH, JoinType.LEFT)
                            .add(SearchRestrictions.eq(ResourceFields.PRODUCT + "." + "id", product.getId()))
                            .add(SearchRestrictions.eq(ResourceFields.LOCATION + "." + "id", location.getId()))
                            .add(SearchRestrictions.in(ResourceFields.BATCH + "." + "id", batches.stream().map(Entity::getId).collect(Collectors.toList())))
                            .setProjection(list().add(alias(sum(ResourceFields.AVAILABLE_QUANTITY), "sum")).add(rowCount()))
                            .addOrder(asc("sum"));

            Entity resource = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

            if (Objects.nonNull(resource)) {
                batchesQuantity = resource.getDecimalField("sum");
            }
        }

        return batchesQuantity;
    }

    private void saveMaterialAvailability(final List<Entity> materialsAvailability) {
        DataDefinition materialAvailabilityDD = getOperationalTaskMaterialAvailabilityDD();

        for (Entity materialAvailability : materialsAvailability) {
            materialAvailabilityDD.save(materialAvailability);
        }
    }

    private DataDefinition getOperationalTaskMaterialAvailabilityDD() {
        return dataDefinitionService.get(
                ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_OPER_TASK_MATERIAL_AVAILABILITY);
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }
}


