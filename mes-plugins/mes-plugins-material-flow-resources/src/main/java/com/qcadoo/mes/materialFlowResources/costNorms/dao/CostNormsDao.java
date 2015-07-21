package com.qcadoo.mes.materialFlowResources.costNorms.dao;

import java.util.Collection;
import java.util.List;

import com.qcadoo.mes.materialFlowResources.costNorms.dao.model.CostNorm;

public interface CostNormsDao {

    public List<CostNorm> getLastPurchaseCostsForProducts(List<Long> productIds, List<Long> warehousesIds);

    public List<CostNorm> getAverageCostForProducts(List<Long> productIds, List<Long> warehousesIds);

    public void updateCostNormsForProducts(Collection<CostNorm> costNorms);
}
