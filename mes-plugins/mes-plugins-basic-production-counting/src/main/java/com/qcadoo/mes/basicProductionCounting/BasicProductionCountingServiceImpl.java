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

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCT;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.USED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields.RUNS;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.IS_NON_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.ROLE;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.TYPE_OF_MATERIAL;
import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.OPERATION_COMPONENT;

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
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class BasicProductionCountingServiceImpl implements BasicProductionCountingService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void createProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<Long> nonComponents = Sets.newHashSet();

        final Map<Long, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentWithQuantities(
                Arrays.asList(order), operationRuns, nonComponents);

        for (Entry<Long, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = productQuantitiesService.getTechnologyOperationComponent(operationRun.getKey());
            BigDecimal runs = operationRun.getValue();

            createProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }

        for (Entry<Long, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            Entity operationProductComponent = productQuantitiesService.getOperationProductComponent(productComponentQuantity
                    .getKey());
            BigDecimal plannedQuantity = productComponentQuantity.getValue();
            Entity technologyOperationComponent = operationProductComponent.getBelongsToField(OPERATION_COMPONENT);
            Entity product = operationProductComponent.getBelongsToField(PRODUCT);

            boolean isNonComponent = nonComponents.contains(operationProductComponent.getId());

            if (OPERATION_PRODUCT_IN_COMPONENT.equals(operationProductComponent.getDataDefinition().getName())) {
                createProductionCountingQuantity(order, technologyOperationComponent, operationProductComponent, null, product,
                        plannedQuantity, isNonComponent);
            } else if (OPERATION_PRODUCT_OUT_COMPONENT.equals(operationProductComponent.getDataDefinition().getName())) {
                createProductionCountingQuantity(order, technologyOperationComponent, null, operationProductComponent, product,
                        plannedQuantity, isNonComponent);
            }
        }

        createProductionCountingQuantity(order, getTechnologyOperationComponent(order), null, null,
                order.getBelongsToField(PRODUCT), order.getDecimalField(PLANNED_QUANTITY), false);
    }

    private Entity getTechnologyOperationComponent(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity technologyOperationComponent = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        return technologyOperationComponent;
    }

    @Override
    public Entity createProductionCountingOperationRun(final Entity order, final Entity technologyOperationComponent,
            final BigDecimal runs) {
        Entity productionCountingOperationRun = getProductionCountingOperationRunDD().create();

        productionCountingOperationRun.setField(ORDER, order);
        productionCountingOperationRun.setField(TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
        productionCountingOperationRun.setField(RUNS, numberService.setScale(runs));

        productionCountingOperationRun = productionCountingOperationRun.getDataDefinition().save(productionCountingOperationRun);

        return productionCountingOperationRun;
    }

    @Override
    public Entity createProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
            final Entity operationProductInComponent, final Entity operationProductOutComponent, final Entity product,
            final BigDecimal plannedQuantity, final boolean isNonComponent) {
        Entity productionCountingQuantity = getProductionCountingQuantityDD().create();

        productionCountingQuantity.setField(ORDER, order);
        productionCountingQuantity.setField(TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
        productionCountingQuantity.setField(OPERATION_PRODUCT_IN_COMPONENT, operationProductInComponent);
        productionCountingQuantity.setField(OPERATION_PRODUCT_OUT_COMPONENT, operationProductOutComponent);
        productionCountingQuantity.setField(PRODUCT, product);
        productionCountingQuantity.setField(ROLE, getRole(operationProductInComponent, operationProductOutComponent));
        productionCountingQuantity.setField(TYPE_OF_MATERIAL,
                getTypeOfMaterial(operationProductInComponent, operationProductOutComponent, isNonComponent));
        productionCountingQuantity.setField(IS_NON_COMPONENT, isNonComponent);
        productionCountingQuantity.setField(PLANNED_QUANTITY, numberService.setScale(plannedQuantity));

        productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

        return productionCountingQuantity;
    }

    private String getRole(final Entity operationProductInComponent, final Entity operationProductOutComponent) {
        if (operationProductInComponent != null) {
            return ProductionCountingQuantityRole.USED.getStringValue();
        } else if (operationProductOutComponent != null) {
            return ProductionCountingQuantityRole.PRODUCED.getStringValue();
        } else {
            return ProductionCountingQuantityRole.PRODUCED.getStringValue();
        }
    }

    private String getTypeOfMaterial(final Entity operationProductInComponent, final Entity operationProductOutComponent,
            boolean isNonComponent) {
        if (operationProductInComponent == null && operationProductOutComponent == null) {
            return ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue();
        } else {
            if (isNonComponent) {
                return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
            } else {
                return ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue();
            }
        }
    }

    public void updateProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<Long> nonComponents = Sets.newHashSet();

        final Map<Long, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentWithQuantities(
                Arrays.asList(order), operationRuns, nonComponents);

        for (Entry<Long, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = productQuantitiesService.getTechnologyOperationComponent(operationRun.getKey());
            BigDecimal runs = operationRun.getValue();

            updateProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }

        for (Entry<Long, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            Entity operationProductComponent = productQuantitiesService.getOperationProductComponent(productComponentQuantity
                    .getKey());
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
        Entity productionCountingOperationRun = getProductionCountingOperationRunDD().find()
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
        Entity productionCountingQuantity = getProductionCountingQuantityDD().find()
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
        final List<Entity> basicProductionCountings = getBasicProductionCountingDD().find()
                .add(SearchRestrictions.belongsTo(ORDER, order)).list().getEntities();

        if (basicProductionCountings == null || basicProductionCountings.isEmpty()) {
            final List<Entity> productionCountingQuantities = order
                    .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.isNull(OPERATION_PRODUCT_OUT_COMPONENT)).list().getEntities();

            Set<Long> alreadyAddedProducts = Sets.newHashSet();

            for (Entity productionCountingQuantity : productionCountingQuantities) {
                Entity product = productionCountingQuantity.getBelongsToField(PRODUCT);
                if (!alreadyAddedProducts.contains(product.getId())) {
                    createBasicProductionCounting(order, product);

                    alreadyAddedProducts.add(product.getId());
                }
            }
        }
    }

    @Override
    public Entity createBasicProductionCounting(final Entity order, final Entity product) {
        Entity basicProductionCounting = getBasicProductionCountingDD().create();

        basicProductionCounting.setField(ORDER, order);
        basicProductionCounting.setField(PRODUCT, product);
        basicProductionCounting.setField(PRODUCED_QUANTITY, numberService.setScale(BigDecimal.ZERO));
        basicProductionCounting.setField(USED_QUANTITY, numberService.setScale(BigDecimal.ZERO));

        basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);

        return basicProductionCounting;
    }

    @Override
    public void associateProductionCountingQuantitiesWithBasicProductionCountings(final Entity order) {
        final List<Entity> basicProductionCountings = order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS).find()
                .list().getEntities();

        for (Entity basicProductionCounting : basicProductionCountings) {
            Entity product = basicProductionCounting.getBelongsToField(PRODUCT);

            final List<Entity> productionCountingQuantities = order
                    .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.belongsTo(PRODUCT, product)).list().getEntities();

            basicProductionCounting.setField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES,
                    productionCountingQuantities);

            basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        }
    }

    @Override
    public Entity getBasicProductionCounting(final Long basicProductionCoutningId) {
        return getBasicProductionCountingDD().get(basicProductionCoutningId);
    }

    @Override
    public Entity getProductionCountingQuantity(final Long productionCountingQuantityId) {
        return getBasicProductionCountingDD().get(productionCountingQuantityId);
    }

    @Override
    public DataDefinition getBasicProductionCountingDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING);
    }

    @Override
    public DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

    private DataDefinition getProductionCountingOperationRunDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_OPERATON_RUN);
    }

    @Override
    public void fillUnitFields(final ViewDefinitionState view, final String productName, final List<String> referenceNames) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(productName);
        Entity product = productLookup.getEntity();

        String unit = "";

        if (product != null) {
            unit = product.getStringField(UNIT);
        }

        for (String referenceName : referenceNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(referenceName);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }
    }

}
