/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.ordersForSubproductsGeneration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.MaterialRequirementCoverageForOrderService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductState;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.MaterialRequirementCoverageForOrderConstans;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageRegisterFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orderSupplies.constants.ProductType;
import com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageService;
import com.qcadoo.mes.orderSupplies.register.RegisterService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.CoverageForOrderFieldsOFSPG;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrderFieldsOFSPG;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
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
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrdersForSubproductsGenerationService {

    protected static final Logger LOG = LoggerFactory.getLogger(OrdersForSubproductsGenerationService.class);

    private static final String COUNT_ALIAS = "count";

    private static final List<String> L_ORDER_FIELD_NAMES = Lists.newArrayList(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
            OrderFieldsPC.REGISTER_PIECEWORK, OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
            OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, OrderFieldsPC.JUST_ONE, OrderFieldsPC.ALLOW_TO_CLOSE,
            OrderFieldsPC.AUTO_CLOSE_ORDER, OrderFieldsPC.REGISTER_PRODUCTION_TIME);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private MaterialRequirementCoverageForOrderService materialRequirementCoverageForOrderService;

    @Autowired
    private MaterialRequirementCoverageService materialRequirementCoverageService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    private static final Integer START_LEVEL = 1;

    private static final String L_COMPONENT = "component";

    public List<Entity> getProductNodesWithCheckedTechnologies(final ViewDefinitionState view, final Entity order) {
        EntityTree tree = productStructureTreeService.generateProductStructureTree(view,
                order.getBelongsToField(OrderFields.TECHNOLOGY));
        return tree
                .stream()
                .filter(node -> node.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE).equals(L_COMPONENT)
                        && (node.getBelongsToField(ProductStructureTreeNodeFields.TECHNOLOGY) != null && node
                                .getBelongsToField(ProductStructureTreeNodeFields.TECHNOLOGY)
                                .getStringField(TechnologyFields.STATE).equals(TechnologyState.CHECKED.getStringValue())))
                .collect(Collectors.toList());
    }

    public Entity getMainTocForOrder(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        Entity mainTOC = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                .add(SearchRestrictions.isNull(TechnologyOperationComponentFields.PARENT)).uniqueResult();
        return mainTOC;
    }

    public List<Entity> getCoveragProductsForTOC(final Entity toc, final Entity materialRequirementCoverage) {
        List<Entity> inputProducts = toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

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

    private Entity getTechnologyForProduct(final Entity product) {
        Entity technology = null;
        technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                .find().add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product))
                .add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).uniqueResult();

        return technology;
    }

    @Transactional
    public void generateSimpleOrderForSubProduct(final Entity entry, final Entity parentOrder, final Locale locale,
            final int index) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).create();
        Entity product = entry.getBelongsToField("product");
        LOG.info(String.format("Start generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));

        order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, entry.getDecimalField("productionCountingQuantities"));
        order.setField(OrderFields.PLANNED_QUANTITY, entry.getDecimalField("productionCountingQuantities"));

        Entity technology = getTechnologyForProduct(product);
        order.setField(OrderFieldsOFSPG.PARENT, parentOrder);
        if (parentOrder.getBelongsToField(OrderFieldsOFSPG.PARENT) == null) {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder);
            order.setField(OrderFieldsOFSPG.LEVEL, START_LEVEL);
        } else {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder.getBelongsToField(OrderFieldsOFSPG.ROOT));
            order.setField(OrderFieldsOFSPG.LEVEL, parentOrder.getIntegerField(OrderFieldsOFSPG.LEVEL) + 1);
        }
        order.setField(OrderFields.NUMBER, generateOrderNumber(parentOrder, order, index));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, locale));
        order.setField(OrderFields.PRODUCT, product);

        order.setField(OrderFields.ORDER_TYPE, OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue());
        order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, technology);
        Entity prodLine = technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTION_LINE);
        if (prodLine != null) {
            order.setField(OrderFields.PRODUCTION_LINE, prodLine);
        } else {
            order.setField(OrderFields.PRODUCTION_LINE, parentOrder.getBelongsToField(OrderFields.PRODUCTION_LINE));
        }

        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                parentOrder.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
        order.setField(OrderFields.DATE_FROM, parentOrder.getDateField(OrderFields.START_DATE));
        order.setField(OrderFields.DATE_TO, parentOrder.getDateField(OrderFields.FINISH_DATE));
        order.setField(OrderFields.COMPANY, parentOrder.getBelongsToField(OrderFields.COMPANY));
        order.setField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS,
                parentOrder.getBooleanField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS));
        setOrderWithDefaultProductionCountingValues(order, technology);
        order = order.getDataDefinition().save(order);
        order.isActive();
        LOG.info(String.format("Finish generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));
    }

    @Transactional
    public void generateOrderForSubProduct(final Entity coverageProduct, final Entity parentOrder, final Locale locale,
            final int index) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).create();

        Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);
        LOG.info(String.format("Start generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));

        Entity productLog = findForOrder(parentOrder, coverageProduct);

        BigDecimal planedQuantity = coverageProduct.getDecimalField(CoverageProductFields.PLANED_QUANTITY);
        BigDecimal missing = productLog.getDecimalField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY).abs(
                numberService.getMathContext());
        if (missing.compareTo(planedQuantity) == 1) {
            order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, planedQuantity);
            order.setField(OrderFields.PLANNED_QUANTITY, planedQuantity);
        } else {
            order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, missing);
            order.setField(OrderFields.PLANNED_QUANTITY, missing);
        }

        Entity technology = getTechnologyForProduct(product);
        order.setField(OrderFieldsOFSPG.PARENT, parentOrder);
        if (parentOrder.getBelongsToField(OrderFieldsOFSPG.PARENT) == null) {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder);
            order.setField(OrderFieldsOFSPG.LEVEL, START_LEVEL);
        } else {
            order.setField(OrderFieldsOFSPG.ROOT, parentOrder.getBelongsToField(OrderFieldsOFSPG.ROOT));
            order.setField(OrderFieldsOFSPG.LEVEL, parentOrder.getIntegerField(OrderFieldsOFSPG.LEVEL) + 1);
        }
        order.setField(OrderFields.NUMBER, generateOrderNumber(parentOrder, order, index));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, locale));
        order.setField(OrderFields.PRODUCT, product);

        order.setField(OrderFields.ORDER_TYPE, OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue());
        order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, technology);
        Entity prodLine = technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTION_LINE);
        if (prodLine != null) {
            order.setField(OrderFields.PRODUCTION_LINE, prodLine);
        } else {
            order.setField(OrderFields.PRODUCTION_LINE, parentOrder.getBelongsToField(OrderFields.PRODUCTION_LINE));
        }

        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                parentOrder.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
        order.setField(OrderFields.DATE_FROM, parentOrder.getDateField(OrderFields.START_DATE));
        order.setField(OrderFields.DATE_TO, parentOrder.getDateField(OrderFields.FINISH_DATE));
        order.setField(OrderFields.COMPANY, parentOrder.getBelongsToField(OrderFields.COMPANY));
        order.setField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS,
                parentOrder.getBooleanField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS));
        setOrderWithDefaultProductionCountingValues(order, technology);
        order = order.getDataDefinition().save(order);
        order.isActive();
        LOG.info(String.format("Finish generation order for order : %s , product %s",
                parentOrder.getStringField(OrderFields.NUMBER), product.getStringField(ProductFields.NUMBER)));
    }

    private Entity findForOrder(Entity parentOrder, Entity coverageProduct) {

        return coverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)
                .stream()
                .filter(e -> Objects.nonNull(e.getBelongsToField(CoverageProductLoggingFields.ORDER)))
                .filter(entity -> entity.getBelongsToField(CoverageProductLoggingFields.ORDER).getId()
                        .equals(parentOrder.getId())).findFirst().get();
    }

    private void setOrderWithDefaultProductionCountingValues(final Entity order, final Entity technology) {
        for (String fieldName : L_ORDER_FIELD_NAMES) {
            order.setField(fieldName, technology.getField(fieldName));
        }
    }

    private String generateOrderNumber(final Entity parentOrder, final Entity order, final int index) {
        StringBuffer number = new StringBuffer();

        number.append(generatePostfixForNumber(parentOrder, order, index));

        return number.toString();
    }

    private String generatePostfixForNumber(final Entity parentOrder, final Entity order, final int index) {
        String postfix = "";
        if (parentOrder.getBelongsToField(OrderFieldsOFSPG.PARENT) == null) {
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
        List<Entity> components = Lists.newArrayList();

        StringBuilder _query = new StringBuilder();
        _query.append("select product from #orderSupplies_coverageProduct product, ");
        _query.append("#orderSupplies_materialRequirementCoverage coverage ");
        _query.append("where product.materialRequirementCoverage.id = coverage.id ");
        _query.append("and coverage.id=:coverageId and product.productType is not null ");
        _query.append("and product.productType='02intermediate' and product.state='03lack'");

        components = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT)
                .find(_query.toString()).setParameter("coverageId", coverage.getId()).list().getEntities();

        return getComponentProductsForOrder(components, order);
    }

    private List<Entity> getComponentProductsForOrder(List<Entity> components, Entity order) {
        List<Entity> componentsForOrder = Lists.newArrayList();
        List<Entity> entries = registerService.getRegisterEntriesForOrder(order);
        Map<Long, Entity> entriesProductMap = Maps.newHashMap();
        for (Entity entity : entries) {
            if (!entriesProductMap.containsKey(entity.getBelongsToField(CoverageRegisterFields.PRODUCT).getId())) {
                entriesProductMap.put(entity.getBelongsToField(CoverageRegisterFields.PRODUCT).getId(),
                        entity.getBelongsToField(CoverageRegisterFields.PRODUCT));
            }
        }
        components.stream().forEach(c -> addComponent(c, componentsForOrder, entriesProductMap));
        return componentsForOrder;
    }

    private void addComponent(Entity component, List<Entity> componentsForOrder, Map<Long, Entity> entriesProductMap) {
        if (entriesProductMap.containsKey(component.getBelongsToField(CoverageProductFields.PRODUCT).getId())) {
            componentsForOrder.add(component);
        }
    }

    public List<Entity> getSubOrders(final Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.PARENT, order)).list().getEntities();
    }

    public boolean hasSubOrders(final Entity order) {
        if (order == null) {
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

    public boolean hasSubOrders(List<Long> orderIds) {
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
    public void generateOrdersByCoverage(Entity order) {
        com.google.common.base.Optional<Entity> coverage = materialRequirementCoverageForOrderService.createMRCFO(order);
        if (coverage.isPresent()) {
            Entity materialRequirementEntity = coverage.get();
            materialRequirementCoverageService.estimateProductCoverageInTime(coverage.get());
            Long materialRequirementCoverageId = materialRequirementEntity.getId();
            LOG.info(String.format("Start generation orders for components. Material requirement coverage : %d",
                    materialRequirementCoverageId));
            Integer generatedOrders = 0;
            if (materialRequirementCoverageId != null) {
                Entity materialRequirementEntityDB = materialRequirementEntity.getDataDefinition().get(
                        materialRequirementEntity.getId());
                List<Entity> orders = materialRequirementEntityDB
                        .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDERS);
                for (Entity orderEntity : orders) {
                    List<Entity> products = getComponentProducts(materialRequirementEntity, orderEntity);
                    int index = 1;
                    for (Entity coverageProduct : products) {
                        generateOrderForSubProduct(coverageProduct, orderEntity, LocaleContextHolder.getLocale(), index);
                        ++index;
                        ++generatedOrders;
                    }
                    if (!products.isEmpty()) {
                        materialRequirementEntity.setField(CoverageForOrderFieldsOFSPG.GENERATED_ORDERS, true);
                        materialRequirementEntity.getDataDefinition().save(materialRequirementEntity);
                    }
                    index = 1;
                    boolean generateSubOrdersForTree = true;
                    List<Entity> subOrdersForActualLevel = Lists.newArrayList();
                    while (generateSubOrdersForTree) {
                        subOrdersForActualLevel = getSubOrdersForRootAndLevel(orderEntity, index);
                        if (subOrdersForActualLevel.isEmpty()) {
                            generateSubOrdersForTree = false;
                        }
                        for (Entity sorder : subOrdersForActualLevel) {
                            Optional<Entity> oCoverage = materialRequirementCoverageForOrderService.createMRCFO(sorder,
                                    materialRequirementEntity);

                            if (oCoverage.isPresent()) {
                                materialRequirementCoverageService.estimateProductCoverageInTime(oCoverage.get());

                                List<Entity> productss = getCoverageProductsForOrder(oCoverage.get(), sorder);

                                int in = 1;
                                for (Entity coverageProduct : productss) {
                                    generateOrderForSubProduct(coverageProduct, sorder, LocaleContextHolder.getLocale(), in);
                                    ++in;
                                    ++generatedOrders;
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
    public void generateOrders(Entity order) {
        LOG.info(String.format("Start generation orders for components"));
        Integer generatedOrders = 0;
        List<Entity> orders = Lists.newArrayList(order);
        for (Entity orderEntity : orders) {
            List<Entity> registryEntries = registerService.findComponentRegistryEntries(orderEntity);

            int index = 1;
            for (Entity registryEntry : registryEntries) {
                generateSimpleOrderForSubProduct(registryEntry, orderEntity, LocaleContextHolder.getLocale(), index);
                ++index;
                ++generatedOrders;
            }

            index = 1;
            boolean generateSubOrdersForTree = true;

            List<Entity> subOrdersForActualLevel = Lists.newArrayList();

            while (generateSubOrdersForTree) {

                subOrdersForActualLevel = getSubOrdersForRootAndLevel(orderEntity, index);

                if (subOrdersForActualLevel.isEmpty()) {
                    generateSubOrdersForTree = false;
                }
                for (Entity sorder : subOrdersForActualLevel) {

                    List<Entity> entries = registerService.findComponentRegistryEntries(sorder);

                    int in = 1;
                    for (Entity _entry : entries) {
                        generateSimpleOrderForSubProduct(_entry, sorder, LocaleContextHolder.getLocale(), in);
                        ++in;
                        ++generatedOrders;
                    }
                }
                ++index;
            }
        }
        LOG.info(String.format("Finish generation orders for components."));
    }
}
