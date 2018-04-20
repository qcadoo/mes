/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basicProductionCounting;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingOperationRunFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.RowStyle;

@Service
public class BasicProductionCountingServiceImpl implements BasicProductionCountingService {

    private static final String QUANTITIES_SUM_ALIAS = "sum";

    private static final String COMPONENTS_LOCATION = "componentsLocation";

    private static final String COMPONENTS_OUTPUT_LOCATION = "componentsOutputLocation";

    private static final String PRODUCTS_INPUT_LOCATION = "productsInputLocation";

    private static final String PRODUCTS_FLOW_LOCATION = "productsFlowLocation";

    private static final String PRODUCTION_FLOW = "productionFlow";

    private static final String inComponentHQL = "select opic from #technologies_operationProductInComponent opic "
            + "left join opic.operationComponent toc " + "left join toc.technology tech " + "where tech.id = :techId";

    private static final String outComponentHQL = "select opoc from #technologies_operationProductOutComponent opoc "
            + "left join opoc.operationComponent toc " + "left join toc.technology tech " + "where tech.id = :techId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductionCountingQuantitySetService productionCountingQuantitySetService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public void updateProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        final OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentWithQuantities(Arrays.asList(order), operationRuns, nonComponents);

        updateProductionCountingOperationRuns(order, operationRuns);
        updateProductionCountingQuantities(order, productComponentQuantities, nonComponents);
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
    public void createProductionCounting(final Entity order) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        final OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentWithQuantities(Arrays.asList(order), operationRuns, nonComponents);

        createProductionCountingOperationRuns(order, operationRuns);

        List<Entity> productionCountingQuantities = new ArrayList<>();

        prepareProductionCountingQuantities(order, nonComponents, productComponentQuantities, productionCountingQuantities);

        List<Entity> basicProductionCounting = Lists.newArrayList();

        prepareBasicProductionCounting(order, productionCountingQuantities, basicProductionCounting);

        saveBasicProductionCounting(productionCountingQuantities, basicProductionCounting);
    }

    private void saveBasicProductionCounting(final List<Entity> productionCountingQuantities,
            final List<Entity> basicProductionCounting) {
        Multimap<Long, Entity> productionCountingQuantitiesByProduct = ArrayListMultimap.create();

        for (Entity pCQ : productionCountingQuantities) {
            productionCountingQuantitiesByProduct.put(pCQ.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId(),
                    pCQ);
        }

        for (Entity bpc : basicProductionCounting) {
            Entity product = bpc.getBelongsToField(BasicProductionCountingFields.PRODUCT);

            Collection<Entity> pCountingQuantities = productionCountingQuantitiesByProduct.get(product.getId());

            bpc = bpc.getDataDefinition().save(bpc);

            for (Entity pcq : pCountingQuantities) {
                pcq.setField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING, bpc);
                pcq.getDataDefinition().save(pcq);
            }
        }
    }

    private void prepareBasicProductionCounting(final Entity order, final List<Entity> productionCountingQuantities,
            final List<Entity> basicProductionCounting) {
        List<Entity> forBasicProductionCounting = productionCountingQuantities.stream()
                .filter(pcq -> pcq.getStringField(ProductionCountingQuantityFields.ROLE)
                        .equals(ProductionCountingQuantityRole.USED.getStringValue())
                        || (pcq.getStringField(ProductionCountingQuantityFields.ROLE)
                                .equals(ProductionCountingQuantityRole.PRODUCED.getStringValue())
                                && pcq.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)
                                        .equals(ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue())))
                .collect(Collectors.toList());

        Set<Long> alreadyAddedProducts = Sets.newHashSet();

        for (Entity productionCountingQuantity : forBasicProductionCounting) {
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

            if (!alreadyAddedProducts.contains(product.getId())) {
                basicProductionCounting.add(prepareBasicProductionCounting(order, product));

                alreadyAddedProducts.add(product.getId());
            }
        }

        basicProductionCounting.add(prepareBasicProductionCounting(order, order.getBelongsToField(OrderFields.PRODUCT)));
    }

    private void prepareProductionCountingQuantities(final Entity order, final Set<OperationProductComponentHolder> nonComponents,
            final OperationProductComponentWithQuantityContainer productComponentQuantities,
            final List<Entity> productionCountingQuantities) {
        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();
            BigDecimal plannedQuantity = productComponentQuantity.getValue();

            Entity technologyOperationComponent = operationProductComponentHolder.getTechnologyOperationComponent();
            Entity product = operationProductComponentHolder.getProduct();

            String role = getRole(operationProductComponentHolder);

            boolean isNonComponent = nonComponents.contains(operationProductComponentHolder);

            Entity productionCountingQuantity = prepareProductionCountingQuantity(order, technologyOperationComponent, product,
                    role, isNonComponent, plannedQuantity);
            productionCountingQuantities.add(productionCountingQuantity);
        }

        if (PluginUtils.isEnabled("productFlowThruDivision")) {
            fillFlow(productionCountingQuantities, order);
        }

        if (parameterService.getParameter().getBooleanField(ParameterFieldsO.CREATE_SET_ELEMENTS_ON_ACCEPT)) {
            productionCountingQuantitySetService.markIntermediateInProductionCountingQuantities(productionCountingQuantities,
                    false);
        }
    }

    @Override
    public Entity createBasicProductionCounting(final Entity order, final Entity product) {
        Entity basicProductionCounting = getBasicProductionCountingDD().create();

        basicProductionCounting.setField(BasicProductionCountingFields.ORDER, order);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCT, product);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY,
                numberService.setScale(BigDecimal.ZERO));
        basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY, numberService.setScale(BigDecimal.ZERO));

        basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);

        return basicProductionCounting;
    }

    private void createProductionCountingOperationRuns(final Entity order, final Map<Long, BigDecimal> operationRuns) {
        for (Entry<Long, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = productQuantitiesService.getTechnologyOperationComponent(operationRun.getKey());
            BigDecimal runs = operationRun.getValue();

            createProductionCountingOperationRun(order, technologyOperationComponent, runs);
        }
    }

    private Entity prepareProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
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
        productionCountingQuantity.setField(ProductionCountingQuantityFields.FLOW_FILLED, Boolean.TRUE);

        return productionCountingQuantity;
    }

    private String getRole(final OperationProductComponentHolder operationProductComponentHolder) {
        if (operationProductComponentHolder.isEntityTypeSame(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)) {
            return ProductionCountingQuantityRole.USED.getStringValue();
        } else if (operationProductComponentHolder
                .isEntityTypeSame(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT)) {
            return ProductionCountingQuantityRole.PRODUCED.getStringValue();
        } else {
            return ProductionCountingQuantityRole.USED.getStringValue();
        }
    }

    private String getTypeOfMaterial(final Entity order, final Entity technologyOperationComponent, final Entity product,
            final String role, boolean isNonComponent) {
        if (isNonComponent) {
            return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
        } else if (isRoleProduced(role)) {
            if (checkIfProductIsFinalProduct(order, technologyOperationComponent, product)) {
                return ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue();
            } else if (checkIfProductAlreadyExists(technologyOperationComponent, product)) {
                return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
            } else {
                return ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue();
            }
        } else {
            return ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue();
        }
    }

    private boolean checkIfProductIsFinalProduct(final Entity order, final Entity technologyOperationComponent,
            final Entity product) {
        return (checkIfProductsAreSame(order, product)
                && checkIfTechnologyOperationComponentsAreSame(order, technologyOperationComponent));
    }

    private boolean checkIfProductsAreSame(final Entity order, final Entity product) {
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        return orderProduct != null && product.getId().equals(orderProduct.getId());
    }

    private boolean checkIfTechnologyOperationComponentsAreSame(final Entity order, final Entity technologyOperationComponent) {
        Entity orderTechnologyOperationComponent = getOrderTechnologyOperationComponent(order);

        return orderTechnologyOperationComponent != null
                && technologyOperationComponent.getId().equals(orderTechnologyOperationComponent.getId());
    }

    private boolean isRoleProduced(final String role) {
        return ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role);
    }

    private Entity getOrderTechnologyOperationComponent(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return null;
        } else {
            return technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();
        }
    }

    private boolean checkIfProductAlreadyExists(final Entity technologyOperationComponent, final Entity product) {
        if (technologyOperationComponent == null) {
            return false;
        } else {
            Entity previousTechnologyOperationComponent = technologyOperationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.PARENT);

            return previousTechnologyOperationComponent != null
                    && checkIfProductExists(previousTechnologyOperationComponent, product);
        }
    }

    private boolean checkIfProductExists(final Entity previousTechnologyOperationComponent, final Entity product) {
        return (previousTechnologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).list()
                .getTotalNumberOfEntities() == 1);
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
        Entity productionCountingOperationRun = getProductionCountingOperationRunDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingOperationRunFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent))
                .setMaxResults(1).uniqueResult();

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
        Entity productionCountingQuantity = getProductionCountingQuantityDD().find()
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

    private Entity prepareBasicProductionCounting(final Entity order, final Entity product) {
        Entity basicProductionCounting = getBasicProductionCountingDD().create();

        basicProductionCounting.setField(BasicProductionCountingFields.ORDER, order);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCT, product);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY,
                numberService.setScale(BigDecimal.ZERO));
        basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY, numberService.setScale(BigDecimal.ZERO));

        return basicProductionCounting;
    }

    @Override
    public void updateProducedQuantity(final Entity order) {
        Entity basicProductionCounting = getBasicProductionCountingDD().find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order)).add(SearchRestrictions
                        .belongsTo(BasicProductionCountingFields.PRODUCT, order.getBelongsToField(OrderFields.PRODUCT)))
                .setMaxResults(1).uniqueResult();
        if (Objects.nonNull(basicProductionCounting)) {
            basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY,
                    order.getDecimalField(OrderFields.DONE_QUANTITY));
            basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        }
    }

    @Override
    public List<Entity> getUsedMaterialsFromProductionCountingQuantities(final Entity order) {
        return getUsedMaterialsFromProductionCountingQuantities(order, false);
    }

    @Override
    public List<Entity> getUsedMaterialsFromProductionCountingQuantities(final Entity order, final boolean onlyComponents) {
        SearchCriteriaBuilder scb = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()));
        if (onlyComponents) {
            scb.add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                    ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()));
        }

        return scb.list().getEntities();
    }

    @Override
    public List<Entity> getMaterialsForOperationFromProductionCountingQuantities(final Entity order,
            final Entity operationComponent) {
        SearchCriteriaBuilder scb = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                        operationComponent));
        return scb.list().getEntities();
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm algorithm) {
        List<Entity> draftOrders = orders.stream()
                .filter(order -> OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE)))
                .collect(Collectors.toList());
        List<Entity> otherOrders = orders.stream()
                .filter(order -> !OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE)))
                .collect(Collectors.toList());
        Map<Long, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(draftOrders,
                algorithm, true);

        if (neededProductQuantities == null) {
            neededProductQuantities = Maps.newHashMap();
        }

        for (Entity order : otherOrders) {
            List<Entity> productionCountingQuantities = getUsedMaterialsFromProductionCountingQuantities(order);

            for (Entity pcq : productionCountingQuantities) {
                Long productId = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId();

                if (neededProductQuantities.containsKey(productId)) {
                    neededProductQuantities.put(productId, pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)
                            .add(neededProductQuantities.get(productId)));
                } else {
                    neededProductQuantities.put(productId,
                            pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
                }
            }
        }

        return neededProductQuantities;
    }

    @Override
    public Entity getBasicProductionCounting(final Long basicProductionCoutningId) {
        return getBasicProductionCountingDD().get(basicProductionCoutningId);
    }

    @Override
    public Entity getProductionCountingQuantity(final Long productionCountingQuantityId) {
        return getProductionCountingQuantityDD().get(productionCountingQuantityId);
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
    public BigDecimal getProducedQuantityFromBasicProductionCountings(final Entity order) {
        Entity entity = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING)
                .find().add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT,
                        order.getBelongsToField(OrderFields.PRODUCT)))
                .setProjection(list().add(alias(sum(BasicProductionCountingFields.PRODUCED_QUANTITY), QUANTITIES_SUM_ALIAS))
                        .add(rowCount()))
                .addOrder(SearchOrders.asc(QUANTITIES_SUM_ALIAS)).setMaxResults(1).uniqueResult();
        BigDecimal doneQuantity = BigDecimalUtils.convertNullToZero(entity.getDecimalField(QUANTITIES_SUM_ALIAS));

        return numberService.setScale(doneQuantity);
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

    private void fillFlow(final List<Entity> productionCountingQuantities, final Entity order) {
        List<Entity> opocs = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT)
                .find(outComponentHQL).setLong("techId", order.getBelongsToField(OrderFields.TECHNOLOGY).getId()).list()
                .getEntities();

        List<Entity> opics = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                .find(inComponentHQL).setLong("techId", order.getBelongsToField(OrderFields.TECHNOLOGY).getId()).list()
                .getEntities();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);
            String type = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

            if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)) {
                Entity opoc = getOperationProduct(opocs,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                if (opoc != null) {
                    productionCountingQuantity.setField(PRODUCTS_INPUT_LOCATION, opoc.getField(PRODUCTS_INPUT_LOCATION));

                }
            } else {
                if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {
                    Entity opic = getOperationProduct(opics,
                            productionCountingQuantity
                                    .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                            productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                    if (opic != null) {
                        if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                            productionCountingQuantity.setField(COMPONENTS_LOCATION, opic.getField(COMPONENTS_LOCATION));
                            productionCountingQuantity.setField(COMPONENTS_OUTPUT_LOCATION,
                                    opic.getField(COMPONENTS_OUTPUT_LOCATION));
                        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                            productionCountingQuantity.setField(PRODUCTION_FLOW, opic.getField(PRODUCTION_FLOW));
                            productionCountingQuantity.setField(PRODUCTS_FLOW_LOCATION, opic.getField(PRODUCTS_FLOW_LOCATION));
                        }
                    }
                } else if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
                    Entity opoc = getOperationProduct(opocs,
                            productionCountingQuantity
                                    .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                            productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                    if (opoc != null) {
                        productionCountingQuantity.setField(PRODUCTION_FLOW, opoc.getField(PRODUCTION_FLOW));
                        productionCountingQuantity.setField(PRODUCTS_FLOW_LOCATION, opoc.getField(PRODUCTS_FLOW_LOCATION));

                    }
                }
            }
        }

    }

    private Entity getOperationProduct(final List<Entity> entities, final Entity toc, final Entity product) {
        Optional<Entity> maybeOperationProduct = entities.stream().filter(
                e -> e.getBelongsToField("operationComponent").equals(toc) && e.getBelongsToField("product").equals(product))
                .findFirst();

        if (maybeOperationProduct.isPresent()) {
            return maybeOperationProduct.get();
        } else {
            return null;
        }
    }

}
