/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.2.0
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
package com.qcadoo.mes.basicProductionCounting.aop;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductQuantitiesServiceImplBPCOverrideUtil {

    @Autowired
    private ProductQuantitiesServiceImpl productQuantitiesServiceImpl;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Map<Long, BigDecimal> getProductComponentWithQuantitiesForOrders(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<Long> nonComponents, final boolean onTheFly) {
        Map<Long, Map<Long, BigDecimal>> productComponentWithQuantitiesForOrders = Maps.newHashMap();

        for (Entity order : orders) {
            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            String state = order.getStringField(OrderFields.STATE);

            if (!onTheFly
                    && (OrderStateStringValues.ACCEPTED.equals(state) || OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED
                            .equals(state))) {
                productComponentWithQuantitiesForOrders.put(order.getId(), getProductComponentWithQuantities(order));

                fillOperationRuns(operationRuns, order);
                fillNonComponents(nonComponents, order);
            } else {
                productComponentWithQuantitiesForOrders.put(order.getId(),
                        productQuantitiesServiceImpl.getProductComponentWithQuantitiesForTechnology(technology, plannedQuantity,
                                operationRuns, nonComponents));
            }
        }

        return productQuantitiesServiceImpl.groupProductComponentWithQuantities(productComponentWithQuantitiesForOrders);
    }

    private Map<Long, BigDecimal> getProductComponentWithQuantities(final Entity order) {
        Map<Long, BigDecimal> productComponentWithQuantities = Maps.newHashMap();

        List<Entity> productionCountingQuantities = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order)).list().getEntities();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            Entity operationProductInComponent = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT);
            Entity operationProductOutComponent = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT);
            BigDecimal plannedQuantity = productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

            if ((operationProductInComponent != null) || (operationProductOutComponent != null)) {
                if (operationProductInComponent != null) {
                    productComponentWithQuantities.put(operationProductInComponent.getId(), plannedQuantity);
                } else if (operationProductOutComponent != null) {
                    productComponentWithQuantities.put(operationProductOutComponent.getId(), plannedQuantity);
                }
            }
        }

        return productComponentWithQuantities;
    }

    private void fillOperationRuns(final Map<Long, BigDecimal> operationRuns, final Entity order) {
        List<Entity> productionCountingOperationRuns = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_OPERATON_RUN).find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order)).list().getEntities();

        for (Entity productionCountingOperationRun : productionCountingOperationRuns) {
            Entity technologyOperationComponent = productionCountingOperationRun
                    .getBelongsToField(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT);
            BigDecimal runs = productionCountingOperationRun.getDecimalField(ProductionCountingOperationRunFields.RUNS);

            operationRuns.put(technologyOperationComponent.getId(), runs);
        }
    }

    private void fillNonComponents(final Set<Long> nonComponents, final Entity order) {
        List<Entity> productionCountingQuantities = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.IS_NON_COMPONENT, true)).list().getEntities();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            Entity operationProductInComponent = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT);
            Entity operationProductOutComponent = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT);

            if ((operationProductInComponent != null) || (operationProductOutComponent != null)) {
                if (operationProductInComponent != null) {
                    nonComponents.add(operationProductInComponent.getId());
                } else if (operationProductOutComponent != null) {
                    nonComponents.add(operationProductOutComponent.getId());
                }
            }
        }
    }

}
