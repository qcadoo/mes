/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
