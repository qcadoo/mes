package com.qcadoo.mes.materialFlowResources.costNorms;

import java.util.List;

import com.qcadoo.model.api.Entity;

public interface CostNormsService {

    void updateCostNormsForProductsFromWarehouses(final List<Entity> products, final List<Entity> warehouses);

}
