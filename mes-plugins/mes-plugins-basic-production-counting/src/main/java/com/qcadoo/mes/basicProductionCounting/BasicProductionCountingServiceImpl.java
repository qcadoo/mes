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

import com.google.common.collect.*;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.*;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.RowStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchProjections.*;

@Service
public class BasicProductionCountingServiceImpl implements BasicProductionCountingService {

    private static final String QUANTITIES_SUM_ALIAS = "sum";

    private static final String COMPONENTS_LOCATION = "componentsLocation";

    private static final String COMPONENTS_OUTPUT_LOCATION = "componentsOutputLocation";

    private static final String PRODUCTS_INPUT_LOCATION = "productsInputLocation";

    public static final String WASTE_RECEPTION_WAREHOUSE = "wasteReceptionWarehouse";

    private static final String PRODUCTS_FLOW_LOCATION = "productsFlowLocation";

    private static final String PRODUCTION_FLOW = "productionFlow";

    private static final String inComponentHQL = "select opic from #technologies_operationProductInComponent opic "
            + "left join opic.operationComponent toc left join toc.technology tech where tech.id = :techId";

    private static final String outComponentHQL = "select opoc from #technologies_operationProductOutComponent opoc "
            + "left join opoc.operationComponent toc left join toc.technology tech where tech.id = :techId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    public void updateProductionCountingQuantitiesAndOperationRuns(final Entity order) {
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        final Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        final OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentWithQuantities(Collections.singletonList(order), operationRuns, nonComponents);

        updateProductionCountingOperationRuns(order, operationRuns);
        updateProductionCountingQuantities(order, productComponentQuantities, nonComponents);
    }

    private Entity createProductionCountingOperationRun(final Entity technologyOperationComponent,
                                                        final BigDecimal runs) {
        Entity productionCountingOperationRun = getProductionCountingOperationRunDD().create();

        productionCountingOperationRun.setField(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        productionCountingOperationRun.setField(ProductionCountingOperationRunFields.RUNS,
                numberService.setScaleWithDefaultMathContext(runs));

        return productionCountingOperationRun;
    }

    @Override
    public OperationProductComponentWithQuantityContainer createProductionCounting(final Entity order) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        final OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentWithQuantities(Collections.singletonList(order), operationRuns, nonComponents);

        createProductionCountingOperationRuns(order, operationRuns);

        List<Entity> productionCountingQuantities = prepareProductionCountingQuantities(order, nonComponents, productComponentQuantities);

        List<Entity> basicProductionCounting = Lists.newArrayList();

        prepareBasicProductionCounting(order, productionCountingQuantities, basicProductionCounting);

        saveBasicProductionCounting(order, productionCountingQuantities, basicProductionCounting,
                order.getBelongsToField(OrderFields.PRODUCT));


        List<Entity> additionalFinalProducts = productionCountingQuantities.stream().filter(pcq -> pcq.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)
                        .equals(ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue())).map(pcq -> pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT))
                .collect(Collectors.toList());
        String additionalFinalProductsNumbers = additionalFinalProducts.stream()
                .map(p -> p.getStringField(ProductFields.NUMBER) + " - " + p.getStringField(ProductFields.NAME))
                .collect(Collectors.joining("\n"));

        order.setField(OrderFields.ADDITIONAL_FINAL_PRODUCTS, additionalFinalProductsNumbers);

        return productComponentQuantities;
    }

    private boolean saveBasicProductionCounting(Entity order, final List<Entity> productionCountingQuantities,
                                                final List<Entity> basicProductionCounting, final Entity orderProduct) {
        boolean productToProductGroupTechnologyDoesntExists = false;
        Multimap<Long, Entity> productionCountingQuantitiesByProduct = ArrayListMultimap.create();

        for (Entity pCQ : productionCountingQuantities) {
            productionCountingQuantitiesByProduct.put(pCQ.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId(),
                    pCQ);
        }

        for (Entity bpc : basicProductionCounting) {
            Entity product = bpc.getBelongsToField(BasicProductionCountingFields.PRODUCT);

            Collection<Entity> pCountingQuantities = productionCountingQuantitiesByProduct.get(product.getId());

            if (pCountingQuantities.isEmpty() && product.getId().equals(orderProduct.getId())) {
                pCountingQuantities = productionCountingQuantitiesByProduct.get(product.getBelongsToField(ProductFields.PARENT)
                        .getId());
            }

            bpc.setField(BasicProductionCountingFields.PRODUCT, product);

            for (Entity pcq : pCountingQuantities) {
                pcq.setField(ProductionCountingQuantityFields.IS_ORDER_CREATE, true);
                pcq.setField(ProductionCountingQuantityFields.PRODUCT, product);
            }
            bpc.setField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES, pCountingQuantities);
        }
        order.setField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS, basicProductionCounting);
        return true;
    }

    private void prepareBasicProductionCounting(final Entity order, final List<Entity> productionCountingQuantities,
                                                final List<Entity> basicProductionCounting) {
        List<Entity> forBasicProductionCounting = productionCountingQuantities
                .stream()
                .filter(pcq -> pcq.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                        ProductionCountingQuantityRole.USED.getStringValue())
                        || (pcq.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()) && (pcq.getStringField(
                        ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
                        ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue()) || pcq.getStringField(
                        ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
                        ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue())))).collect(Collectors.toList());

        Set<Long> alreadyAddedProducts = Sets.newHashSet();

        for (Entity productionCountingQuantity : forBasicProductionCounting) {
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

            if (!alreadyAddedProducts.contains(product.getId())) {
                basicProductionCounting.add(prepareBasicProductionCounting(product));

                alreadyAddedProducts.add(product.getId());
            }
        }

        basicProductionCounting.add(prepareBasicProductionCounting(order.getBelongsToField(OrderFields.PRODUCT)));
    }

    private List<Entity> prepareProductionCountingQuantities(final Entity order,
                                                             final Set<OperationProductComponentHolder> nonComponents,
                                                             final OperationProductComponentWithQuantityContainer productComponentQuantities
    ) {
        final List<Entity> productionCountingQuantities = new ArrayList<>();
        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();
            BigDecimal plannedQuantity = productComponentQuantity.getValue();
            Entity product = operationProductComponentHolder.getProduct();
            if (product == null) {
                continue;
            }

            Entity technologyOperationComponent = operationProductComponentHolder.getTechnologyOperationComponent();
            Entity operationProductComponent = operationProductComponentHolder.getOperationProductComponent();
            Entity technologyInputProductType = operationProductComponentHolder.getTechnologyInputProductType();
            Entity attribute = operationProductComponentHolder.getAttribute();

            String role = getRole(operationProductComponentHolder);

            boolean isNonComponent = nonComponents.contains(operationProductComponentHolder);
            boolean isWaste = operationProductComponentHolder.isWaste();

            Entity productionCountingQuantity = prepareProductionCountingQuantity(order, technologyOperationComponent, attribute,
                    technologyInputProductType, operationProductComponent, product, role, isNonComponent, plannedQuantity, isWaste);
            productionCountingQuantities.add(productionCountingQuantity);
        }

        if (PluginUtils.isEnabled("productFlowThruDivision")) {
            fillFlow(productionCountingQuantities, order);
        }
        return productionCountingQuantities;
    }

    @Override
    public Entity createBasicProductionCounting(final Entity order, final Entity product) {
        Entity basicProductionCounting = getBasicProductionCountingDD().create();

        basicProductionCounting.setField(BasicProductionCountingFields.ORDER, order);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCT, product);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));
        basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));

        basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);

        return basicProductionCounting;
    }

    private void createProductionCountingOperationRuns(final Entity order, final Map<Long, BigDecimal> operationRuns) {
        List<Entity> productionCountingOperationRuns = Lists.newArrayList();
        for (Entry<Long, BigDecimal> operationRun : operationRuns.entrySet()) {
            Entity technologyOperationComponent = productQuantitiesService.getTechnologyOperationComponent(operationRun.getKey());
            BigDecimal runs = operationRun.getValue();

            productionCountingOperationRuns.add(createProductionCountingOperationRun(technologyOperationComponent, runs));
        }
        order.setField(OrderFieldsBPC.PRODUCTION_COUNTING_OPERATION_RUNS, productionCountingOperationRuns);
    }

    private Entity prepareProductionCountingQuantity(final Entity order, final Entity technologyOperationComponent,
                                                     final Entity attribute,
                                                     final Entity technologyInputProductType,
                                                     final Entity operationProductComponent, Entity product,
                                                     final String role,
                                                     final boolean isNonComponent, final BigDecimal plannedQuantity,
                                                     final boolean isWaste) {
        Entity productionCountingQuantity = getProductionCountingQuantityDD().create();

        productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_INPUT_PRODUCT_TYPE,
                technologyInputProductType);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, product);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.ATTRIBUTE, attribute);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.ROLE, role);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                getTypeOfMaterial(order, technologyOperationComponent, product, role, isNonComponent, isWaste));
        productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(plannedQuantity));
        productionCountingQuantity.setField(ProductionCountingQuantityFields.FLOW_FILLED, Boolean.TRUE);

        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role) && operationProductComponent.getBooleanField("showMaterialComponent")) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.SHOW_MATERIAL_COMPONENT_IN_WORK_PLAN,
                    operationProductComponent.getBooleanField("showMaterialComponent"));
        }

        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {
            List<Entity> sections = new ArrayList<>();
            DataDefinition dataDefinition = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_SECTION);
            for (Entity topicSection : operationProductComponent.getHasManyField(OperationProductInComponentFields.SECTIONS)) {
                Entity pcqSection = dataDefinition.create();
                pcqSection.setField(SectionFieldsBPC.QUANTITY, topicSection.getIntegerField(SectionFields.QUANTITY));
                pcqSection.setField(SectionFieldsBPC.LENGTH, topicSection.getField(SectionFields.LENGTH));
                pcqSection.setField(SectionFieldsBPC.UNIT, topicSection.getField(SectionFields.UNIT));
                sections.add(pcqSection);
            }
            productionCountingQuantity.setField(ProductionCountingQuantityFields.SECTIONS, sections);
        }
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

    private String getTypeOfMaterial(final Entity order, final Entity technologyOperationComponent,
                                     final Entity product,
                                     final String role, boolean isNonComponent, boolean isWaste) {
        if (isNonComponent) {
            return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
        } else if (isRoleProduced(role)) {
            if (checkIfProductIsFinalProduct(order, technologyOperationComponent, product)) {
                return ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue();
            } else if (checkIfProductAlreadyExists(technologyOperationComponent, product)) {
                return ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue();
            } else if (isWaste) {
                return ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue();
            } else {
                return ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue();
            }
        } else {
            return ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue();
        }
    }

    private boolean checkIfProductIsFinalProduct(final Entity order, final Entity technologyOperationComponent,
                                                 final Entity product) {
        return (checkIfProductsAreSame(order, product) && checkIfTechnologyOperationComponentsAreSame(order,
                technologyOperationComponent));
    }

    private boolean checkIfProductsAreSame(final Entity order, Entity product) {
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))) {
            return true;
        }
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        return orderProduct != null && product.getId().equals(orderProduct.getId());
    }

    private boolean checkIfTechnologyOperationComponentsAreSame(final Entity order,
                                                                final Entity technologyOperationComponent) {
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
                                                  final Entity product, final String role, final boolean isNonComponent,
                                                  final BigDecimal plannedQuantity) {
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
                    numberService.setScaleWithDefaultMathContext(plannedQuantity));

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        }
    }

    private Entity prepareBasicProductionCounting(final Entity product) {
        Entity basicProductionCounting = getBasicProductionCountingDD().create();

        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCT, product);
        basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));
        basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(BigDecimal.ZERO));

        return basicProductionCounting;
    }

    @Override
    public void updateProducedQuantity(final Entity order) {
        Entity basicProductionCounting = getBasicProductionCountingDD()
                .find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT,
                        order.getBelongsToField(OrderFields.PRODUCT))).setMaxResults(1).uniqueResult();
        if (Objects.nonNull(basicProductionCounting)) {
            basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY,
                    order.getDecimalField(OrderFields.DONE_QUANTITY));
            basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        }
    }

    @Override
    public List<Entity> getAdditionalFinalProducts(final Entity order) {
        SearchCriteriaBuilder scb = getProductionCountingQuantityDD()
                .find()
                .createAlias(ProductionCountingQuantityFields.ORDER, "o", JoinType.LEFT)
                .add(SearchRestrictions.eq("o.id", order.getId()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL, ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue()));

        return scb.list().getEntities().stream().map(pcq -> pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)).collect(Collectors.toList());

    }

    @Override
    public List<Entity> getUsedMaterialsFromProductionCountingQuantities(final Entity order) {
        return getUsedMaterialsFromProductionCountingQuantities(order, false);
    }

    @Override
    public List<Entity> getUsedMaterialsFromProductionCountingQuantities(final Entity order,
                                                                         final boolean onlyComponents) {
        SearchCriteriaBuilder scb = getProductionCountingQuantityDD()
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()));
        if (onlyComponents) {
            scb.add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                    ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()));
        }

        return scb.list().getEntities();
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm algorithm) {
        Map<Long, BigDecimal> neededProductQuantities = Maps.newHashMap();

        for (Entity order : orders) {
            List<Entity> productionCountingQuantities;
            if (algorithm.getStringValue().equals(MrpAlgorithm.ONLY_MATERIALS.getStringValue())) {
                productionCountingQuantities = getUsedMaterialsFromProductionCountingQuantities(order, true);
            } else {
                productionCountingQuantities = getUsedMaterialsFromProductionCountingQuantities(order, false);
            }

            for (Entity pcq : productionCountingQuantities) {
                Long productId = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId();

                if (neededProductQuantities.containsKey(productId)) {
                    neededProductQuantities.put(productId, pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)
                            .add(neededProductQuantities.get(productId)));
                } else {
                    neededProductQuantities
                            .put(productId, pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
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
                .find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT,
                        order.getBelongsToField(OrderFields.PRODUCT)))
                .setProjection(
                        list().add(alias(sum(BasicProductionCountingFields.PRODUCED_QUANTITY), QUANTITIES_SUM_ALIAS)).add(
                                rowCount())).addOrder(SearchOrders.asc(QUANTITIES_SUM_ALIAS)).setMaxResults(1).uniqueResult();
        BigDecimal doneQuantity = BigDecimalUtils.convertNullToZero(entity.getDecimalField(QUANTITIES_SUM_ALIAS));

        return numberService.setScaleWithDefaultMathContext(doneQuantity);
    }

    @Override
    public void fillUnitFields(final ViewDefinitionState view, final String productName,
                               final List<String> referenceNames) {
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
        } else if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)
                || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)) {
            rowStyles.add(RowStyle.YELLOW_BACKGROUND);
        } else {
            rowStyles.add(RowStyle.BROWN_BACKGROUND);
        }

        return rowStyles;
    }

    @Override
    public void fillFlow(final List<Entity> productionCountingQuantities, final Entity order) {
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

            if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)
                    || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type)) {
                Entity opoc = getOperationProduct(opocs,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                if (opoc != null) {
                    productionCountingQuantity.setField(PRODUCTS_INPUT_LOCATION, opoc.getField(PRODUCTS_INPUT_LOCATION));
                }
            } else if (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type)) {
                Entity opoc = getOperationProduct(opocs,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                if (opoc != null) {
                    productionCountingQuantity.setField(WASTE_RECEPTION_WAREHOUSE, opoc.getField(WASTE_RECEPTION_WAREHOUSE));
                }
            } else if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {
                Entity opic = getOperationProduct(opics,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));

                if (Objects.isNull(opic)) {
                    Entity parent = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT)
                            .getBelongsToField(ProductFields.PARENT);
                    if (Objects.nonNull(parent)) {
                        opic = getOperationProduct(opics,
                                productionCountingQuantity
                                        .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                                parent);
                    }
                }

                if (opic != null) {
                    if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                        productionCountingQuantity.setField(COMPONENTS_LOCATION, opic.getField(COMPONENTS_LOCATION));
                        productionCountingQuantity
                                .setField(COMPONENTS_OUTPUT_LOCATION, opic.getField(COMPONENTS_OUTPUT_LOCATION));
                    } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                        if ("02cumulated".equals(order.getStringField("typeOfProductionRecording"))) {
                            productionCountingQuantity.setField(PRODUCTION_FLOW, "02withinTheProcess");
                        } else {
                            productionCountingQuantity.setField(PRODUCTION_FLOW, opic.getField(PRODUCTION_FLOW));
                            productionCountingQuantity.setField(PRODUCTS_FLOW_LOCATION, opic.getField(PRODUCTS_FLOW_LOCATION));
                        }
                    }
                }
            } else if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
                Entity opoc = getOperationProduct(opocs,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                if (opoc != null) {
                    if ("02cumulated".equals(order.getStringField("typeOfProductionRecording"))) {
                        productionCountingQuantity.setField(PRODUCTION_FLOW, "02withinTheProcess");
                    } else {
                        productionCountingQuantity.setField(PRODUCTION_FLOW, opoc.getField(PRODUCTION_FLOW));
                        productionCountingQuantity.setField(PRODUCTS_FLOW_LOCATION, opoc.getField(PRODUCTS_FLOW_LOCATION));
                    }
                }
            }
        }
    }

    private Entity getOperationProduct(final List<Entity> entities, final Entity toc, final Entity product) {

        Optional<Entity> maybeOperationProduct = entities
                .stream()
                .filter(e -> e.getBelongsToField("operationComponent").equals(toc)
                        && Objects.nonNull(e.getBelongsToField("product")) && e.getBelongsToField("product").equals(product))
                .findFirst();

        if (!maybeOperationProduct.isPresent()) {
            List<Entity> forGroupSize = entities
                    .stream()
                    .filter(e -> e.getBelongsToField("operationComponent").equals(toc)
                            && e.getBooleanField("differentProductsInDifferentSizes")).collect(Collectors.toList());
            for (Entity entity : forGroupSize) {
                Optional<Entity> maybeOperationProductForSizeGroup = entity
                        .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS).stream()
                        .filter(pG -> pG.getBelongsToField(ProductBySizeGroupFields.PRODUCT).getId().equals(product.getId()))
                        .findFirst();
                if (maybeOperationProductForSizeGroup.isPresent()) {
                    return entity;
                }
            }
        }

        return maybeOperationProduct.orElse(null);
    }

    @Override
    public BigDecimal getOperationComponentRuns(Entity order, Entity toc) {
        return getProductionCountingOperationRunDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingOperationRunFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingOperationRunFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .uniqueResult().getDecimalField(ProductionCountingOperationRunFields.RUNS);
    }

    @Override
    public BigDecimal getProductPlannedQuantity(Entity order, Entity toc, Entity product) {
        return getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .uniqueResult().getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
    }

}
