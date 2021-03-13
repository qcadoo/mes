package com.qcadoo.mes.materialRequirements.print;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class MaterialRequirementDataService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public Map<WarehouseDateKey, List<MaterialRequirementEntry>> getGroupedData(final Entity materialRequirement) {
        Map<WarehouseDateKey, List<MaterialRequirementEntry>> data;

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

        List<MaterialRequirementEntry> entries = Lists.newArrayList();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            MaterialRequirementEntry materialRequirementEntry = mapToMaterialRequirementEntry(productionCountingQuantity,
                    includeWarehouse, includeStartDateOrder);

            entries.add(materialRequirementEntry);
        }

        data = convertToMap(entries, includeWarehouse, includeStartDateOrder);

        return data;
    }

    private Map<WarehouseDateKey, List<MaterialRequirementEntry>> convertToMap(List<MaterialRequirementEntry> entries,
            boolean includeWarehouse, boolean includeStartDateOrder) {
        Map<WarehouseDateKey, List<MaterialRequirementEntry>> data = Maps.newHashMap();

        for (MaterialRequirementEntry entry : entries) {
            WarehouseDateKey warehouseDateKey = new WarehouseDateKey(entry, includeWarehouse, includeStartDateOrder);

            if (data.containsKey(warehouseDateKey)) {
                List<MaterialRequirementEntry> elements = data.get(warehouseDateKey);

                elements.add(entry);
            } else {
                data.put(warehouseDateKey, Lists.newArrayList(entry));
            }
        }

        return data;
    }

    private MaterialRequirementEntry mapToMaterialRequirementEntry(final Entity productionCountingQuantity,
            final boolean includeWarehouse, final boolean includeStartDateOrder) {
        MaterialRequirementEntry materialRequirementEntry = new MaterialRequirementEntry();

        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        materialRequirementEntry
                .setId(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId());
        materialRequirementEntry.setNumber(product.getStringField(ProductFields.NUMBER));
        materialRequirementEntry.setName(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                .getStringField(ProductFields.NAME));
        materialRequirementEntry.setProduct(product);
        materialRequirementEntry.setPlannedQuantity(
                productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
        materialRequirementEntry.setUnit(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                .getStringField(ProductFields.UNIT));

        if (includeStartDateOrder) {
            if (Objects.nonNull(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER)
                    .getDateField(OrderFields.START_DATE))) {
                materialRequirementEntry.setOrderStartDate(
                        new DateTime(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER)
                                .getDateField(OrderFields.START_DATE)).withTimeAtStartOfDay().toDate());
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

    public Map<Long, Map<Long, BigDecimal>> getQuantitiesInStock(final List<? extends MaterialRequirementEntry> entries) {
        Map<Long, Entity> warehouses = Maps.newHashMap();
        Map<Long, List<Entity>> warehouseProducts = Maps.newHashMap();

        for (MaterialRequirementEntry entry : entries) {
            Long warehouseId = entry.getWarehouseId();
            Entity warehouse = entry.getWarehouse();

            if (Objects.nonNull(warehouse)) {
                if (!warehouses.containsKey(warehouseId)) {
                    warehouses.put(warehouseId, warehouse);
                }
                if (warehouseProducts.containsKey(warehouseId)) {
                    List<Entity> products = warehouseProducts.get(warehouseId);

                    products.add(entry.getProduct());
                } else {
                    warehouseProducts.put(warehouseId, Lists.newArrayList(entry.getProduct()));
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

}
