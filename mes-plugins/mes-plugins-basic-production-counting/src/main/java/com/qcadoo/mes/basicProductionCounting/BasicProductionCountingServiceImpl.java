/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basicProductionCounting;

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCT;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.USED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields.RUNS;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.IS_NON_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.PLANNED_QUANTITY;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionCountingServiceImpl implements BasicProductionCountingService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void createProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<Entity> nonComponents = Sets.newHashSet();

        final Map<Entity, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentWithQuantities(
                Arrays.asList(order), operationRuns, nonComponents);

        for (Entry<Entity, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = operationRun.getKey();
            BigDecimal runs = operationRun.getValue();

            createProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            Entity operationProductComponent = productComponentQuantity.getKey();
            BigDecimal plannedQuantity = productComponentQuantity.getValue();
            Entity product = operationProductComponent.getBelongsToField(PRODUCT);

            boolean isNonComponent = nonComponents.contains(operationProductComponent);

            if (OPERATION_PRODUCT_IN_COMPONENT.equals(operationProductComponent.getDataDefinition().getName())) {
                createProductionCountingQuantity(order, operationProductComponent, null, product, plannedQuantity, isNonComponent);
            } else if (OPERATION_PRODUCT_OUT_COMPONENT.equals(operationProductComponent.getDataDefinition().getName())) {
                createProductionCountingQuantity(order, null, operationProductComponent, product, plannedQuantity, isNonComponent);
            }
        }

        createProductionCountingQuantity(order, null, null, order.getBelongsToField(PRODUCT),
                order.getDecimalField(PLANNED_QUANTITY), false);
    }

    private void createProductionCountingOperationRun(final Entity order, final Entity technologyOperationComponent,
            final BigDecimal runs) {
        Entity productionCountingOperationRun = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_OPERATON_RUN).create();

        productionCountingOperationRun.setField(ORDER, order);
        productionCountingOperationRun.setField(TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
        productionCountingOperationRun.setField(RUNS, numberService.setScale(runs));

        productionCountingOperationRun.getDataDefinition().save(productionCountingOperationRun);
    }

    private void createProductionCountingQuantity(final Entity order, final Entity operationProductInComponent,
            final Entity operationProductOutComponent, final Entity product, final BigDecimal plannedQuantity,
            final boolean isNonComponent) {
        Entity productionCountingQuantity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).create();

        productionCountingQuantity.setField(ORDER, order);
        productionCountingQuantity.setField(OPERATION_PRODUCT_IN_COMPONENT, operationProductInComponent);
        productionCountingQuantity.setField(OPERATION_PRODUCT_OUT_COMPONENT, operationProductOutComponent);
        productionCountingQuantity.setField(PRODUCT, product);
        productionCountingQuantity.setField(PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        productionCountingQuantity.setField(IS_NON_COMPONENT, isNonComponent);

        productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
    }

    public void updateProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<Entity> nonComponents = Sets.newHashSet();

        final Map<Entity, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentWithQuantities(
                Arrays.asList(order), operationRuns, nonComponents);

        for (Entry<Entity, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = operationRun.getKey();
            BigDecimal runs = operationRun.getValue();

            updateProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            Entity operationProductComponent = productComponentQuantity.getKey();
            BigDecimal plannedQuantity = productComponentQuantity.getValue();
            Entity product = operationProductComponent.getBelongsToField(PRODUCT);

            boolean isNonComponent = nonComponents.contains(operationProductComponent);

            if (OPERATION_PRODUCT_IN_COMPONENT.equals(operationProductComponent.getDataDefinition().getName())) {
                updateProductionCountingQuantity(order, operationProductComponent, null, product, plannedQuantity, isNonComponent);
            } else if (OPERATION_PRODUCT_OUT_COMPONENT.equals(operationProductComponent.getDataDefinition().getName())) {
                updateProductionCountingQuantity(order, null, operationProductComponent, product, plannedQuantity, isNonComponent);
            }
        }

        updateProductionCountingQuantity(order, null, null, order.getBelongsToField(PRODUCT),
                order.getDecimalField(PLANNED_QUANTITY), false);
    }

    private void updateProductionCountingOperationRun(final Entity order, final Entity technologyOperationComponent,
            final BigDecimal runs) {
        Entity productionCountingOperationRun = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_OPERATON_RUN).find()
                .add(SearchRestrictions.belongsTo(ORDER, order))
                .add(SearchRestrictions.belongsTo(TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent)).setMaxResults(1)
                .uniqueResult();

        if (productionCountingOperationRun != null) {
            productionCountingOperationRun.setField(ORDER, order);
            productionCountingOperationRun.setField(TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
            productionCountingOperationRun.setField(RUNS, runs);

            productionCountingOperationRun.getDataDefinition().save(productionCountingOperationRun);
        }
    }

    private void updateProductionCountingQuantity(final Entity order, final Entity operationProductInComponent,
            final Entity operationProductOutComponent, final Entity product, final BigDecimal plannedQuantity,
            final boolean isNonComponent) {
        Entity productionCountingQuantity = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).find()
                .add(SearchRestrictions.belongsTo(ORDER, order))
                .add(SearchRestrictions.belongsTo(OPERATION_PRODUCT_IN_COMPONENT, operationProductInComponent))
                .add(SearchRestrictions.belongsTo(OPERATION_PRODUCT_OUT_COMPONENT, operationProductOutComponent))
                .add(SearchRestrictions.belongsTo(PRODUCT, product)).setMaxResults(1).uniqueResult();

        if (productionCountingQuantity != null) {
            productionCountingQuantity.setField(ORDER, order);
            productionCountingQuantity.setField(OPERATION_PRODUCT_IN_COMPONENT, operationProductInComponent);
            productionCountingQuantity.setField(OPERATION_PRODUCT_OUT_COMPONENT, operationProductOutComponent);
            productionCountingQuantity.setField(PRODUCT, product);
            productionCountingQuantity.setField(PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
            productionCountingQuantity.setField(IS_NON_COMPONENT, isNonComponent);

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        }
    }

    public void createBasicProductionCountings(final Entity order) {
        final List<Entity> prodCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(ORDER, order)).list().getEntities();

        if (prodCountings == null || prodCountings.isEmpty()) {
            final List<Entity> productionCountingQuantities = order
                    .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.isNull(OPERATION_PRODUCT_OUT_COMPONENT)).list().getEntities();

            for (Entity productionCountingQuantity : productionCountingQuantities) {
                createBasicProductionCounting(order, productionCountingQuantity.getBelongsToField(PRODUCT),
                        productionCountingQuantity);
            }

        }
    }

    private Entity createBasicProductionCounting(final Entity order, final Entity product, final Entity productionCountingQuantity) {
        Entity basicProductionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();

        basicProductionCounting.setField(ORDER, order);
        basicProductionCounting.setField(PRODUCT, product);
        basicProductionCounting.setField(PRODUCED_QUANTITY, numberService.setScale(BigDecimal.ZERO));
        basicProductionCounting.setField(USED_QUANTITY, numberService.setScale(BigDecimal.ZERO));
        basicProductionCounting.setField(PRODUCTION_COUNTING_QUANTITY, productionCountingQuantity);

        basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);

        return basicProductionCounting;
    }

}
