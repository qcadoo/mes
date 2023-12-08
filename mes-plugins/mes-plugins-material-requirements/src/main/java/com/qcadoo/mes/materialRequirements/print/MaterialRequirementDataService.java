package com.qcadoo.mes.materialRequirements.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MaterialRequirementDataService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public Map<WarehouseDateKey, List<MaterialRequirementEntry>> getGroupedData(final Entity materialRequirement) {
        List<Entity> orders = materialRequirement.getHasManyField(MaterialRequirementFields.ORDERS);

        List<Entity> productionCountingQuantities = Lists.newArrayList();

        for (Entity order : orders) {
            if (materialRequirement.getStringField(MaterialRequirementFields.MRP_ALGORITHM)
                    .equals(MrpAlgorithm.ONLY_MATERIALS.getStringValue())) {
                productionCountingQuantities
                        .addAll(basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order, true));
            } else {
                productionCountingQuantities
                        .addAll(basicProductionCountingService.getUsedMaterialsFromProductionCountingQuantities(order, false));
            }
        }

        boolean includeWarehouse = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        boolean includeStartDateOrder = materialRequirement.getBooleanField(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        Entity location = materialRequirement.getBelongsToField(MaterialRequirementFields.LOCATION);

        List<MaterialRequirementEntry> materialRequirementEntries = Lists.newArrayList();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            MaterialRequirementEntry materialRequirementEntry = mapToMaterialRequirementEntry(productionCountingQuantity,
                    includeWarehouse, includeStartDateOrder);

            Long warehouseId = materialRequirementEntry.getWarehouseId();

            if (Objects.nonNull(location)) {
                if (location.getId().equals(warehouseId)) {
                    materialRequirementEntries.add(materialRequirementEntry);
                }
            } else {
                materialRequirementEntries.add(materialRequirementEntry);
            }
        }

        return convertToMap(materialRequirementEntries, includeWarehouse, includeStartDateOrder);
    }

    private MaterialRequirementEntry mapToMaterialRequirementEntry(final Entity productionCountingQuantity,
                                                                   final boolean includeWarehouse, final boolean includeStartDateOrder) {
        MaterialRequirementEntry materialRequirementEntry = new MaterialRequirementEntry();

        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        BigDecimal plannedQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

        materialRequirementEntry.setId(product.getId());
        materialRequirementEntry.setNumber(product.getStringField(ProductFields.NUMBER));
        materialRequirementEntry.setName(product.getStringField(ProductFields.NAME));
        materialRequirementEntry.setProduct(product);
        materialRequirementEntry.setPlannedQuantity(plannedQuantity);
        materialRequirementEntry.setUnit(product.getStringField(ProductFields.UNIT));
        materialRequirementEntry.setBatches(productionCountingQuantity.getHasManyField(ProductionCountingQuantityFields.BATCHES));

        if (includeStartDateOrder) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

            if (Objects.nonNull(order.getDateField(OrderFields.START_DATE))) {
                materialRequirementEntry.setOrderStartDate(new DateTime(order.getDateField(OrderFields.START_DATE))
                        .withTimeAtStartOfDay().toDate());
            } else {
                materialRequirementEntry.setOrderStartDate(null);
            }
        }

        if (includeWarehouse) {
            Entity warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.COMPONENTS_LOCATION);

            if (Objects.nonNull(warehouse)) {
                materialRequirementEntry.setWarehouseId(warehouse.getId());
                materialRequirementEntry.setWarehouseNumber(warehouse.getStringField(LocationFields.NUMBER));
                materialRequirementEntry.setWarehouse(warehouse);
            }
        }

        return materialRequirementEntry;
    }

    private Map<WarehouseDateKey, List<MaterialRequirementEntry>> convertToMap(final List<MaterialRequirementEntry> materialRequirementEntries,
                                                                               final boolean includeWarehouse, final boolean includeStartDateOrder) {
        Map<WarehouseDateKey, List<MaterialRequirementEntry>> materialRequirementEntriesMap = Maps.newHashMap();

        for (MaterialRequirementEntry materialRequirementEntry : materialRequirementEntries) {
            WarehouseDateKey warehouseDateKey = new WarehouseDateKey(materialRequirementEntry, includeWarehouse, includeStartDateOrder);

            if (materialRequirementEntriesMap.containsKey(warehouseDateKey)) {
                List<MaterialRequirementEntry> elements = materialRequirementEntriesMap.get(warehouseDateKey);

                elements.add(materialRequirementEntry);
            } else {
                materialRequirementEntriesMap.put(warehouseDateKey, Lists.newArrayList(materialRequirementEntry));
            }
        }

        return materialRequirementEntriesMap;
    }

    public Map<Long, Map<Long, BigDecimal>> getQuantitiesInStock(final List<? extends MaterialRequirementEntry> materialRequirementEntries) {
        Map<Long, Entity> warehouses = Maps.newHashMap();
        Map<Long, List<Entity>> warehouseProducts = Maps.newHashMap();

        for (MaterialRequirementEntry materialRequirementEntry : materialRequirementEntries) {
            Long warehouseId = materialRequirementEntry.getWarehouseId();
            Entity warehouse = materialRequirementEntry.getWarehouse();

            if (Objects.nonNull(warehouse)) {
                if (!warehouses.containsKey(warehouseId)) {
                    warehouses.put(warehouseId, warehouse);
                }

                if (warehouseProducts.containsKey(warehouseId)) {
                    List<Entity> products = warehouseProducts.get(warehouseId);

                    products.add(materialRequirementEntry.getProduct());
                } else {
                    warehouseProducts.put(warehouseId, Lists.newArrayList(materialRequirementEntry.getProduct()));
                }
            }
        }

        Map<Long, Map<Long, BigDecimal>> quantitiesInStock = Maps.newHashMap();

        for (Map.Entry<Long, List<Entity>> entry : warehouseProducts.entrySet()) {
            List<Entity> products = entry.getValue();
            Entity location = warehouses.get(entry.getKey());

            quantitiesInStock.put(entry.getKey(),
                    materialFlowResourcesService.getQuantitiesForProductsAndLocation(products, location));
        }

        return quantitiesInStock;
    }

    public BigDecimal getQuantity(final Map<Long, Map<Long, BigDecimal>> quantitiesInStock,
                                  final MaterialRequirementEntry material) {
        Map<Long, BigDecimal> quantitiesInWarehouse = quantitiesInStock.get(material.getWarehouseId());

        if (Objects.nonNull(quantitiesInWarehouse)) {
            return BigDecimalUtils.convertNullToZero(quantitiesInWarehouse.get(material.getId()));
        } else {
            return BigDecimal.ZERO;
        }
    }

    public Map<String, MaterialRequirementEntry> getNeededProductQuantities(final List<MaterialRequirementEntry> materialRequirementEntries) {
        Map<String, MaterialRequirementEntry> neededProductQuantities = Maps.newHashMap();

        for (MaterialRequirementEntry materialRequirementEntry : materialRequirementEntries) {
            String product = materialRequirementEntry.getNumber();

            if (neededProductQuantities.containsKey(product)) {
                BigDecimal plannedQuantity = materialRequirementEntry.getPlannedQuantity().add(neededProductQuantities.get(product).getPlannedQuantity());

                materialRequirementEntry.setPlannedQuantity(plannedQuantity);
            }

            neededProductQuantities.put(product, materialRequirementEntry);
        }

        return neededProductQuantities;
    }

}
