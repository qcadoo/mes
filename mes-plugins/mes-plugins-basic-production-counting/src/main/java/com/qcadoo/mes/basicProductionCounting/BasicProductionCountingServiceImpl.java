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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.RowStyle;

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
            Entity technologyOperationComponent = operationProductComponent
                    .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);
            Entity product = operationProductComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

            boolean isNonComponent = nonComponents.contains(operationProductComponent.getId());

            if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(operationProductComponent.getDataDefinition()
                    .getName())) {
                createProductionCountingQuantity(order, technologyOperationComponent, operationProductComponent, null, product,
                        plannedQuantity, isNonComponent);
            } else if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT.equals(operationProductComponent
                    .getDataDefinition().getName())) {
                createProductionCountingQuantity(order, technologyOperationComponent, null, operationProductComponent, product,
                        plannedQuantity, isNonComponent);
            }
        }

        createProductionCountingQuantity(order, getTechnologyOperationComponent(order), null, null,
                order.getBelongsToField(OrderFields.PRODUCT), order.getDecimalField(OrderFields.PLANNED_QUANTITY), false);
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

        productionCountingOperationRun.setField(ProductionCountingOperationRunFields.ORDER, order);
        productionCountingOperationRun.setField(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        productionCountingOperationRun.setField(ProductionCountingOperationRunFields.RUNS, numberService.setScale(runs));

        productionCountingOperationRun = productionCountingOperationRun.getDataDefinition().save(productionCountingOperationRun);

        return productionCountingOperationRun;
    }

    @Override
    public Entity createProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
            final Entity operationProductInComponent, final Entity operationProductOutComponent, final Entity product,
            final BigDecimal plannedQuantity, final boolean isNonComponent) {
        Entity productionCountingQuantity = getProductionCountingQuantityDD().create();

        productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER, order);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT,
                operationProductInComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT,
                operationProductOutComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, product);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.ROLE,
                getRole(operationProductInComponent, operationProductOutComponent));
        productionCountingQuantity.setField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                getTypeOfMaterial(operationProductInComponent, operationProductOutComponent, isNonComponent));
        productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY,
                numberService.setScale(plannedQuantity));

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
            Entity product = operationProductComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

            boolean isNonComponent = nonComponents.contains(operationProductComponent);

            if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(operationProductComponent.getDataDefinition()
                    .getName())) {
                updateProductionCountingQuantity(order, operationProductComponent, null, product, plannedQuantity, isNonComponent);
            } else if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT.equals(operationProductComponent
                    .getDataDefinition().getName())) {
                updateProductionCountingQuantity(order, null, operationProductComponent, product, plannedQuantity, isNonComponent);
            }
        }

        updateProductionCountingQuantity(order, null, null, order.getBelongsToField(OrderFields.PRODUCT),
                order.getDecimalField(OrderFields.PLANNED_QUANTITY), false);
    }

    private void updateProductionCountingOperationRun(final Entity order, final Entity technologyOperationComponent,
            final BigDecimal runs) {
        Entity productionCountingOperationRun = getProductionCountingOperationRunDD()
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingOperationRunFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent)).setMaxResults(1).uniqueResult();

        if (productionCountingOperationRun != null) {
            productionCountingOperationRun.setField(ProductionCountingOperationRunFields.ORDER, order);
            productionCountingOperationRun.setField(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent);
            productionCountingOperationRun.setField(ProductionCountingOperationRunFields.RUNS, runs);

            productionCountingOperationRun.getDataDefinition().save(productionCountingOperationRun);
        }
    }

    private void updateProductionCountingQuantity(final Entity order, final Entity operationProductInComponent,
            final Entity operationProductOutComponent, final Entity product, final BigDecimal plannedQuantity,
            final boolean isNonComponent) {
        Entity productionCountingQuantity = getProductionCountingQuantityDD()
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT,
                        operationProductInComponent))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT,
                        operationProductOutComponent))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (productionCountingQuantity != null) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER, order);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT,
                    operationProductInComponent);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT,
                    operationProductOutComponent);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, product);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY,
                    numberService.setScale(plannedQuantity));
            productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        }
    }

    public void createBasicProductionCountings(final Entity order) {
        final List<Entity> basicProductionCountings = getBasicProductionCountingDD().find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order)).list().getEntities();

        if (basicProductionCountings == null || basicProductionCountings.isEmpty()) {
            final List<Entity> productionCountingQuantities = order
                    .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.isNull(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT)).list()
                    .getEntities();

            Set<Long> alreadyAddedProducts = Sets.newHashSet();

            for (Entity productionCountingQuantity : productionCountingQuantities) {
                Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

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

        basicProductionCounting.setField(BasicProductionCountingFields.ORDER, order);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCT, product);
        basicProductionCounting
                .setField(BasicProductionCountingFields.PRODUCED_QUANTITY, numberService.setScale(BigDecimal.ZERO));
        basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY, numberService.setScale(BigDecimal.ZERO));

        basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);

        return basicProductionCounting;
    }

    @Override
    public void associateProductionCountingQuantitiesWithBasicProductionCountings(final Entity order) {
        final List<Entity> basicProductionCountings = order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS).find()
                .list().getEntities();

        for (Entity basicProductionCounting : basicProductionCountings) {
            Entity product = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT);

            final List<Entity> productionCountingQuantities = order
                    .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT, product)).list().getEntities();

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
            unit = product.getStringField(ProductFields.UNIT);
        }

        for (String referenceName : referenceNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(referenceName);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }
    }

    @Override
    public Set<String> fillRowStylesDependsOfTypeOfMaterial(final Entity productionCountingQuantity) {
        final Set<String> rowStyles = Sets.newHashSet();

        final String typeOfMaterial = productionCountingQuantity
                .getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

        if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add("lightGreyBg");
        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add("greyBg");
        } else if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add("darkGreyBg");
        } else {
            rowStyles.add(RowStyle.RED_BACKGROUND);
        }

        return rowStyles;
    }

}
