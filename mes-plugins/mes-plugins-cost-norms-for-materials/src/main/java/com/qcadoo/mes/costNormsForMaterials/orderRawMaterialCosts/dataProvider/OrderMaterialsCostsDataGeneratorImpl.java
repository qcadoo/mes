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

import static com.qcadoo.model.api.search.SearchRestrictions.in;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.commons.functional.Optionals;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsCriteria;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
final class OrderMaterialsCostsDataGeneratorImpl implements OrderMaterialsCostDataGenerator {

    private static final SearchProjection PRODUCT_WITH_COSTS_PROJECTION = ProductWithCostsBuilder
            .buildProjectionForProduct(TechnologyRawInputProductComponentsCriteria.PRODUCT_ALIAS);

    @Autowired
    private OrderMaterialCostsEntityBuilder orderMaterialCostsEntityBuilder;

    @Autowired
    private TechnologyRawInputProductComponentsDataProvider technologyRawInputProductComponentsDataProvider;

    @Autowired
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Override
    public List<Entity> generateUpdatedMaterialsListFor(final Entity order) {
        for (Long technologyId : extractTechnologyIdFrom(order).asSet()) {
            List<ProductWithCosts> allTechnologyRawProductsWithCosts = findRawInputProductsFor(technologyId);
            final Set<Long> technologyRawProductIds = allTechnologyRawProductsWithCosts.stream()
                    .map(ProductWithCosts.EXTRACT_ID::apply).collect(Collectors.toSet());
            List<Entity> existingOrderMaterialCosts = findExistingOrderMaterialCosts(order, technologyRawProductIds);
            return createMissingOrderMaterialCostsEntities(order, allTechnologyRawProductsWithCosts, existingOrderMaterialCosts);
        }
        return ImmutableList.of();
    }

    private List<Entity> createMissingOrderMaterialCostsEntities(final Entity order,
            final List<ProductWithCosts> allTechnologyRawProductsWithCosts, final List<Entity> existingOrderMaterialCosts) {
        final Set<Long> existingMaterialCostIds = existingOrderMaterialCosts.stream()
                .map(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT)::apply)
                .map(EntityUtils.getIdExtractor()::apply).collect(Collectors.toSet());
        if (OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE))) {
            List<Entity> allOrderMaterialCosts = allTechnologyRawProductsWithCosts.stream()
                    .filter(productWithCosts -> !existingMaterialCostIds.contains(productWithCosts.getProductId()))
                    .map(productWithCosts -> orderMaterialCostsEntityBuilder.create(order, productWithCosts))
                    .collect(Collectors.toList());
            allOrderMaterialCosts.addAll(existingOrderMaterialCosts);
            return allOrderMaterialCosts;
        } else {
            List<Entity> allOrderMaterialCosts = basicProductionCountingService
                    .getUsedMaterialsFromProductionCountingQuantities(order, true)
                    .stream()
                    .map(material -> orderMaterialCostsEntityBuilder.create(order,
                            material.getBelongsToField(ProductionCountingQuantityFields.PRODUCT))).collect(Collectors.toList());
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
    }

    private List<Entity> findExistingOrderMaterialCosts(final Entity order, final Collection<Long> productIds) {
        if (order.getId() == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        OrderMaterialCostsCriteria criteria = OrderMaterialCostsCriteria.forOrder(order.getId());
        criteria.setProductCriteria(in("id", productIds));
        return orderMaterialCostsDataProvider.findAll(criteria);
    }

    private List<ProductWithCosts> findRawInputProductsFor(final Long technologyId) {
        TechnologyRawInputProductComponentsCriteria criteria = TechnologyRawInputProductComponentsCriteria
                .forTechnology(technologyId);
        criteria.setSearchProjection(PRODUCT_WITH_COSTS_PROJECTION);
        return asProductsWithCosts(technologyRawInputProductComponentsDataProvider.findAll(criteria));
    }

    private List<ProductWithCosts> asProductsWithCosts(final List<Entity> projectionResults) {
        return FluentIterable.from(projectionResults).transform(ProductWithCostsBuilder.BUILD_FROM_PROJECTION).toList();
    }

    private Optional<Long> extractTechnologyIdFrom(final Entity order) {
        return FluentOptional.fromNullable(order)
                .flatMap(Optionals.lift(EntityUtils.getBelongsToFieldExtractor(OrderFields.TECHNOLOGY)))
                .flatMap(EntityUtils.getSafeIdExtractor()).toOpt();
    }

}
