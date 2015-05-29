package com.qcadoo.mes.materialFlowResources.costNorms;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.costNorms.dao.CostNormsDao;
import com.qcadoo.mes.materialFlowResources.costNorms.dao.model.CostNorm;
import com.qcadoo.model.api.Entity;

@Service
public class CostNormsServiceImpl implements CostNormsService {

    @Autowired
    private CostNormsDao costNormsDao;

    @Override
    public void updateCostNormsForProductsFromWarehouses(final List<Entity> products, final List<Entity> warehouses) {
        List<Long> productIds = products.stream().map(product -> product.getId()).collect(Collectors.toList());
        List<Long> warehousesIds = warehouses.stream().filter(warehouse -> warehouse != null).map(warehouse -> warehouse.getId())
                .collect(Collectors.toList());
        List<CostNorm> lastPurchases = costNormsDao.getLastPurchaseCostsForProducts(productIds, warehousesIds);
        List<CostNorm> averageCosts = costNormsDao.getAverageCostForProducts(productIds, warehousesIds);
        costNormsDao.updateCostNormsForProducts(mergeCostNorms(lastPurchases, averageCosts));
    }

    private Collection<CostNorm> mergeCostNorms(List<CostNorm> lastPurchases, List<CostNorm> averageCosts) {
        Map<Long, CostNorm> costNormMap = lastPurchases.stream().collect(
                Collectors.toMap(CostNorm::getProductId, purchase -> purchase, (p, q) -> p));
        for (CostNorm averageCost : averageCosts) {
            CostNorm purchase = costNormMap.get(averageCost.getProductId());
            if (purchase != null) {
                purchase.setAverageCost(averageCost.getAverageCost());
                costNormMap.put(averageCost.getProductId(), purchase);
            } else {
                costNormMap.put(averageCost.getProductId(), averageCost);
            }
        }
        return costNormMap.values();
    }

}
