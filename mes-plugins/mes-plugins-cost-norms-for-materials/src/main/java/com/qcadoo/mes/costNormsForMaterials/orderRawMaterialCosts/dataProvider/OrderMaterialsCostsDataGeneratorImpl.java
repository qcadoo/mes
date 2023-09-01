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
package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.EntityUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.qcadoo.model.api.search.SearchRestrictions.in;

@Service
final class OrderMaterialsCostsDataGeneratorImpl implements OrderMaterialsCostDataGenerator {

    @Autowired
    private OrderMaterialCostsEntityBuilder orderMaterialCostsEntityBuilder;

    @Autowired
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Override
    public List<Entity> generateUpdatedMaterialsListFor(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology == null || technology.getId() == null) {
            return Lists.newArrayList();
        }
        List<Entity> existingOrderMaterialCosts = findExistingOrderMaterialCosts(order, null);
        return createMissingOrderMaterialCostsEntities(order, existingOrderMaterialCosts);
    }

    private List<Entity> createMissingOrderMaterialCostsEntities(final Entity order, final List<Entity> existingOrderMaterialCosts) {
        final Set<Long> existingMaterialCostIds = existingOrderMaterialCosts.stream()
                .map(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT)::apply)
                .map(EntityUtils.getIdExtractor()::apply).collect(Collectors.toSet());

        Set<Entity> allOrderMaterialCosts = basicProductionCountingService
                .getUsedMaterialsFromProductionCountingQuantities(order, true)
                .stream()
                .map(material -> orderMaterialCostsEntityBuilder.create(order,
                        material.getBelongsToField(ProductionCountingQuantityFields.PRODUCT))).collect(Collectors.toSet());


        final Set<Long> allMaterialCostIds = allOrderMaterialCosts.stream()
                .map(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT)::apply)
                .map(EntityUtils.getIdExtractor()::apply).collect(Collectors.toSet());

        List<Entity> mergedOrderMaterialCosts = existingOrderMaterialCosts
                .stream()
                .filter(materialCost -> allMaterialCostIds.contains(materialCost.getBelongsToField(
                        TechnologyInstOperProductInCompFields.PRODUCT).getId())).collect(Collectors.toList());

        mergedOrderMaterialCosts.addAll(allOrderMaterialCosts
                .stream()
                .filter(materialCost -> !existingMaterialCostIds.contains(materialCost.getBelongsToField(
                        TechnologyInstOperProductInCompFields.PRODUCT).getId())).collect(Collectors.toList()));
        return mergedOrderMaterialCosts;
    }


    private List<Entity> findExistingOrderMaterialCosts(final Entity order, final Collection<Long> productIds) {
        if (order.getId() == null) {
            return Collections.emptyList();
        }
        OrderMaterialCostsCriteria criteria = OrderMaterialCostsCriteria.forOrder(order.getId());
        if (productIds != null) {
            criteria.setProductCriteria(in("id", productIds));
        }
        return orderMaterialCostsDataProvider.findAll(criteria);
    }

}
