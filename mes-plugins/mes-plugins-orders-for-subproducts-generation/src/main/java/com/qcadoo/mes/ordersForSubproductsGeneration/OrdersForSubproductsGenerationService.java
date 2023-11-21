/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.ordersForSubproductsGeneration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.MaterialRequirementCoverageForOrderService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductState;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.MaterialRequirementCoverageForOrderConstans;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orderSupplies.constants.ProductType;
import com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageHelper;
import com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.CoverageForOrderFieldsOFSPG;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrderFieldsOFSPG;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductStructureTreeNodeFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchDisjunction;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrdersForSubproductsGenerationService {

    protected static final Logger LOG = LoggerFactory.getLogger(OrdersForSubproductsGenerationService.class);

    private static final String COUNT_ALIAS = "count";

    private static final List<String> L_ORDER_FIELD_NAMES = Lists.newArrayList(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
            OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
            OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, OrderFieldsPC.REGISTER_PRODUCTION_TIME);

    private static final String L_TRANSFER_ORDERS_GROUP_TO_ORDERS_FOR_COMPONENTS = "transferOrdersGroupToOrdersForComponents";

    private static final String L_ORDERS_GROUP = "ordersGroup";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private MaterialRequirementCoverageForOrderService materialRequirementCoverageForOrderService;

    @Autowired
    private MaterialRequirementCoverageService materialRequirementCoverageService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private MaterialRequirementCoverageHelper materialRequirementCoverageHelper;

    @Autowired
    private ParameterService parameterService;

    private static final Integer START_LEVEL = 1;

    private static final String L_COMPONENT = "component";

    public List<Entity> getProductNodesWithCheckedTechnologies(final ViewDefinitionState view, final Entity order) {
        EntityTree tree = productStructureTreeService.generateProductStructureTree(view,
                order.getBelongsToField(OrderFields.TECHNOLOGY));
        return tree.stream()
                .filter(node -> node.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE).equals(L_COMPONENT)
                        && (Objects.nonNull(node.getBelongsToField(ProductStructureTreeNodeFields.TECHNOLOGY))
                        && node.getBelongsToField(ProductStructureTreeNodeFields.TECHNOLOGY)
                        .getStringField(TechnologyFields.STATE).equals(TechnologyState.CHECKED.getStringValue())))
                .collect(Collectors.toList());
    }

    public Entity getMainTocForOrder(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                .add(SearchRestrictions.isNull(TechnologyOperationComponentFields.PARENT)).setMaxResults(1).uniqueResult();
    }

    public List<Entity> getCoveragProductsForTOC(final Entity toc, final Entity materialRequirementCoverage) {
        SearchCriteriaBuilder scb = dataDefinitionService.get(MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER,
                MaterialRequirementCoverageForOrderConstans.MODEL_COVERAGE_PRODUCT).find();

        scb.add(SearchRestrictions.belongsTo(CoverageProductFields.COVERAGE_FOR_ORDER,
                MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER,
                MaterialRequirementCoverageForOrderConstans.MODEL_COVERAGE_FOR_ORDER, materialRequirementCoverage.getId()));
        scb.add(SearchRestrictions.eq(CoverageProductFields.STATE, CoverageProductState.LACK.getStringValue()));
        scb.add(SearchRestrictions.eq(CoverageProductFields.PRODUCT_TYPE, ProductType.INTERMEDIATE.getStringValue()));

        SearchDisjunction sd = SearchRestrictions.disjunction();

        for (Entity product : getInputProducts(toc)) {
            sd.add(SearchRestrictions.belongsTo(CoverageProductFields.PRODUCT, product));
        }

        scb.add(sd);

        return scb.list().getEntities();

    }

    public List<Entity> getInputProducts(final Entity toc) {
        List<Entity> products = Lists.newArrayList();

        for (Entity opic : toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
            products.add(opic.getBelongsToField(OperationProductInComponentFields.PRODUCT));
        }

        return products;
    }

    @Transactional
    public void generateSimpleOrderForSubProduct(final Entity entry, final Entity parentOrder, final Locale locale,
                                                 final int index) {

        boolean transferOrdersGroupToOrdersForComponents = parameterService.getParameter().getBooleanField(L_TRANSFER_ORDERS_GROUP_TO_ORDERS_FOR_COMPONENTS);
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).create();
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                .get(Long.valueOf(entry.getIntegerField("productId")));

        LOG.info(String.format("Start generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));

        order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, entry.getDecimalField("plannedQuantity"));
        order.setField(OrderFields.PLANNED_QUANTITY, entry.getDecimalField("plannedQuantity"));

        Entity technology = technologyServiceO.getDefaultTechnology(product);
        order.setField(OrderFieldsOFSPG.PARENT, parentOrder);

        if (Objects.isNull(parentOrder.getBelongsToField(OrderFieldsOFSPG.PARENT))) {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder);
            order.setField(OrderFieldsOFSPG.LEVEL, START_LEVEL);
        } else {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder.getBelongsToField(OrderFieldsOFSPG.ROOT));
            order.setField(OrderFieldsOFSPG.LEVEL, parentOrder.getIntegerField(OrderFieldsOFSPG.LEVEL) + 1);
        }

        order.setField(OrderFields.NUMBER, generatePostfixForNumber(parentOrder, order, index));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, locale));
        order.setField(OrderFields.PRODUCT, product);

        order.setField(OrderFields.TECHNOLOGY, technology);
        getProductionLine(order, technology);
        getDivision(parentOrder, order, technology);
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                parentOrder.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
        order.setField(OrderFields.DATE_FROM, parentOrder.getDateField(OrderFields.START_DATE));
        order.setField(OrderFields.DATE_TO, parentOrder.getDateField(OrderFields.FINISH_DATE));
        order.setField(OrderFields.COMPANY, parentOrder.getBelongsToField(OrderFields.COMPANY));
        order.setField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS,
                parentOrder.getBooleanField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS));
        setOrderWithDefaultProductionCountingValues(order, technology);
        order.setField(OrderFields.DESCRIPTION,
                buildDescription(parentOrder.getStringField(OrderFields.DESCRIPTION), technology, product));

        if (transferOrdersGroupToOrdersForComponents && Objects.nonNull(parentOrder.getBelongsToField(L_ORDERS_GROUP))) {
            Entity orderGroup = parentOrder.getBelongsToField(L_ORDERS_GROUP);
            order.setField(L_ORDERS_GROUP, orderGroup);
            order.setField(OrderFields.DATE_FROM, orderGroup.getDateField("startDate"));
            order.setField(OrderFields.DATE_TO, orderGroup.getDateField("finishDate"));
        }

        order = order.getDataDefinition().save(order);

        LOG.info(String.format("Finish generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));
    }

    @Transactional
    public void generateOrderForSubProduct(final Entity coverageProduct, final Entity parentOrder, final Locale locale,
                                           final int index) {
        boolean transferOrdersGroupToOrdersForComponents = parameterService.getParameter().getBooleanField(L_TRANSFER_ORDERS_GROUP_TO_ORDERS_FOR_COMPONENTS);

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).create();

        Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

        LOG.info(String.format("Start generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));

        Entity productLog = findForOrder(parentOrder, coverageProduct);

        BigDecimal planedQuantity = productLog.getDecimalField(CoverageProductLoggingFields.CHANGES);
        BigDecimal missing = productLog.getDecimalField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY)
                .abs(numberService.getMathContext());

        if (missing.compareTo(planedQuantity) > 0) {
            order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, planedQuantity);
            order.setField(OrderFields.PLANNED_QUANTITY, planedQuantity);
        } else {
            order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, missing);
            order.setField(OrderFields.PLANNED_QUANTITY, missing);
        }

        Entity technology = technologyServiceO.getDefaultTechnology(product);
        order.setField(OrderFieldsOFSPG.PARENT, parentOrder);

        if (Objects.isNull(parentOrder.getBelongsToField(OrderFieldsOFSPG.PARENT))) {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder);
            order.setField(OrderFieldsOFSPG.LEVEL, START_LEVEL);
        } else {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder.getBelongsToField(OrderFieldsOFSPG.ROOT));
            order.setField(OrderFieldsOFSPG.LEVEL, parentOrder.getIntegerField(OrderFieldsOFSPG.LEVEL) + 1);
        }

        order.setField(OrderFields.NUMBER, generatePostfixForNumber(parentOrder, order, index));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, locale));
        order.setField(OrderFields.PRODUCT, product);

        order.setField(OrderFields.TECHNOLOGY, technology);
        getProductionLine( order, technology);
        getDivision(parentOrder, order, technology);
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                parentOrder.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
        order.setField(OrderFields.DATE_FROM, parentOrder.getDateField(OrderFields.START_DATE));
        order.setField(OrderFields.DATE_TO, parentOrder.getDateField(OrderFields.FINISH_DATE));
        order.setField(OrderFields.COMPANY, parentOrder.getBelongsToField(OrderFields.COMPANY));
        order.setField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS,
                parentOrder.getBooleanField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS));
        setOrderWithDefaultProductionCountingValues(order, technology);
        order.setField(OrderFields.DESCRIPTION,
                buildDescription(parentOrder.getStringField(OrderFields.DESCRIPTION), technology, product));

        if (transferOrdersGroupToOrdersForComponents && Objects.nonNull(parentOrder.getBelongsToField(L_ORDERS_GROUP))) {
            Entity orderGroup = parentOrder.getBelongsToField(L_ORDERS_GROUP);
            order.setField(L_ORDERS_GROUP, orderGroup);
            order.setField(OrderFields.DATE_FROM, orderGroup.getDateField("startDate"));
            order.setField(OrderFields.DATE_TO, orderGroup.getDateField("finishDate"));
        }

        order = order.getDataDefinition().save(order);

        LOG.info(String.format("Finish generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));
    }

    private String buildDescription(String parentOrderDescription, Entity technology, Entity product) {
        Entity parameter = parameterService.getParameter();
        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        boolean fillOrderDescriptionBasedOnProductDescription = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_PRODUCT_DESCRIPTION);

        if (fillOrderDescriptionBasedOnTechnology || fillOrderDescriptionBasedOnProductDescription) {
            StringBuilder descriptionBuilder = new StringBuilder();

            if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                    && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
                descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));
            }

            buildProductDescription(product, fillOrderDescriptionBasedOnProductDescription, descriptionBuilder);

            return descriptionBuilder.toString();
        } else {
            return parentOrderDescription;
        }
    }

    private void buildProductDescription(Entity product, boolean fillOrderDescriptionBasedOnProductDescription,
                                         StringBuilder descriptionBuilder) {
        if (fillOrderDescriptionBasedOnProductDescription && Objects.nonNull(product)) {
            String productDescription = product.getStringField(ProductFields.DESCRIPTION);
            if (StringUtils.isNoneBlank(productDescription)) {
                if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                    descriptionBuilder.append("\n");
                }
                descriptionBuilder.append(productDescription);
            }
        }
    }

    private void getProductionLine(final Entity order, final Entity technology) {
        Entity productionLine = orderService.getProductionLine(technology);

        order.setField(OrderFields.PRODUCTION_LINE, productionLine);
    }

    private void getDivision(final Entity parentOrder, final Entity order, final Entity technology) {
        Entity division = technology.getBelongsToField(TechnologyFieldsPFTD.DIVISION);

        if (Objects.nonNull(division)) {
            order.setField(OrderFields.DIVISION, division);
        } else {
            Entity productionLine = parentOrder.getBelongsToField(OrderFields.PRODUCTION_LINE);
            if (Objects.nonNull(productionLine)) {
                List<Entity> divisions = productionLine.getManyToManyField(ProductionLineFields.DIVISIONS);
                if (divisions.size() == 1) {
                    order.setField(OrderFields.DIVISION, division);
                }
            }
        }
    }

    private Entity findForOrder(final Entity parentOrder, final Entity coverageProduct) {
        return coverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS).stream()
                .filter(e -> Objects.nonNull(e.getBelongsToField(CoverageProductLoggingFields.ORDER))).filter(entity -> entity
                        .getBelongsToField(CoverageProductLoggingFields.ORDER).getId().equals(parentOrder.getId()))
                .findFirst().get();
    }

    private void setOrderWithDefaultProductionCountingValues(final Entity order, final Entity technology) {
        for (String fieldName : L_ORDER_FIELD_NAMES) {
            order.setField(fieldName, technology.getField(fieldName));
        }
    }

    private String generatePostfixForNumber(final Entity parentOrder, final Entity order, final int index) {
        String postfix;

        if (Objects.isNull(parentOrder.getBelongsToField(OrderFieldsOFSPG.PARENT))) {
            postfix = parentOrder.getStringField(OrderFields.NUMBER) + "-"
                    + order.getIntegerField(OrderFieldsOFSPG.LEVEL).toString() + "." + index;
        } else {
            String parentOrderNumber = parentOrder.getStringField(OrderFields.NUMBER);
            postfix = parentOrderNumber + "." + index;
        }

        return postfix;
    }

    public List<Entity> getCoverageProductsForOrder(final Entity coverage, final Entity subOrder) {
        String sql = "select product from #orderSupplies_coverageProduct product, #orderSupplies_materialRequirementCoverage coverage "
                + "where product.materialRequirementCoverage.id = coverage.id and coverage.order.id =:orderId "
                + "and  coverage.id=:coverageId and  product.productType is not null "
                + "and product.productType='02intermediate' and product.state='03lack'";

        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT)
                .find(sql).setParameter("coverageId", coverage.getId()).setParameter("orderId", subOrder.getId()).list()
                .getEntities();
    }

    public List<Entity> getComponentProducts(final Entity coverage, final Entity order) {
        String query = "select product from #orderSupplies_coverageProduct product, "
                + "#orderSupplies_materialRequirementCoverage coverage "
                + "where product.materialRequirementCoverage.id = coverage.id "
                + "and coverage.id=:coverageId and product.productType is not null "
                + "and product.productType='02intermediate' and product.state='03lack'";

        List<Entity> components = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT).find(query)
                .setParameter("coverageId", coverage.getId()).list().getEntities();

        return getComponentProductsForOrder(components, order);
    }

    private List<Entity> getComponentProductsForOrder(final List<Entity> components, final Entity order) {
        List<Entity> componentsForOrder = Lists.newArrayList();
        List<Long> productsIds = materialRequirementCoverageHelper.getOrderProductsIds(order);

        components.forEach(c -> addComponent(c, componentsForOrder, productsIds));

        return componentsForOrder;
    }

    private void addComponent(final Entity component, final List<Entity> componentsForOrder, final List<Long> productsIds) {
        if (productsIds.contains(component.getBelongsToField(CoverageProductFields.PRODUCT).getId())) {
            componentsForOrder.add(component);
        }
    }

    public List<Entity> getSubOrders(final Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.PARENT, order)).list().getEntities();
    }

    public boolean hasSubOrders(final Entity order) {
        if (Objects.isNull(order)) {
            return false;
        }

        SearchCriteriaBuilder scb = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find();

        scb.add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.PARENT, order));
        scb.setProjection(SearchProjections.alias(SearchProjections.countDistinct("id"), COUNT_ALIAS));
        scb.addOrder(SearchOrders.desc(COUNT_ALIAS));

        Entity projectionResult = scb.setMaxResults(1).uniqueResult();

        Long countValue = (Long) projectionResult.getField(COUNT_ALIAS);

        return countValue > 0;
    }

    public boolean hasSubOrders(final List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return false;
        }

        SearchCriteriaBuilder scb = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find();

        scb.createAlias(OrderFieldsOFSPG.PARENT, OrderFieldsOFSPG.PARENT, JoinType.LEFT);
        scb.add(SearchRestrictions.in(OrderFieldsOFSPG.PARENT + ".id", orderIds));
        scb.setProjection(SearchProjections.alias(SearchProjections.countDistinct("id"), COUNT_ALIAS));
        scb.addOrder(SearchOrders.desc(COUNT_ALIAS));

        Entity projectionResult = scb.setMaxResults(1).uniqueResult();

        Long countValue = (Long) projectionResult.getField(COUNT_ALIAS);

        return countValue > 0;
    }

    public List<Entity> getSubOrdersForRootAndLevel(final Entity rootOrder, final int level) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.ROOT, rootOrder))
                .add(SearchRestrictions.eq(OrderFieldsOFSPG.LEVEL, level)).list().getEntities();
    }

    @Transactional
    public void generateOrdersByCoverage(final Entity order) {
        Optional<Entity> coverage = materialRequirementCoverageForOrderService.createMRCFO(order);

        if (coverage.isPresent()) {
            Entity materialRequirementEntity = coverage.get();

            materialRequirementCoverageService.estimateProductCoverageInTime(coverage.get());

            Long materialRequirementCoverageId = materialRequirementEntity.getId();

            LOG.info(String.format("Start generation orders for components. Material requirement coverage : %d",
                    materialRequirementCoverageId));

            if (Objects.nonNull(materialRequirementCoverageId)) {
                Entity materialRequirementEntityDB = materialRequirementEntity.getDataDefinition()
                        .get(materialRequirementEntity.getId());

                List<Entity> orders = materialRequirementEntityDB
                        .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDERS);

                for (Entity orderEntity : orders) {
                    List<Entity> products = getComponentProducts(materialRequirementEntity, orderEntity);

                    int index = 1;
                    for (Entity coverageProduct : products) {
                        generateOrderForSubProduct(coverageProduct, orderEntity, LocaleContextHolder.getLocale(), index);

                        ++index;
                    }

                    if (!products.isEmpty()) {
                        materialRequirementEntity.setField(CoverageForOrderFieldsOFSPG.GENERATED_ORDERS, true);
                        materialRequirementEntity.getDataDefinition().save(materialRequirementEntity);
                    }

                    boolean generateSubOrdersForTree = true;

                    List<Entity> subOrdersForActualLevel;

                    index = 1;
                    while (generateSubOrdersForTree) {
                        subOrdersForActualLevel = getSubOrdersForRootAndLevel(orderEntity, index);

                        if (subOrdersForActualLevel.isEmpty()) {
                            generateSubOrdersForTree = false;
                        }

                        for (Entity subOrder : subOrdersForActualLevel) {
                            Optional<Entity> oCoverage = materialRequirementCoverageForOrderService.createMRCFO(subOrder,
                                    materialRequirementEntity);

                            if (oCoverage.isPresent()) {
                                materialRequirementCoverageService.estimateProductCoverageInTime(oCoverage.get());

                                List<Entity> coverageProducts = getCoverageProductsForOrder(oCoverage.get(), subOrder);

                                int in = 1;
                                for (Entity coverageProduct : coverageProducts) {
                                    generateOrderForSubProduct(coverageProduct, subOrder, LocaleContextHolder.getLocale(), in);

                                    ++in;
                                }
                            } else {
                                throw new IllegalStateException("Coverage generation error");
                            }
                        }

                        ++index;
                    }
                }
            }

            LOG.info(String.format("Finish generation orders for components. Material requirement coverage : %d",
                    materialRequirementCoverageId));
        } else {
            throw new IllegalStateException("Coverage generation error");
        }
    }

    @Transactional
    public void generateOrders(final Entity order) {
        LOG.info("Start generation orders for components");

        List<Entity> orders = Lists.newArrayList(order);

        for (Entity orderEntity : orders) {
            List<Entity> registryEntries = materialRequirementCoverageHelper.findComponentEntries(orderEntity);

            int index = 1;
            for (Entity registryEntry : registryEntries) {
                generateSimpleOrderForSubProduct(registryEntry, orderEntity, LocaleContextHolder.getLocale(), index);

                ++index;
            }

            boolean generateSubOrdersForTree = true;

            List<Entity> subOrdersForActualLevel;

            index = 1;
            while (generateSubOrdersForTree) {
                subOrdersForActualLevel = getSubOrdersForRootAndLevel(orderEntity, index);

                if (subOrdersForActualLevel.isEmpty()) {
                    generateSubOrdersForTree = false;
                }

                for (Entity subOrder : subOrdersForActualLevel) {
                    List<Entity> entries = materialRequirementCoverageHelper.findComponentEntries(subOrder);

                    int in = 1;
                    for (Entity _entry : entries) {
                        generateSimpleOrderForSubProduct(_entry, subOrder, LocaleContextHolder.getLocale(), in);

                        ++in;
                    }
                }

                ++index;
            }
        }

        LOG.info("Finish generation orders for components.");
    }

}
