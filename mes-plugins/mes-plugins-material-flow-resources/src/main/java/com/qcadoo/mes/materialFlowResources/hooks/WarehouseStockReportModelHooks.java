package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WarehouseStockReportModelHooks {

    public void onSave(final DataDefinition warehouseStockReportDD, final Entity warehouseStockReport) {
        if (StorageLocationMode.ALL.getStringValue().equals(warehouseStockReport.getStringField("storageLocationMode"))) {
            warehouseStockReport.setField("storageLocations", null);
        }
        if (Objects.nonNull(warehouseStockReport.getId())) {
            Entity warehouseStockReportDb = warehouseStockReportDD.get(warehouseStockReport.getId());
            if (!warehouseStockReport.getBelongsToField("location").getId()
                    .equals(warehouseStockReportDb.getBelongsToField("location").getId())) {
                warehouseStockReport.setField("storageLocations", null);
            }
        }

    }
}
