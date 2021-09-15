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

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchRestrictions.in;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsCriteria;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyRawInputProductComponentsDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
final class OrderMaterialsCostsDataGeneratorImpl implements OrderMaterialsCostDataGenerator {

    @Autowired
    private OrderMaterialCostsEntityBuilder orderMaterialCostsEntityBuilder;

    @Autowired
    private TechnologyRawInputProductComponentsDataProvider technologyRawInputProductComponentsDataProvider;

    @Autowired
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    public List<Entity> generateUpdatedMaterialsListFor(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology == null || technology.getId() == null) {
            return Lists.newArrayList();
        }
        List<Entity> existingOrderMaterialCosts;
        if (OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE))) {
            List<Entity> allTechnologyRawProducts = findRawInputProductsFor(order);
            final Set<Long> technologyRawProductIds = Sets.newHashSet();

            for (Entity product : allTechnologyRawProducts) {
                if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()
                        .equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
                    Entity productToProductGroupTechnology = technologyService.getProductToProductGroupTechnology(
                            order.getBelongsToField(TechnologyFields.PRODUCT), product.getId());
                    if (productToProductGroupTechnology != null) {
                        technologyRawProductIds.add(productToProductGroupTechnology
                                .getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT).getId());
                    }
                } else {
                    technologyRawProductIds.add(product.getId());
                }
            }
            existingOrderMaterialCosts = findExistingOrderMaterialCosts(order, technologyRawProductIds);
            return createMissingPendingOrderMaterialCostsEntities(order, allTechnologyRawProducts, existingOrderMaterialCosts);
        } else {
            existingOrderMaterialCosts = findExistingOrderMaterialCosts(order, null);
            return createMissingOrderMaterialCostsEntities(order, existingOrderMaterialCosts);

        }
    }

    private List<Entity> createMissingPendingOrderMaterialCostsEntities(final Entity order,
            final List<Entity> allTechnologyRawProductsWithCosts, final List<Entity> existingOrderMaterialCosts) {
        final Set<Long> existingMaterialCostIds = existingOrderMaterialCosts.stream()
                .map(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT)::apply)
                .map(EntityUtils.getIdExtractor()::apply).collect(Collectors.toSet());
        List<Entity> allOrderMaterialCosts = new ArrayList<>();
        for (Entity product : allTechnologyRawProductsWithCosts) {
            Long productId = product.getId();
            if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()
                    .equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
                Entity productToProductGroupTechnology = technologyService
                        .getProductToProductGroupTechnology(order.getBelongsToField(TechnologyFields.PRODUCT), product.getId());
                if (productToProductGroupTechnology != null) {
                    productId = productToProductGroupTechnology.getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT)
                            .getId();
                }
            }
            if (!existingMaterialCostIds.contains(productId)) {
                Entity entity = orderMaterialCostsEntityBuilder.create(order, product);
                allOrderMaterialCosts.add(entity);
            }
        }
        allOrderMaterialCosts.addAll(existingOrderMaterialCosts);
        List<Entity> technologyComponents = technologyService
                .findComponentsForTechnology(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
        List<Entity> components = Lists.newArrayList();
        for (Entity component : technologyComponents) {
            if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()
                    .equals(component.getStringField(ProductFields.ENTITY_TYPE))) {
                Entity productToProductGroupTechnology = technologyService
                        .getProductToProductGroupTechnology(order.getBelongsToField(TechnologyFields.PRODUCT), component.getId());
                if (productToProductGroupTechnology != null) {
                    component = productToProductGroupTechnology.getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT);
                }
            }
            components.add(component);
        }
        allOrderMaterialCosts = allOrderMaterialCosts.stream()
                .filter(materialCost -> isComponent(components,
                        materialCost.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT)))
                .collect(Collectors.toList());
        return allOrderMaterialCosts;
    }

    private List<Entity> createMissingOrderMaterialCostsEntities(final Entity order,
            final List<Entity> existingOrderMaterialCosts) {
        final Set<Long> existingMaterialCostIds = existingOrderMaterialCosts.stream()
                .map(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT)::apply)
                .map(EntityUtils.getIdExtractor()::apply).collect(Collectors.toSet());

        List<Entity> allOrderMaterialCosts = basicProductionCountingService
                .getUsedMaterialsFromProductionCountingQuantities(order, true).stream()
                .map(material -> orderMaterialCostsEntityBuilder.create(order,
                        material.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)))
                .collect(Collectors.toList());
        final Set<Long> allMaterialCostIds = allOrderMaterialCosts.stream()
                .map(EntityUtils.getBelongsToFieldExtractor(TechnologyInstOperProductInCompFields.PRODUCT)::apply)
                .map(EntityUtils.getIdExtractor()::apply).collect(Collectors.toSet());
        List<Entity> mergedOrderMaterialCosts = existingOrderMaterialCosts.stream()
                .filter(materialCost -> allMaterialCostIds
                        .contains(materialCost.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT).getId()))
                .collect(Collectors.toList());
        mergedOrderMaterialCosts.addAll(allOrderMaterialCosts.stream()
                .filter(materialCost -> !existingMaterialCostIds
                        .contains(materialCost.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT).getId()))
                .collect(Collectors.toList()));
        return mergedOrderMaterialCosts;
    }

    private boolean isComponent(final List<Entity> components, final Entity product) {
        return components.stream().anyMatch(component -> component.getId().equals(product.getId()));
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

    private List<Entity> findRawInputProductsFor(final Entity order) {
        TechnologyRawInputProductComponentsCriteria criteria = TechnologyRawInputProductComponentsCriteria
                .forTechnology(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
        criteria.setSearchProjection(
                list().add(alias(field(TechnologyRawInputProductComponentsCriteria.PRODUCT_ALIAS + ".id"), "id")));
        return technologyRawInputProductComponentsDataProvider.findAll(criteria).stream()
                .map(e -> e.getBelongsToField(OperationProductInComponentFields.PRODUCT)).collect(Collectors.toList());
    }

}
