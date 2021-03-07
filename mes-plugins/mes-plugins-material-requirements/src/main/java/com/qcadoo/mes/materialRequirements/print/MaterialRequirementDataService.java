package com.qcadoo.mes.materialRequirements.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MaterialRequirementDataService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public Map<WarehouseDateKey, List<MaterialRequirementEntry>> getGroupedData(Entity materialRequirement) {
        Map<WarehouseDateKey, List<MaterialRequirementEntry>> data;

        List<Entity> orders = materialRequirement.getHasManyField(MaterialRequirementFields.ORDERS);

        List<Entity> productionCountingQuantities = Lists.newArrayList();

        for (Entity order : orders) {
            if (materialRequirement.getStringField(MaterialRequirementFields.MRP_ALGORITHM).equals(
                    MrpAlgorithm.ONLY_MATERIALS.getStringValue())) {
                productionCountingQuantities.addAll(basicProductionCountingService
                        .getUsedMaterialsFromProductionCountingQuantities(order, true));
            } else {
                productionCountingQuantities.addAll(basicProductionCountingService
                        .getUsedMaterialsFromProductionCountingQuantities(order, false));
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

    private MaterialRequirementEntry mapToMaterialRequirementEntry(Entity productionCountingQuantity, boolean includeWarehouse,
            boolean includeStartDateOrder) {
        MaterialRequirementEntry materialRequirementEntry = new MaterialRequirementEntry();
        materialRequirementEntry.setName(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                .getStringField(ProductFields.NAME));
        materialRequirementEntry.setNumber(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                .getStringField(ProductFields.NUMBER));
        materialRequirementEntry.setId(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                .getId());
        materialRequirementEntry.setUnit(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                .getStringField(ProductFields.UNIT));
        materialRequirementEntry.setPlannedQuantity(productionCountingQuantity
                .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));

        if (includeStartDateOrder) {
            if (Objects.nonNull(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER)
                    .getDateField(OrderFields.START_DATE))) {
                materialRequirementEntry.setOrderStartDate(new DateTime(productionCountingQuantity.getBelongsToField(
                        ProductionCountingQuantityFields.ORDER).getDateField(OrderFields.START_DATE)).withTimeAtStartOfDay()
                        .toDate().getTime());
            } else {
                materialRequirementEntry.setOrderStartDate(null);
            }
        }

        if (includeWarehouse) {
            Entity warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.COMPONENTS_LOCATION);
            if (Objects.nonNull(warehouse)) {
                materialRequirementEntry.setWarehouseNumber(warehouse.getStringField(LocationFields.NUMBER));
                materialRequirementEntry.setWarehouseId(warehouse.getId());
            }
        }
        return materialRequirementEntry;
    }

}
