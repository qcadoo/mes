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
package com.qcadoo.mes.materialRequirementCoverageForOrder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.IncludeInCalculationDeliveries;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageLocationFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductLoggingEventType;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductLoggingFields;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductLoggingState;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageProductState;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageType;
import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.*;
import com.qcadoo.mes.materialRequirements.MaterialRequirementService;
import com.qcadoo.mes.materialRequirements.constants.InputProductsRequiredForType;
import com.qcadoo.mes.materialRequirements.constants.OrderFieldsMR;
import com.qcadoo.mes.orderSupplies.constants.*;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productFlowThruDivision.ProductFlowThruDivisionService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class MaterialRequirementCoverageForOrderServiceImpl implements MaterialRequirementCoverageForOrderService {


    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private MaterialRequirementService materialRequirementService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private ProductFlowThruDivisionService productFlowThruDivisionService;

    @Autowired
    private ParameterService parameterService;


    @Transactional
    @Override
    public void estimateProductCoverageInTime(Entity coverageForOrder) {
        Date coverageToDate = new DateTime(coverageForOrder.getDateField(CoverageForOrderFields.COVERAGE_TO_DATE)).plusMinutes(1)
                .toDate();
        Date actualDate = coverageForOrder.getDateField(CoverageForOrderFields.ACTUAL_DATE);

        String coverageType = coverageForOrder.getStringField(CoverageForOrderFields.COVERAGE_TYPE);

        boolean includeDraftDeliveries = coverageForOrder.getBooleanField(CoverageForOrderFields.INCLUDE_DRAFT_DELIVERIES);

        List<Entity> coverageLocations = coverageForOrder.getHasManyField(CoverageForOrderFields.COVERAGE_LOCATIONS);

        Entity coveredOrder = coverageForOrder.getBelongsToField(CoverageForOrderFields.ORDER);

        List<Entity> coverageProducts = getCoverageProductsForOrder(coveredOrder);
        if (!coverageProducts.isEmpty()) {
            updateCoverageProductsForOrder(coverageProducts);
        }

        List<Entity> includedOrders = getOrdersFromDB(coverageToDate);
        List<Entity> includedDeliveries = getDeliveriesFromDB(coverageToDate, includeDraftDeliveries, coverageLocations);

        Map<Long, Entity> productAndCoverageProducts = Maps.newHashMap();

        estimateProductDeliveriesInTime(productAndCoverageProducts, includedDeliveries, actualDate);
        estimateProductDemandInTime(productAndCoverageProducts, includedOrders, coverageToDate, coveredOrder);
        estimateProductLocationsInTime(productAndCoverageProducts, coverageLocations, actualDate);

        fillCoverageProductStatesAndQuantities(productAndCoverageProducts);

        coverageForOrder.setField(CoverageForOrderFields.COVERAGE_PRODUCTS,
                filterCoverageProducts(productAndCoverageProducts, coverageType));

        coverageForOrder = coverageForOrder.getDataDefinition().save(coverageForOrder);
    }

    private void estimateProductDeliveriesInTime(final Map<Long, Entity> productAndCoverageProducts,
                                                 final List<Entity> includedDeliveries, final Date actualDate) {
        for (Entity delivery : includedDeliveries) {
            Date coverageDate = getCoverageProductLoggingDateForDelivery(delivery, actualDate);

            List<Entity> deliveryProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

            for (Entity deliveryProduct : deliveryProducts) {
                estimateProductDelivery(productAndCoverageProducts,
                        new CoverageProductForDelivery(coverageDate, delivery, deliveryProduct));
            }
        }
    }

    private void estimateProductDelivery(final Map<Long, Entity> productAndCoverageProducts,
                                         final CoverageProductForDelivery coverageProductForDelivery) {
        BigDecimal quantity = coverageProductForDelivery.getDeliveryQuantity();

        coverageProductForDelivery.setQuantity(quantity);
        Entity coverageProductLogging = createCoverageProductLoggingForDelivery(coverageProductForDelivery);
        fillCoverageProductForDelivery(productAndCoverageProducts, coverageProductForDelivery.getProduct(),
                coverageProductLogging);
    }

    private Date getCoverageProductLoggingDateForDelivery(final Entity delivery, final Date actualDate) {
        Date deliveryDate = delivery.getDateField(DeliveryFields.DELIVERY_DATE);
        Date coverageDate;

        if (deliveryDate.before(actualDate)) {
            coverageDate = new DateTime(actualDate).plusSeconds(2).toDate();
        } else {
            coverageDate = deliveryDate;
        }

        return coverageDate;
    }

    private Entity createCoverageProductLoggingForDelivery(final CoverageProductForDelivery coverageProductForDelivery) {
        Entity coverageProductLogging = getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE, coverageProductForDelivery.getCoverageDate());
        coverageProductLogging.setField(CoverageProductLoggingFields.DELIVERY, coverageProductForDelivery.getDelivery());
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(coverageProductForDelivery.getQuantity()));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE,
                CoverageProductLoggingEventType.DELIVERY.getStringValue());
        coverageProductLogging.setField(CoverageProductLoggingFields.STATE, CoverageProductLoggingState.COVERED.getStringValue());

        return coverageProductLogging;
    }

    private void fillCoverageProductForDelivery(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                                final Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(product.getId())) {
                updateCoverageProductForDelivery(productAndCoverageProducts, product, coverageProductLogging);
            } else {
                addCoverageProductForDelivery(productAndCoverageProducts, product, coverageProductLogging);
            }
        }
    }

    private void addCoverageProductForDelivery(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                               final Entity coverageProductLogging) {
        Entity coverageProduct = getCoverageProductDD().create();

        coverageProduct.setField(CoverageProductFields.PRODUCT, product);
        coverageProduct.setField(CoverageProductFields.DELIVERED_QUANTITY, numberService
                .setScaleWithDefaultMathContext(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES)));
        coverageProduct.setField(CoverageProductFields.STATE, CoverageProductState.COVERED.getStringValue());
        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, Lists.newArrayList(coverageProductLogging));

        productAndCoverageProducts.put(product.getId(), coverageProduct);
    }

    private void updateCoverageProductForDelivery(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                                  final Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(product.getId());

        BigDecimal deliveredQuantity = BigDecimalUtils
                .convertNullToZero(addedCoverageProduct.getDecimalField(CoverageProductFields.DELIVERED_QUANTITY));

        deliveredQuantity = deliveredQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists
                .newArrayList(addedCoverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.DELIVERED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(deliveredQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);

        productAndCoverageProducts.put(product.getId(), addedCoverageProduct);
    }

    private void estimateProductDemandInTime(final Map<Long, Entity> productAndCoverageProducts, final List<Entity> orders,
                                             final Date coverageToDate, final Entity coveredOrder) {
        for (Entity order : orders) {
            String coveredOrdersProductsSql = "select toc.id as tocId, parentToc.id as parentId, inputProd.id as prodId, opic.id as opicId, "
                    + "(select count(*) from #technologies_technology t where t.product = inputProd and t.master = true) "
                    + "as prodTechId from  #technologies_operationProductInComponent opic "
                    + "left join opic.product as inputProd left join opic.operationComponent toc "
                    + "left join toc.operation operation left join toc.operationProductOutComponents opoc "
                    + "left join opoc.product outputProd left join toc.technology tech "
                    + "left join toc.parent as parentToc where tech.id = :technologyID ";
            List<Entity> coveredOrderProducts = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                    .find(coveredOrdersProductsSql)
                    .setLong("technologyID", order.getBelongsToField(OrderFields.TECHNOLOGY).getId()).list().getEntities();

            StringBuilder coveredOrdersProductsToSql = new StringBuilder(
                    "select toc.id as tocId, parentToc.id as parentId, inputProd.id as prodId, opic.id as opicId, "
                            + "(select count(*) from #technologies_technology t where t.product = inputProd and t.master = true) "
                            + "as prodTechId from #technologies_operationProductInComponent opic "
                            + "left join opic.product as inputProd left join opic.operationComponent toc "
                            + "left join toc.operation operation left join toc.operationProductOutComponents opoc "
                            + "left join opoc.product outputProd left join toc.technology tech "
                            + "left join toc.parent as parentToc where tech.id = :technologyID and inputProd.id IN ( ");
            boolean isFirst = true;
            for (Entity entity : coveredOrderProducts) {
                if (isFirst) {
                    coveredOrdersProductsToSql.append(entity.getField("prodId"));
                    isFirst = false;
                } else {
                    coveredOrdersProductsToSql.append(" ,");
                    coveredOrdersProductsToSql.append(entity.getField("prodId"));
                }
            }
            coveredOrdersProductsToSql.append(")");

            List<Entity> coveredOrderProductsTo = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                    .find(coveredOrdersProductsToSql.toString())
                    .setLong("technologyID", coveredOrder.getBelongsToField(OrderFields.TECHNOLOGY).getId()).list().getEntities();

            if (!coveredOrderProductsTo.isEmpty()) {
                String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
                String intputProductsRequiredForType = order.getStringField(OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE);

                Map<Long, BigDecimal> productComponentQuantities = Maps.newHashMap();
                OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = new OperationProductComponentWithQuantityContainer();

                if (InputProductsRequiredForType.START_ORDER.getStringValue().equals(intputProductsRequiredForType)) {
                    if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
                        operationProductComponentWithQuantityContainer = productQuantitiesService
                                .getProductComponentQuantitiesWithoutNonComponents(Lists.newArrayList(order));
                    } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording)
                            || TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {
                        productComponentQuantities = productQuantitiesService.getNeededProductQuantities(order,
                                materialRequirementService.getDefaultMrpAlgorithm());
                    }
                }

                operationProductComponentWithQuantityContainer = operationProductComponentWithQuantityContainer
                        .getAllWithSameEntityType(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

                for (Entity productE : coveredOrderProductsTo) {

                    Entity technologyOperationComponent = dataDefinitionService
                            .get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                            .get((Long) productE.getField("tocId"));
                    Entity operationProductInComponent = dataDefinitionService
                            .get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                            .get((Long) productE.getField("opicId"));

                    estimateProductDemandForOperationInComponent(productAndCoverageProducts,
                            new CoverageProductForOrder(coverageToDate, order, technologyOperationComponent, null,
                                    operationProductInComponent, productComponentQuantities,
                                    operationProductComponentWithQuantityContainer),
                            coveredOrder);

                }

            }
        }
    }

    private void estimateProductDemandForOperationInComponent(final Map<Long, Entity> productAndCoverageProducts,
                                                              final CoverageProductForOrder coverageProductForOrder, final Entity coveredOrder) {
        if (checkIfContainsKey(coverageProductForOrder)
                && checkIfProductWithOrderShouldBeAdded(productAndCoverageProducts, coverageProductForOrder.getProduct(),
                coverageProductForOrder.getOrder(), coverageProductForOrder.getTypeOfProductionRecording())) {

            fillProductTypeAndOrder(coverageProductForOrder, coveredOrder);

            BigDecimal quantity = getQuantity(coverageProductForOrder);

            quantity = subtractUsedQuantityFromProductionTrackings(quantity, coverageProductForOrder.getOrder(),
                    coverageProductForOrder.getTechnologyOperationComponent(), coverageProductForOrder.getProduct(),
                    coverageProductForOrder.getTypeOfProductionRecording());

            coverageProductForOrder.setQuantity(quantity);

            if (BigDecimal.ZERO.compareTo(quantity) < 0) {
                Entity coverageProductLogging = createCoverageProductLoggingForOrder(coverageProductForOrder);

                fillCoverageProductForOrder(productAndCoverageProducts, coverageProductForOrder.getProduct(),
                        coverageProductForOrder.getProductType(), coverageProductLogging, coverageProductForOrder.getOrder(),
                        coveredOrder, coverageProductForOrder.getPlannedQuantity());

            }
        }
    }

    private void fillProductTypeAndOrder(final CoverageProductForOrder coverageProductForOrder, final Entity coveredOrder) {
        if (!coverageProductForOrder.getOrder().getId().equals(coveredOrder.getId())) {
            return;
        }
        Entity opic = coverageProductForOrder.getOperationProductInComponent();

        Entity product = opic.getBelongsToField(OperationProductInComponentFields.PRODUCT);

        List<Entity> technologiesForProduct = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).find()
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true))
                .add(SearchRestrictions.or(
                        SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.ACCEPTED.getStringValue()),
                        SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.CHECKED.getStringValue())))
                .list().getEntities();

        if (coverageProductForOrder.getProductComponentQuantities() != null
                && coverageProductForOrder.getProductComponentQuantities().size() > 0) {
            coverageProductForOrder.setPlannedQuantity(coverageProductForOrder.getProductComponentQuantities()
                    .get(coverageProductForOrder.getOperationProductInComponent().getBelongsToField("product").getId()));
        } else {
            coverageProductForOrder.setPlannedQuantity(coverageProductForOrder.getOperationProductComponentWithQuantityContainer()
                    .get(coverageProductForOrder.getOperationProductInComponent()));
        }
        if (technologiesForProduct.isEmpty()) {
            coverageProductForOrder.setProductType(ProductType.COMPONENT);
        } else {
            coverageProductForOrder.setProductType(ProductType.INTERMEDIATE);
        }

    }

    private BigDecimal getQuantity(final CoverageProductForOrder coverageProductForOrder) {
        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(coverageProductForOrder.getTypeOfProductionRecording())) {
            return coverageProductForOrder.getOperationProductComponentWithQuantityContainer()
                    .get(coverageProductForOrder.getOperationProductInComponent());
        } else {
            return coverageProductForOrder.getProductComponentQuantities().get(coverageProductForOrder.getProduct().getId());
        }
    }

    private Entity createCoverageProductLoggingForOrder(final CoverageProductForOrder coverageProductForOrder) {
        Entity coverageProductLogging = getCoverageProductLoggingDD().create();

        String eventType;

        if (coverageProductForOrder.getOperation() == null) {
            eventType = CoverageProductLoggingEventType.ORDER_INPUT.getStringValue();
        } else {
            eventType = CoverageProductLoggingEventType.OPERATION_INPUT.getStringValue();
        }

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE, coverageProductForOrder.getCoverageDate());
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER, coverageProductForOrder.getOrder());
        coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION, coverageProductForOrder.getOperation());
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(coverageProductForOrder.getQuantity()));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, eventType);

        return coverageProductLogging;
    }

    private void fillCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                             final ProductType productType, final Entity coverageProductLogging, final Entity retriveOrder,
                                             final Entity coveredOrder, final BigDecimal plannedQuantity) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(product.getId())) {
                updateCoverageProductForOrder(productAndCoverageProducts, product, productType, coverageProductLogging,
                        retriveOrder, coveredOrder, plannedQuantity);
            } else {
                addCoverageProductForOrder(productAndCoverageProducts, product, productType, coverageProductLogging, retriveOrder,
                        coveredOrder, plannedQuantity);
            }
        }
    }

    private void addCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                            final ProductType productType, final Entity coverageProductLogging, final Entity retriveOrder,
                                            final Entity coveredOrder, final BigDecimal plannedQuantity) {
        Entity coverageProduct = getCoverageProductDD().create();

        coverageProduct.setField(CoverageProductFields.PRODUCT, product);
        coverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY, numberService
                .setScaleWithDefaultMathContext(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES)));
        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, Lists.newArrayList(coverageProductLogging));

        if (retriveOrder.getId().equals(coveredOrder.getId())) {
            coverageProduct.setField(CoverageProductFields.PRODUCT_TYPE, productType.getStringValue());
            coverageProduct.setField(CoverageProductFields.ORDER, coveredOrder);
            coverageProduct.setField(CoverageProductFields.PLANED_QUANTITY, plannedQuantity);
        }

        productAndCoverageProducts.put(product.getId(), coverageProduct);
    }

    private void updateCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                               final ProductType productType, final Entity coverageProductLogging, final Entity retriveOrder,
                                               final Entity coveredOrder, final BigDecimal plannedQuantity) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(product.getId());

        BigDecimal demandQuantity = BigDecimalUtils
                .convertNullToZero(addedCoverageProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY));

        demandQuantity = demandQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists
                .newArrayList(addedCoverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY,
                numberService.setScaleWithDefaultMathContext(demandQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);
        if (retriveOrder.getId().equals(coveredOrder.getId())) {
            addedCoverageProduct.setField(CoverageProductFields.ORDER, coveredOrder);
            addedCoverageProduct.setField(CoverageProductFields.PRODUCT_TYPE, productType.getStringValue());
            addedCoverageProduct.setField(CoverageProductFields.PLANED_QUANTITY, plannedQuantity);
        }
        productAndCoverageProducts.put(product.getId(), addedCoverageProduct);
    }

    private boolean checkIfContainsKey(final CoverageProductForOrder coverageProductForOrder) {
        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(coverageProductForOrder.getTypeOfProductionRecording())) {
            return coverageProductForOrder.getOperationProductComponentWithQuantityContainer()
                    .containsKey(coverageProductForOrder.getOperationProductInComponent());
        } else {
            return coverageProductForOrder.getProductComponentQuantities()
                    .containsKey(coverageProductForOrder.getProduct().getId());
        }
    }

    private boolean checkIfProductWithOrderShouldBeAdded(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
                                                         final Entity order, final String typeOfProductionRecording) {
        if (!TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)
                && productAndCoverageProducts.containsKey(product.getId())) {
            Entity coverageProduct = productAndCoverageProducts.get(product.getId());

            List<Entity> coverageProductLoggings = coverageProduct
                    .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS);

            for (Entity coverageProductLogging : coverageProductLoggings) {
                Entity orderAdded = coverageProductLogging.getBelongsToField(CoverageProductLoggingFields.ORDER);

                if ((orderAdded != null) && order.getId().equals(orderAdded.getId())) {
                    return false;
                }
            }
        }

        return true;
    }

    private void estimateProductLocationsInTime(final Map<Long, Entity> productAndCoverageProducts,
                                                final List<Entity> coverageLocations, final Date actualDate) {
        for (Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
            Entity addedCoverageProduct = productAndCoverageProduct.getValue();

            Entity product = addedCoverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

            BigDecimal locationsQuantity = getLocationsQuantity(coverageLocations, product);
            Entity coverageProductLogging = createCoverageProductLoggingForLocations(actualDate, locationsQuantity);

            List<Entity> coverageProductLoggings = Lists
                    .newArrayList(addedCoverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
            coverageProductLoggings.add(coverageProductLogging);
            addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);
            addedCoverageProduct.setField(CoverageProductFields.LOCATIONS_QUANTITY, locationsQuantity);
        }
    }

    private Entity createCoverageProductLoggingForLocations(final Date actualDate, final BigDecimal locationsQuantity) {
        Entity coverageProductLogging = getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE, actualDate);
        coverageProductLogging.setField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY,
                numberService.setScaleWithDefaultMathContext(locationsQuantity));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE,
                CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue());

        return coverageProductLogging;
    }

    private BigDecimal getLocationsQuantity(final List<Entity> coverageLocations, final Entity product) {
        BigDecimal locationsQuantity = BigDecimal.ZERO;

        for (Entity coverageLocation : coverageLocations) {
            Entity location = coverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);

            BigDecimal resourceQuantity = materialFlowResourcesService.getResourcesQuantityForLocationAndProduct(location,
                    product);

            if (resourceQuantity != null) {
                locationsQuantity = locationsQuantity.add(resourceQuantity, numberService.getMathContext());
            }
        }

        return locationsQuantity;
    }

    private void fillCoverageProductStatesAndQuantities(final Map<Long, Entity> productAndCoverageProducts) {
        for (Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
            Entity coverageProduct = productAndCoverageProduct.getValue();
            fillCoverageProductLoggingsStates(coverageProduct);
            fillCoverageProductQuantities(coverageProduct);
        }
    }

    private void fillCoverageProductQuantities(final Entity coverageProduct) {
        BigDecimal coveredQuantity;
        BigDecimal reserveMissingQuantity;

        BigDecimal demandQuantity = BigDecimalUtils
                .convertNullToZero(coverageProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY));
        BigDecimal deliveredQuantity = BigDecimalUtils
                .convertNullToZero(coverageProduct.getDecimalField(CoverageProductFields.DELIVERED_QUANTITY));
        BigDecimal locationsQuantity = BigDecimalUtils
                .convertNullToZero(coverageProduct.getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY));

        coveredQuantity = deliveredQuantity.add(locationsQuantity, numberService.getMathContext());
        reserveMissingQuantity = coveredQuantity.subtract(demandQuantity, numberService.getMathContext());

        Date lackFromDate = coverageProduct.getDateField(CoverageProductFields.LACK_FROM_DATE);

        String state;
        if (reserveMissingQuantity.compareTo(BigDecimal.ZERO) >= 0) {
            if (lackFromDate == null) {
                state = CoverageProductState.COVERED.getStringValue();
            } else {
                state = CoverageProductState.DELAY.getStringValue();
            }
        } else {
            state = CoverageProductState.LACK.getStringValue();
        }

        coverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY,
                numberService.setScaleWithDefaultMathContext(demandQuantity));
        coverageProduct.setField(CoverageProductFields.COVERED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(coveredQuantity));
        coverageProduct.setField(CoverageProductFields.RESERVE_MISSING_QUANTITY,
                numberService.setScaleWithDefaultMathContext(reserveMissingQuantity));
        coverageProduct.setField(CoverageProductFields.DELIVERED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(deliveredQuantity));
        coverageProduct.setField(CoverageProductFields.LOCATIONS_QUANTITY,
                numberService.setScaleWithDefaultMathContext(locationsQuantity));
        coverageProduct.setField(CoverageProductFields.STATE, state);
    }

    private void fillCoverageProductLoggingsStates(final Entity coverageProduct) {
        List<Entity> coverageProductLoggings = Lists
                .newLinkedList(coverageProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.sort((entity1, entity2) -> {
            Date date1 = entity1.getDateField(CoverageProductLoggingFields.DATE);
            Date date2 = entity2.getDateField(CoverageProductLoggingFields.DATE);

            return date1.compareTo(date2);
        });

        BigDecimal reserveMissingQuantity = BigDecimal.ZERO;

        for (Entity coverageProductLogging : coverageProductLoggings) {
            String eventType = coverageProductLogging.getStringField(CoverageProductLoggingFields.EVENT_TYPE);

            if (CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue().equals(eventType)) {
                BigDecimal quantity = coverageProductLogging
                        .getDecimalField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY);

                reserveMissingQuantity = reserveMissingQuantity.add(quantity, numberService.getMathContext());
            } else if (CoverageProductLoggingEventType.DELIVERY.getStringValue().equals(eventType)) {
                BigDecimal quantity = coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES);

                reserveMissingQuantity = reserveMissingQuantity.add(quantity, numberService.getMathContext());

                fillCoverageProductLogginState(coverageProduct, coverageProductLogging, reserveMissingQuantity);
            } else if (CoverageProductLoggingEventType.OPERATION_INPUT.getStringValue().equals(eventType)
                    || CoverageProductLoggingEventType.ORDER_INPUT.getStringValue().equals(eventType)) {
                BigDecimal quantity = coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES);

                reserveMissingQuantity = reserveMissingQuantity.subtract(quantity, numberService.getMathContext());

                fillCoverageProductLogginState(coverageProduct, coverageProductLogging, reserveMissingQuantity);
            }
        }
    }

    private void fillCoverageProductLogginState(final Entity coverageProduct, final Entity coverageProductLogging,
                                                final BigDecimal reserveMissingQuantity) {
        String state = null;

        if (reserveMissingQuantity.compareTo(BigDecimal.ZERO) >= 0) {
            state = CoverageProductLoggingState.COVERED.getStringValue();
        } else {
            state = CoverageProductLoggingState.LACK.getStringValue();

            Date lackFromDate = coverageProduct.getDateField(CoverageProductFields.LACK_FROM_DATE);

            if (lackFromDate == null) {
                lackFromDate = coverageProductLogging.getDateField(CoverageProductLoggingFields.DATE);

                coverageProduct.setField(CoverageProductFields.LACK_FROM_DATE, lackFromDate);
            }
        }

        coverageProductLogging.setField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY, reserveMissingQuantity);
        coverageProductLogging.setField(CoverageProductLoggingFields.STATE, state);
    }

    private List<Entity> filterCoverageProducts(final Map<Long, Entity> productAndCoverageProducts, final String coverageType) {
        return filterCoverageProductsWithCoverageType(getCoverageProducts(productAndCoverageProducts), coverageType);
    }

    private List<Entity> getCoverageProducts(final Map<Long, Entity> productAndCoverageProducts) {
        return Lists.newArrayList(productAndCoverageProducts.values());
    }

    private List<Entity> filterCoverageProductsWithCoverageType(final List<Entity> coverageProducts, final String coverageType) {
        List<Entity> filteredCoverageProducts = Lists.newArrayList();

        for (Entity coverageProduct : coverageProducts) {
            if (CoverageType.WITHOUT_PRODUCTS_FROM_WAREHOUSE.getStringValue().equals(coverageType)) {
                BigDecimal locationsQuantity = coverageProduct.getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY);
                BigDecimal demandQuantity = coverageProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY);

                if (locationsQuantity.compareTo(demandQuantity) < 0) {
                    filteredCoverageProducts.add(coverageProduct);
                }
            } else if (CoverageType.ONLY_SHORCOMINGS_AND_DELAYS.getStringValue().equals(coverageType)) {
                String coverageProductState = coverageProduct.getStringField(CoverageProductFields.STATE);

                if (CoverageProductState.LACK.getStringValue().equals(coverageProductState)
                        || CoverageProductState.DELAY.getStringValue().equals(coverageProductState)) {
                    filteredCoverageProducts.add(coverageProduct);
                }
            } else {
                filteredCoverageProducts.add(coverageProduct);
            }
        }

        return filteredCoverageProducts;
    }

    private List<Entity> getOrdersFromDB(final Date coverageToDate) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.le(OrderFields.START_DATE, coverageToDate))
                .add(SearchRestrictions.isNotNull(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                .add(SearchRestrictions.isNotNull(OrderFields.TECHNOLOGY))
                .add(SearchRestrictions.or(SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.PENDING),
                        SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.ACCEPTED),
                        SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.INTERRUPTED)))
                .add(SearchRestrictions.eq(OrderFields.ACTIVE, true)).list().getEntities();
    }

    private List<Entity> getDeliveriesFromDB(final Date coverageToDate, final boolean includeDraftDeliveries,
                                             List<Entity> coverageLocations) {
        if (includeDraftDeliveries) {
            SearchCriteriaBuilder scb = getDeliveryDD()
                    .find()
                    .add(SearchRestrictions.le(DeliveryFields.DELIVERY_DATE, coverageToDate))
                    .add(SearchRestrictions.or(SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.PREPARED),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.DURING_CORRECTION),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.APPROVED)))
                    .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true));
            if (!coverageLocations.isEmpty()) {
                scb = scb.createAlias(DeliveryFields.LOCATION, DeliveryFields.LOCATION, JoinType.LEFT).add(
                        SearchRestrictions.in(
                                DeliveryFields.LOCATION + L_DOT + L_ID,
                                coverageLocations.stream()
                                        .map(cl -> cl.getBelongsToField(
                                                com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields.LOCATION).getId())
                                        .collect(Collectors.toList())));
            }
            return scb.list().getEntities();
        } else {
            SearchCriteriaBuilder scb = getDeliveryDD()
                    .find()
                    .add(SearchRestrictions.le(DeliveryFields.DELIVERY_DATE, coverageToDate))
                    .add(SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.APPROVED))
                    .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true));
            if (!coverageLocations.isEmpty()) {
                scb = scb.createAlias(DeliveryFields.LOCATION, DeliveryFields.LOCATION, JoinType.LEFT).add(
                        SearchRestrictions.in(
                                DeliveryFields.LOCATION + L_DOT + L_ID,
                                coverageLocations.stream()
                                        .map(cl -> cl.getBelongsToField(
                                                com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields.LOCATION).getId())
                                        .collect(Collectors.toList())));
            }
            return scb.list().getEntities();

        }
    }

    private BigDecimal subtractUsedQuantityFromProductionTrackings(final BigDecimal quantity, final Entity order,
                                                                   final Entity technologyOperationComponent, final Entity product, final String typeOfProductionRecording) {
        BigDecimal usedQuantity;

        if (TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {
            usedQuantity = getUsedQuantityForBasic(order, product);
        } else {
            usedQuantity = getUsedQuantityForOtherFromProductionTrackings(order, technologyOperationComponent, product,
                    typeOfProductionRecording);
        }

        BigDecimal demandQuantity = quantity.subtract(usedQuantity, numberService.getMathContext());

        if (demandQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return demandQuantity;
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getUsedQuantityForBasic(final Entity order, final Entity product) {
        BigDecimal usedQuantity = BigDecimal.ZERO;

        Entity basicProductionCounting = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING)
                .find().add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (basicProductionCounting != null) {
            BigDecimal quantity = basicProductionCounting.getDecimalField(BasicProductionCountingFields.USED_QUANTITY);

            if (quantity != null) {
                usedQuantity = usedQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return usedQuantity;
    }

    private BigDecimal getUsedQuantityForOtherFromProductionTrackings(final Entity order,
                                                                      final Entity technologyOperationComponent, final Entity product, final String typeOfProductionRecording) {
        BigDecimal usedQuantity = BigDecimal.ZERO;

        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order));

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent));
        }

        List<Entity> productionTrackings = searchCriteriaBuilder.list().getEntities();

        for (Entity productionTracking : productionTrackings) {
            Entity trackingOperationProductInComponent = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS).find()
                    .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCT, product))
                    .setMaxResults(1).uniqueResult();

            if (trackingOperationProductInComponent != null) {
                BigDecimal quantity = trackingOperationProductInComponent
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                if (quantity != null) {
                    usedQuantity = usedQuantity.add(quantity, numberService.getMathContext());
                }
            }
        }

        return usedQuantity;
    }

    @Override
    public Entity getMRCForOrder(Long mRCId) {
        return getMRCDD().get(mRCId);
    }

    @Override
    public DataDefinition getMRCDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE);
    }

    private DataDefinition getCoverageProductLoggingDD() {

        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT_LOGGING);
    }

    private DataDefinition getCoverageProductDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT);
    }

    @Override
    public DataDefinition getCoverageLocationDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_LOCATION);
    }

    @Override
    public List<Entity> getColumnsForCoveragesForOrder() {
        return getColumnForCoveragesForOrderDD().find().addOrder(SearchOrders.asc(ColumnForCoveragesForOrderFields.SUCCESSION))
                .list().getEntities();
    }

    @Override
    public DataDefinition getColumnForCoveragesForOrderDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COLUMN_FOR_COVERAGES);
    }

    @Override
    public Optional<Entity> createMRCFO(final Entity order, final Entity materialRequirementEntity) {

        Entity mcfo = createBaseCoverage(order);

        mcfo.setField(MaterialRequirementCoverageFields.COVERAGE_TYPE,
                materialRequirementEntity.getStringField(MaterialRequirementCoverageFields.COVERAGE_TYPE));
        mcfo.setField(MaterialRequirementCoverageFields.INCLUDE_IN_CALCULATION_DELIVERIES,
                materialRequirementEntity.getStringField(MaterialRequirementCoverageFields.INCLUDE_IN_CALCULATION_DELIVERIES));

        return saveCoverage(mcfo);
    }

    @Override
    public Optional<Entity> createMRCFO(final Entity order) {
        Entity mcfo = createBaseCoverage(order);

        mcfo.setField(MaterialRequirementCoverageFields.COVERAGE_TYPE, CoverageType.ALL.getStringValue());
        mcfo.setField(MaterialRequirementCoverageFields.INCLUDE_IN_CALCULATION_DELIVERIES, IncludeInCalculationDeliveries.CONFIRMED_DELIVERIES.getStringValue());

        return saveCoverage(mcfo);
    }

    private Optional<Entity> saveCoverage(Entity mcfo) {
        mcfo = mcfo.getDataDefinition().save(mcfo);

        if (!mcfo.isValid()) {
            mcfo = null;
        }

        return Optional.ofNullable(mcfo);
    }

    private Entity createBaseCoverage(final Entity order) {
        Entity mcfo = getMRCDD().create();

        mcfo.setField("order", order);

        String mrcfoNumber = "~~" + new Date().getTime();

        mcfo.setField(MaterialRequirementCoverageFields.NUMBER, mrcfoNumber);
        mcfo.setField(MaterialRequirementCoverageFields.SAVED, false);

        fillDatesMRCFO(mcfo, order);
        fillLocationMRCFO(mcfo, order);

        return mcfo;
    }

    private void fillLocationMRCFO(final Entity mcfo, final Entity order) {
        Set<Entity> locations = productFlowThruDivisionService.getProductsLocations(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
        List<Entity> parameterCoverageLocations = parameterService.getParameter()
                .getHasManyField(ParameterFieldsOS.COVERAGE_LOCATIONS);

        for (Entity parameterCoverageLocation : parameterCoverageLocations) {
            Entity location = parameterCoverageLocation
                    .getBelongsToField(com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields.LOCATION);
            locations.add(location);
        }

        List<Entity> list = Lists.newArrayList();
        for (Entity en : locations) {
            Entity location = getCoverageLocationDD().create();
            location.setField(CoverageLocationFields.LOCATION, en);
            location = getCoverageLocationDD().save(location);
            list.add(location);
        }

        mcfo.setField(CoverageForOrderFields.COVERAGE_LOCATIONS, list);
    }

    private void fillDatesMRCFO(final Entity mcfo, final Entity order) {
        mcfo.setField(CoverageForOrderFields.ACTUAL_DATE, new Date());
        if (order.getDateField(OrderFields.DATE_FROM) == null) {
            mcfo.setField(CoverageForOrderFields.COVERAGE_TO_DATE, DateTime.now().plusDays(1).toDate());
            return;
        }
        mcfo.setField(CoverageForOrderFields.COVERAGE_TO_DATE, order.getDateField(OrderFields.START_DATE));
    }

    private List<Entity> getCoverageProductsForOrder(final Entity order) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT).find();

        scb.add(SearchRestrictions.belongsTo(CoverageProductFields.ORDER, OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER, order.getId()));
        return scb.list().getEntities();

    }

    private void updateCoverageProductsForOrder(final List<Entity> coverageProducts) {
        for (Entity coverageProduct : coverageProducts) {
            coverageProduct.setField(CoverageProductFields.ORDER, null);
            coverageProduct.getDataDefinition().save(coverageProduct);
        }
    }

    private DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
    }
}
