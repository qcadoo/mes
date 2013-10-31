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
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
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
        final Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        final OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentWithQuantities(Arrays.asList(order), operationRuns, nonComponents);

        createProductionCountingOperationRuns(order, operationRuns);
        createProductionCountingQuantities(order, productComponentQuantities, nonComponents);
    }

    private void createProductionCountingOperationRuns(final Entity order, final Map<Long, BigDecimal> operationRuns) {
        for (Entry<Long, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = productQuantitiesService.getTechnologyOperationComponent(operationRun.getKey());
            BigDecimal runs = operationRun.getValue();

            createProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }
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

    private void createProductionCountingQuantities(final Entity order,
            final OperationProductComponentWithQuantityContainer productComponentQuantities,
            final Set<OperationProductComponentHolder> nonComponents) {
        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();
            BigDecimal plannedQuantity = productComponentQuantity.getValue();

            Entity technologyOperationComponent = operationProductComponentHolder.getTechnologyOperationComponent();
            Entity product = operationProductComponentHolder.getProduct();

            String role = getRole(operationProductComponentHolder);

            boolean isNonComponent = nonComponents.contains(operationProductComponentHolder);

            createProductionCountingQuantity(order, technologyOperationComponent, product, role, isNonComponent, plannedQuantity);
        }
    }

    @Override
    public Entity createProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
            final Entity product, final String role, final boolean isNonComponent, final BigDecimal plannedQuantity) {
        Entity productionCountingQuantity = getProductionCountingQuantityDD().create();

        productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER, order);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, product);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.ROLE, role);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                getTypeOfMaterial(order, technologyOperationComponent, product, role, isNonComponent));
        productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY,
                numberService.setScale(plannedQuantity));

        productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

        return productionCountingQuantity;
    }

    private String getRole(final OperationProductComponentHolder operationProductComponentHolder) {
        if (operationProductComponentHolder.isEntityTypeSame(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)) {
            return ProductionCountingQuantityRole.USED.getStringValue();
        } else if (operationProductComponentHolder.isEntityTypeSame(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT)) {
            return ProductionCountingQuantityRole.PRODUCED.getStringValue();
        } else {
            return ProductionCountingQuantityRole.USED.getStringValue();
        }
    }

    private String getTypeOfMaterial(final Entity order, final Entity technologyOperationComponent, final Entity product,
            final String role, boolean isNonComponent) {
        if (isNonComponent) {
            return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
        } else {
            if (isRoleProduced(role)) {
                if (checkIfProductIsFinalProduct(order, technologyOperationComponent, product)) {
                    return ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue();
                } else {
                    if (checkIfProductAlreadyExists(technologyOperationComponent, product)) {
                        return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
                    } else {
                        return ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue();
                    }
                }
            } else {
                return ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue();
            }
        }
    }

    private boolean checkIfProductIsFinalProduct(final Entity order, final Entity technologyOperationComponent,
            final Entity product) {
        return (checkIfProductsAreSame(order, product) && checkIfTechnologyOperationComponentsAreSame(order,
                technologyOperationComponent));
    }

    private boolean checkIfProductsAreSame(final Entity order, final Entity product) {
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        if (orderProduct == null) {
            return false;
        } else {
            return product.getId().equals(orderProduct.getId());
        }
    }

    private boolean checkIfTechnologyOperationComponentsAreSame(final Entity order, final Entity technologyOperationComponent) {
        Entity orderTechnologyOperationComponent = getOrderTechnologyOperationComponent(order);

        if (orderTechnologyOperationComponent == null) {
            return false;
        } else {
            return technologyOperationComponent.getId().equals(orderTechnologyOperationComponent.getId());
        }
    }

    private boolean isRoleProduced(final String role) {
        return ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role);
    }

    private Entity getOrderTechnologyOperationComponent(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return null;
        } else {
            Entity technologyOperationComponent = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

            return technologyOperationComponent;
        }
    }

    private boolean checkIfProductAlreadyExists(final Entity technologyOperationComponent, final Entity product) {
        if (technologyOperationComponent == null) {
            return false;
        } else {
            Entity previousTechnologyOperationComponent = technologyOperationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.PARENT);

            if (previousTechnologyOperationComponent == null) {
                return false;
            } else {
                return checkIfProductExists(previousTechnologyOperationComponent, product);
            }
        }
    }

    private boolean checkIfProductExists(final Entity previousTechnologyOperationComponent, final Entity product) {
        return (previousTechnologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).list()
                .getTotalNumberOfEntities() == 1);
    }

    public void updateProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        final OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentWithQuantities(Arrays.asList(order), operationRuns, nonComponents);

        updateProductionCountingOperationRuns(order, operationRuns);
        updateProductionCountingQuantities(order, productComponentQuantities, nonComponents);
    }

    private void updateProductionCountingOperationRuns(final Entity order, final Map<Long, BigDecimal> operationRuns) {
        for (Entry<Long, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = productQuantitiesService.getTechnologyOperationComponent(operationRun.getKey());
            BigDecimal runs = operationRun.getValue();

            updateProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }
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

    private void updateProductionCountingQuantities(final Entity order,
            final OperationProductComponentWithQuantityContainer productComponentQuantities,
            final Set<OperationProductComponentHolder> nonComponents) {
        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();
            BigDecimal plannedQuantity = productComponentQuantity.getValue();

            Entity technologyOperationComponent = operationProductComponentHolder.getTechnologyOperationComponent();
            Entity product = operationProductComponentHolder.getProduct();

            String role = getRole(operationProductComponentHolder);

            boolean isNonComponent = nonComponents.contains(operationProductComponentHolder);

            updateProductionCountingQuantity(order, technologyOperationComponent, product, role, isNonComponent, plannedQuantity);
        }

        updateProductionCountingQuantity(order, getOrderTechnologyOperationComponent(order),
                order.getBelongsToField(OrderFields.PRODUCT), ProductionCountingQuantityRole.PRODUCED.getStringValue(), false,
                order.getDecimalField(OrderFields.PLANNED_QUANTITY));
    }

    private void updateProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
            final Entity product, final String role, final boolean isNonComponent, final BigDecimal plannedQuantity) {
        Entity productionCountingQuantity = getProductionCountingQuantityDD()
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, role)).setMaxResults(1).uniqueResult();

        if (productionCountingQuantity != null) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER, order);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, product);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.ROLE, role);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);
            productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY,
                    numberService.setScale(plannedQuantity));

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        }
    }

    public void createBasicProductionCountings(final Entity order) {
        final List<Entity> basicProductionCountings = getBasicProductionCountingDD().find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order)).list().getEntities();

        if (basicProductionCountings == null || basicProductionCountings.isEmpty()) {
            final List<Entity> productionCountingQuantities = order
                    .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                    .find()
                    .add(SearchRestrictions.or(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                            ProductionCountingQuantityRole.USED.getStringValue()), SearchRestrictions.and(SearchRestrictions.eq(
                            ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.PRODUCED.getStringValue()),
                            SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                                    ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue())))).list().getEntities();

            Set<Long> alreadyAddedProducts = Sets.newHashSet();

            for (Entity productionCountingQuantity : productionCountingQuantities) {
                Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

                if (!alreadyAddedProducts.contains(product.getId())) {
                    createBasicProductionCounting(order, product);

                    alreadyAddedProducts.add(product.getId());
                }
            }

            createBasicProductionCounting(order, order.getBelongsToField(OrderFields.PRODUCT));
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
    public void setTechnologyOperationComponentFieldRequired(final ViewDefinitionState view) {
        FieldComponent typeOfMaterialField = (FieldComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

        String typeOfMaterial = (String) typeOfMaterialField.getFieldValue();

        boolean isRequired = ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial);

        technologyOperationComponentLookup.setRequired(isRequired);
    }

    @Override
    public Set<String> fillRowStylesDependsOfTypeOfMaterial(final Entity productionCountingQuantity) {
        final Set<String> rowStyles = Sets.newHashSet();

        final String typeOfMaterial = productionCountingQuantity
                .getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

        if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add(RowStyle.GREEN_BACKGROUND);
        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add(RowStyle.BLUE_BACKGROUND);
        } else if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add(RowStyle.YELLOW_BACKGROUND);
        } else {
            rowStyles.add(RowStyle.BROWN_BACKGROUND);
        }

        return rowStyles;
    }

}
