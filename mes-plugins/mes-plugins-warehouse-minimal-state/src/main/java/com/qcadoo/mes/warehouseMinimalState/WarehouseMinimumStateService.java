package com.qcadoo.mes.warehouseMinimalState;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseMinimumStateService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> getMinimalStateGreaterThanZero() {
        String query = "select state from #warehouseMinimalState_warehouseMinimumState as state where state.minimumState > 0";
        return getWarehouseMinimumStateDD().find(query).list().getEntities();
    }

    public List<Entity> getWarehouseStockWithTooSmallMinState() {
        String query = "select stock from #materialFlowResources_warehouseStock as stock where stock.minimumState > 0 "
                + "and COALESCE(stock.orderedQuantity,0) + COALESCE(stock.quantity,0) <= stock.minimumState";
        return getWarehouseStockDD().find(query).list().getEntities();
    }

    public DataDefinition getWarehouseMinimumStateDD() {
        return dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumState");
    }

    private DataDefinition getWarehouseStockDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "warehouseStock");
    }
}
