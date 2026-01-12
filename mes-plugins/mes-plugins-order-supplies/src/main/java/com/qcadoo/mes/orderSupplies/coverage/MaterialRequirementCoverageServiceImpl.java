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
package com.qcadoo.mes.orderSupplies.coverage;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.MaterialCostsUsed;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.IncludeInCalculationDeliveries;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.*;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class MaterialRequirementCoverageServiceImpl implements MaterialRequirementCoverageService {

    private static final Logger LOG = LoggerFactory.getLogger(MaterialRequirementCoverageServiceImpl.class);

    private static final String L_ORDER = "order";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private MaterialRequirementCoverageHelper materialRequirementCoverageHelper;

    @Autowired
    private ParameterService parameterService;

    @Transactional
    @Override
    public void estimateProductCoverageInTime(final Entity materialRequirementCoverage) {
        LOG.info("Start generation material requirement - id : " + materialRequirementCoverage.getId());

        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);
        Date actualDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.ACTUAL_DATE);

        String coverageType = materialRequirementCoverage.getStringField(MaterialRequirementCoverageFields.COVERAGE_TYPE);

        String includeInCalculationDeliveries = materialRequirementCoverage
                .getStringField(MaterialRequirementCoverageFields.INCLUDE_IN_CALCULATION_DELIVERIES);

        List<Entity> coverageLocations = materialRequirementCoverage
                .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS);

        List<Entity> includedDeliveries = getDeliveriesFromDB(coverageToDate, includeInCalculationDeliveries, coverageLocations);

        Map<Long, Entity> productAndCoverageProducts = Maps.newHashMap();

        List<Entity> orderStates = materialRequirementCoverage
                .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDER_STATES);
        List<Entity> selectedOrders = materialRequirementCoverage.getHasManyField("coverageOrders");

        if (!selectedOrders.isEmpty()) {
            orderStates = Collections.emptyList();
        }

        Entity assignedOrder = materialRequirementCoverage.getBelongsToField(L_ORDER);
        fillFromProductionCounting(productAndCoverageProducts, assignedOrder, coverageToDate, actualDate, orderStates);

        estimateProductLocationsInTime(materialRequirementCoverage, productAndCoverageProducts, coverageLocations, actualDate);

        estimateProductDeliveriesInTime(materialRequirementCoverage, productAndCoverageProducts, includedDeliveries, actualDate,
                coverageToDate, includeInCalculationDeliveries);

        estimateProductProducedInTimeFromPQ(productAndCoverageProducts, coverageToDate, actualDate, orderStates);

        additionalProcessProductCoverage(materialRequirementCoverage, productAndCoverageProducts);

        fillCoverageProductStatesAndQuantities(productAndCoverageProducts);

        fillCoverageProductSupplier(productAndCoverageProducts);

        materialRequirementCoverage.getDataDefinition().save(materialRequirementCoverage);

        saveCoverage(materialRequirementCoverage, filterCoverageProducts(productAndCoverageProducts, coverageType));

        LOG.info("Finish generation material requirement - id : " + materialRequirementCoverage.getId());
    }

    private void estimateProductProducedInTimeFromPQ(final Map<Long, Entity> productAndCoverageProducts,
                                                     final Date coverageToDate, final Date actualDate,
                                                     final List<Entity> orderStates) {

        List<String> states = Lists.newArrayList();

        if (orderStates != null && !orderStates.isEmpty()) {
            states = orderStates.stream().map(o -> o.getStringField(CoverageOrderStateFields.STATE)).collect(Collectors.toList());
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT registry FROM #orderSupplies_productionCountingQuantityOutput AS registry ");

        query.append("WHERE registry.finishDate <= :dateTo ");

        if (!states.isEmpty()) {
            query.append("AND orderState IN (:states)");
        }

        SearchQueryBuilder queryBuilder = getCoverageRegisterDD().find(query.toString()).setParameter("dateTo", coverageToDate);

        if (!states.isEmpty()) {
            queryBuilder.setParameterList("states", states);
        }

        List<Entity> regs = queryBuilder.list().getEntities();

        for (Entity reg : regs) {
            if (BigDecimal.ZERO.compareTo(reg.getDecimalField(CoverageRegisterFields.QUANTITY)) < 0) {
                Entity coverageProductLogging = materialRequirementCoverageHelper.createProductLoggingForOrderProduced(reg,
                        actualDate, coverageToDate);

                materialRequirementCoverageHelper.fillCoverageProductForOrderProduced(productAndCoverageProducts,
                        Long.valueOf(reg.getIntegerField("productId")), coverageProductLogging);
            }
        }
    }

    private void saveCoverage(final Entity materialRequirementCoverage, final List<Entity> entities) {
        List<Entity> selectedOrders = materialRequirementCoverage.getHasManyField("coverageOrders");

        if (!selectedOrders.isEmpty()) {
            List<Entity> filtredEntities = entities.stream()
                    .filter(e -> e.getBooleanField(CoverageProductFields.FROM_SELECTED_ORDER)).collect(Collectors.toList());

            for (Entity covProduct : filtredEntities) {
                saveCoverageProduct(materialRequirementCoverage, covProduct);
                for (Entity log : covProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)) {
                    saveCoverageProductLogging(log);
                }
            }
        } else {
            for (Entity covProduct : entities) {
                saveCoverageProduct(materialRequirementCoverage, covProduct);
                for (Entity log : covProduct.getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS)) {
                    saveCoverageProductLogging(log);
                }
            }
        }
    }

    private void saveCoverageProductLogging(final Entity log) {
        String sqlLog = "INSERT INTO ordersupplies_coverageproductlogging(coverageproduct_id, date, "
                + "order_id, delivery_id, operation_id, reservemissingquantity, changes, eventtype, state, warehouseNumber, deliveryNumberExternal) "
                + "VALUES (currval('ordersupplies_coverageproduct_id_seq'), :date, :order_id, :delivery_id, :operation_id, "
                + ":reservemissingquantity, :changes, :eventtype, :state, :warehouseNumber, :deliveryNumberExternal);";

        Map<String, Object> parametersLogg = Maps.newHashMap();

        parametersLogg.put("date", log.getDateField(CoverageProductLoggingFields.DATE));

        if (log.getBelongsToField(CoverageProductLoggingFields.DELIVERY) != null) {
            parametersLogg.put("delivery_id", log.getBelongsToField(CoverageProductLoggingFields.DELIVERY).getId());
        } else {
            parametersLogg.put("delivery_id", null);
        }

        if (log.getBelongsToField(CoverageProductLoggingFields.ORDER) != null) {
            parametersLogg.put("order_id", log.getBelongsToField(CoverageProductLoggingFields.ORDER).getId());
        } else {
            parametersLogg.put("order_id", null);
        }

        if (log.getBelongsToField(CoverageProductLoggingFields.OPERATION) != null) {
            parametersLogg.put("operation_id", log.getBelongsToField(CoverageProductLoggingFields.OPERATION).getId());
        } else {
            parametersLogg.put("operation_id", null);

        }

        parametersLogg.put("reservemissingquantity", log.getDecimalField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY));
        parametersLogg.put("changes", log.getDecimalField(CoverageProductLoggingFields.CHANGES));
        parametersLogg.put("eventtype", log.getStringField(CoverageProductLoggingFields.EVENT_TYPE));
        parametersLogg.put("state", log.getStringField(CoverageProductLoggingFields.STATE));
        parametersLogg.put("warehouseNumber", log.getStringField(CoverageProductLoggingFields.WAREHOUSE_NUMBER));
        parametersLogg.put("deliveryNumberExternal", log.getStringField("deliveryNumberExternal"));

        SqlParameterSource nParameters = new MapSqlParameterSource(parametersLogg);

        jdbcTemplate.update(sqlLog, nParameters);
    }

    private void saveCoverageProduct(final Entity materialRequirementCoverage, final Entity coverageProduct) {
        String sql = "INSERT INTO ordersupplies_coverageproduct "
                + "(materialrequirementcoverage_id, product_id, lackfromdate, demandquantity, coveredquantity, "
                + "reservemissingquantity, deliveredquantity, locationsquantity, state, productnumber, productname, "
                + "productunit, produceQuantity, fromSelectedOrder, company_id, price) "
                + "VALUES (:materialrequirementcoverage_id, :product_id, :lackfromdate, :demandquantity, :coveredquantity, "
                + ":reservemissingquantity, :deliveredquantity, :locationsquantity, :state, :productnumber, :productname, "
                + ":productunit, :produceQuantity, :fromSelectedOrder, :company_id, :price)";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("materialrequirementcoverage_id", materialRequirementCoverage.getId());
        parameters.put("product_id", coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT).getId());

        Entity company = coverageProduct.getBelongsToField(CoverageProductFields.COMPANY);

        if (company != null) {
            parameters.put("company_id", company.getId());
        } else {
            parameters.put("company_id", null);
        }

        parameters.put("lackfromdate", coverageProduct.getDateField(CoverageProductFields.LACK_FROM_DATE));
        parameters.put("demandquantity", coverageProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY));
        parameters.put("coveredquantity", coverageProduct.getDecimalField(CoverageProductFields.COVERED_QUANTITY));
        parameters.put("reservemissingquantity", coverageProduct.getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY));
        parameters.put("deliveredquantity", coverageProduct.getDecimalField(CoverageProductFields.DELIVERED_QUANTITY));
        parameters.put("locationsquantity", coverageProduct.getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY));
        parameters.put("produceQuantity", coverageProduct.getDecimalField(CoverageProductFields.PRODUCE_QUANTITY));
        parameters.put("state", coverageProduct.getStringField(CoverageProductFields.STATE));
        parameters.put("productnumber",
                coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT).getStringField(ProductFields.NUMBER));
        parameters.put("productname",
                coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT).getStringField(ProductFields.NAME));
        parameters.put("productunit",
                coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT).getStringField(ProductFields.UNIT));
        parameters.put("fromSelectedOrder", coverageProduct.getBooleanField(CoverageProductFields.FROM_SELECTED_ORDER));

        parameters.put("price", coverageProduct.getDecimalField(CoverageProductFields.PRICE));

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

        jdbcTemplate.update(sql, namedParameters);
    }

    // Do not remove, around by aspect
    public void additionalProcessProductCoverage(final Entity materialRequirementCoverage,
                                                 final Map<Long, Entity> productAndCoverageProducts) {
        List<Entity> selectedOrders = materialRequirementCoverage.getHasManyField("coverageOrders");
        if (!selectedOrders.isEmpty()) {
            List<Number> orderIds = getIdsFromCoverageOrders(selectedOrders);

            String sql = "";

            sql = "SELECT distinct registry.productId AS productId FROM #orderSupplies_productionCountingQuantityInput AS registry "
                    + "WHERE registry.orderId IN :ids AND eventType IN ('04orderInput','03operationInput')";
            List<Entity> regs = getCoverageRegisterDD().find(sql)
                    .setParameterList("ids", orderIds.stream().map(Number::intValue).collect(Collectors.toList())).list().getEntities();

            List<Long> pids = getIdsFromRegisterProduct(regs);

            for (Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
                Entity addedCoverageProduct = productAndCoverageProduct.getValue();

                if (pids.contains(productAndCoverageProduct.getKey())) {
                    addedCoverageProduct.setField(CoverageProductFields.FROM_SELECTED_ORDER, true);
                } else {
                    addedCoverageProduct.setField(CoverageProductFields.FROM_SELECTED_ORDER, false);
                }
            }
        }

    }

    private List<Number> getIdsFromCoverageOrders(final List<Entity> selectedOrders) {
        return selectedOrders.stream().map(Entity::getId).collect(Collectors.toList());
    }

    private List<Long> getIdsFromRegisterProduct(List<Entity> registerProducts) {

        return registerProducts.stream().map(p -> ((Number) p.getField("productId")).longValue()).collect(Collectors.toList());
    }

    private void fillFromProductionCounting(final Map<Long, Entity> productAndCoverageProducts, Entity assignedOrder,
                                            final Date coverageToDate, final Date actualDate,
                                            final List<Entity> orderStates) {
        List<String> states = Lists.newArrayList();

        if (!orderStates.isEmpty()) {
            states = orderStates.stream().map(order -> order.getStringField(CoverageOrderStateFields.STATE))
                    .collect(Collectors.toList());
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT registry FROM #orderSupplies_productionCountingQuantityInput AS registry ");

        query.append("WHERE 1=1 AND registry.startDate <= :dateTo ");

        boolean appendOrderId = false;

        if (!states.isEmpty()) {
            if (Objects.nonNull(assignedOrder)) {
                Optional<Entity> maybeState = orderStates
                        .stream()
                        .filter(state -> state.getStringField(CoverageOrderStateFields.STATE).equals(
                                assignedOrder.getStringField(OrderFields.STATE))).findAny();
                if (!maybeState.isPresent()) {
                    query.append("AND (orderState IN (:states) OR orderId = :orderId) ");
                    appendOrderId = true;
                } else {
                    query.append("AND orderState IN (:states)");
                }
            } else {
                query.append("AND orderState IN (:states)");
            }

        }

        SearchQueryBuilder queryBuilder = productionCountingQuantityInputDD().find(query.toString()).setParameter("dateTo",
                coverageToDate);

        if (!states.isEmpty()) {
            queryBuilder.setParameterList("states", states);
        }

        if (appendOrderId) {
            queryBuilder.setLong("orderId", assignedOrder.getId());
        }

        List<Entity> regs = queryBuilder.list().getEntities();
        Entity parameter = parameterService.getParameter();
        String priceField = "nominalCost";
        if (!Strings.isNullOrEmpty(parameter.getStringField(CostCalculationFields.MATERIAL_COSTS_USED))) {
            MaterialCostsUsed currentCost = MaterialCostsUsed.parseString(parameter.getStringField(CostCalculationFields.MATERIAL_COSTS_USED));
            switch (currentCost) {
                case AVERAGE:
                    priceField = "averageCost";
                    break;
                case LAST_PURCHASE:
                    priceField = "lastPurchaseCost";
                    break;
                case AVERAGE_OFFER_COST:
                    priceField = "averageOfferCost";
                    break;
                case LAST_OFFER_COST:
                    priceField = "lastOfferCost";
                    break;
                case OFFER_COST_OR_LAST_PURCHASE:
                    priceField = "offerCostOrLastPurchase";
            }
        }

        for (Entity reg : regs) {
            if (BigDecimal.ZERO.compareTo(reg.getDecimalField(CoverageRegisterFields.QUANTITY)) < 0) {
                Entity coverageProductLogging = materialRequirementCoverageHelper.createCoverageProductLoggingForOrder(reg,
                        actualDate);

                BigDecimal price = BigDecimal.ZERO;
                if (!Strings.isNullOrEmpty(priceField)) {
                    price = reg.getDecimalField(priceField);
                }


                materialRequirementCoverageHelper.fillCoverageProductForOrder(productAndCoverageProducts,
                        reg.getIntegerField("productId"), reg.getStringField("productType"), price, coverageProductLogging);
            }
        }
    }

    private void estimateProductDeliveriesInTime(final Entity materialRequirementCoverage,
                                                 final Map<Long, Entity> productAndCoverageProducts,
                                                 final List<Entity> includedDeliveries, final Date actualDate,
                                                 final Date coverageToDate,
                                                 final String includeInCalculationDeliveries) {
        for (Entity delivery : includedDeliveries) {
            Date coverageDate = getCoverageProductLoggingDateForDelivery(delivery, actualDate);

            List<Entity> deliveryProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

            for (Entity deliveryProduct : deliveryProducts) {
                estimateProductDelivery(productAndCoverageProducts, new CoverageProductForDelivery(coverageDate, delivery,
                        deliveryProduct));
            }
        }
    }

    private void estimateProductDelivery(final Map<Long, Entity> productAndCoverageProducts,
                                         final CoverageProductForDelivery coverageProductForDelivery) {
        if (productAndCoverageProducts.containsKey(coverageProductForDelivery.getProduct().getId())) {
            BigDecimal quantity = coverageProductForDelivery.getDeliveryQuantity();

            coverageProductForDelivery.setQuantity(quantity);

            Entity coverageProductLogging = createCoverageProductLoggingForDelivery(coverageProductForDelivery);

            fillCoverageProductForDelivery(productAndCoverageProducts, coverageProductForDelivery.getProduct(),
                    coverageProductLogging);
        }
    }

    private Date getCoverageProductLoggingDateForDelivery(final Entity delivery, final Date actualDate) {
        Date coverageDate;

        Date deliveryDate = delivery.getDateField(DeliveryFields.DELIVERY_DATE);

        if (deliveryDate.before(actualDate)) {
            coverageDate = new DateTime(actualDate).plusSeconds(2).toDate();
        } else {
            coverageDate = deliveryDate;
        }

        return coverageDate;
    }

    private Entity createCoverageProductLoggingForDelivery(
            final CoverageProductForDelivery coverageProductForDelivery) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE, coverageProductForDelivery.getCoverageDate());
        coverageProductLogging.setField(CoverageProductLoggingFields.DELIVERY, coverageProductForDelivery.getDelivery());
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(coverageProductForDelivery.getQuantity()));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE,
                CoverageProductLoggingEventType.DELIVERY.getStringValue());
        coverageProductLogging.setField(CoverageProductLoggingFields.STATE, CoverageProductLoggingState.COVERED.getStringValue());

        return coverageProductLogging;
    }

    private void fillCoverageProductForDelivery(final Map<Long, Entity> productAndCoverageProducts,
                                                final Entity product,
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
        Entity coverageProduct = orderSuppliesService.getCoverageProductDD().create();

        coverageProduct.setField(CoverageProductFields.PRODUCT, product);
        coverageProduct.setField(CoverageProductFields.DELIVERED_QUANTITY, numberService
                .setScaleWithDefaultMathContext(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES)));
        coverageProduct.setField(CoverageProductFields.STATE, CoverageProductState.COVERED.getStringValue());
        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, Lists.newArrayList(coverageProductLogging));

        productAndCoverageProducts.put(product.getId(), coverageProduct);
    }

    private void updateCoverageProductForDelivery(final Map<Long, Entity> productAndCoverageProducts,
                                                  final Entity product,
                                                  final Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(product.getId());

        BigDecimal deliveredQuantity = BigDecimalUtils.convertNullToZero(addedCoverageProduct
                .getDecimalField(CoverageProductFields.DELIVERED_QUANTITY));

        deliveredQuantity = deliveredQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists.newArrayList(addedCoverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.DELIVERED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(deliveredQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);

        productAndCoverageProducts.put(product.getId(), addedCoverageProduct);
    }

    private void estimateProductLocationsInTime(final Entity materialRequirementCoverage,
                                                final Map<Long, Entity> productAndCoverageProducts,
                                                final List<Entity> coverageLocations, final Date actualDate) {
        for (Entity coverageLocation : coverageLocations) {
            Entity location = coverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);

            String sql = "SELECT resource.product.id AS product, SUM(resource.quantity) AS quantity "
                    + "FROM #materialFlowResources_resource AS resource "
                    + "WHERE resource.location.id = :locationId GROUP BY resource.product.id";

            List<Entity> resources = getResourceDD().find(sql).setParameter("locationId", location.getId()).list().getEntities();

            Map<Long, BigDecimal> map = resources.stream().collect(
                    Collectors.toMap(res -> (Long) res.getField("product"), res -> res.getDecimalField("quantity")));

            for (Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
                Entity addedCoverageProduct = productAndCoverageProduct.getValue();

                BigDecimal locationsQuantity = BigDecimalUtils.convertNullToZero(map.get(productAndCoverageProduct.getKey()));
                Entity coverageProductLogging = createCoverageProductLoggingForLocations(location, actualDate, locationsQuantity);
                List<Entity> coverageProductLoggings = Lists.newArrayList(addedCoverageProduct
                        .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
                coverageProductLoggings.add(coverageProductLogging);
                BigDecimal lQuantity = BigDecimalUtils.convertNullToZero(addedCoverageProduct
                        .getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY));

                lQuantity = lQuantity.add(locationsQuantity, numberService.getMathContext());
                addedCoverageProduct.setField(CoverageProductFields.LOCATIONS_QUANTITY, lQuantity);
                addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);
            }
        }
    }

    private Entity createCoverageProductLoggingForLocations(final Entity location, final Date actualDate,
                                                            final BigDecimal locationsQuantity) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE, actualDate);
        coverageProductLogging.setField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY,
                numberService.setScaleWithDefaultMathContext(locationsQuantity));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE,
                CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue());
        coverageProductLogging.setField(CoverageProductLoggingFields.WAREHOUSE_NUMBER,
                location.getStringField(LocationFields.NUMBER));

        return coverageProductLogging;
    }

    private void fillCoverageProductSupplier(final Map<Long, Entity> productAndCoverageProducts) {
        productAndCoverageProducts.values().forEach(
                coverageProduct -> deliveriesService.getDefaultSupplierWithIntegration(
                        coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT).getId()).ifPresent(
                        supplier -> coverageProduct.setField(CoverageProductFields.COMPANY, supplier)));
    }

    private void fillCoverageProductStatesAndQuantities(final Map<Long, Entity> productAndCoverageProducts) {
        for (Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
            Entity coverageProduct = productAndCoverageProduct.getValue();

            fillCoverageProductLoggingsStates(coverageProduct);
            fillCoverageProductQuantities(coverageProduct);
        }
    }

    private void fillCoverageProductQuantities(final Entity coverageProduct) {
        BigDecimal demandQuantity = BigDecimalUtils.convertNullToZero(coverageProduct
                .getDecimalField(CoverageProductFields.DEMAND_QUANTITY));
        BigDecimal deliveredQuantity = BigDecimalUtils.convertNullToZero(coverageProduct
                .getDecimalField(CoverageProductFields.DELIVERED_QUANTITY));
        BigDecimal locationsQuantity = BigDecimalUtils.convertNullToZero(coverageProduct
                .getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY));
        BigDecimal produceQuantity = BigDecimalUtils.convertNullToZero(coverageProduct
                .getDecimalField(CoverageProductFields.PRODUCE_QUANTITY));

        BigDecimal coveredQuantity = deliveredQuantity.add(locationsQuantity, numberService.getMathContext());
        coveredQuantity = coveredQuantity.add(produceQuantity, numberService.getMathContext());

        BigDecimal reserveMissingQuantity = coveredQuantity.subtract(demandQuantity, numberService.getMathContext());

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
        coverageProductLoggings.sort((entity1, entity2) -> ComparisonChain.start()
                .compare(entity1.getDateField(CoverageProductLoggingFields.DATE),
                        entity2.getDateField(CoverageProductLoggingFields.DATE))
                .compare(entity2.getStringField(CoverageProductLoggingFields.EVENT_TYPE),
                        entity1.getStringField(CoverageProductLoggingFields.EVENT_TYPE))
                .compare(entity2.getLongField(CoverageProductLoggingFields.ORDER),
                        entity1.getLongField(CoverageProductLoggingFields.ORDER), Ordering.natural().nullsFirst())
                .compare(entity2.getLongField(CoverageProductLoggingFields.OPERATION),
                        entity1.getLongField(CoverageProductLoggingFields.OPERATION), Ordering.natural().nullsFirst())
                .result());

        BigDecimal reserveMissingQuantity = BigDecimal.ZERO;

        for (Entity coverageProductLogging : coverageProductLoggings) {
            String eventType = coverageProductLogging.getStringField(CoverageProductLoggingFields.EVENT_TYPE);

            if (CoverageProductLoggingEventType.WAREHOUSE_STATE.getStringValue().equals(eventType)) {
                BigDecimal quantity = coverageProductLogging
                        .getDecimalField(CoverageProductLoggingFields.RESERVE_MISSING_QUANTITY);

                reserveMissingQuantity = reserveMissingQuantity.add(quantity, numberService.getMathContext());
            } else if (CoverageProductLoggingEventType.DELIVERY.getStringValue().equals(eventType)
                    || CoverageProductLoggingEventType.ORDER_OUTPUT.getStringValue().equals(eventType)) {
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

        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);
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

    private List<Entity> filterCoverageProducts(final Map<Long, Entity> productAndCoverageProducts,
                                                final String coverageType) {
        return filterCoverageProductsWithCoverageType(getCoverageProducts(productAndCoverageProducts), coverageType);
    }

    private List<Entity> getCoverageProducts(final Map<Long, Entity> productAndCoverageProducts) {

        return Lists.newArrayList(productAndCoverageProducts.values());
    }

    private List<Entity> filterCoverageProductsWithCoverageType(final List<Entity> coverageProducts,
                                                                final String coverageType) {
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

    private List<Entity> getDeliveriesFromDB(final Date coverageToDate, final String includeInCalculationDeliveries,
                                             List<Entity> coverageLocations) {
        List<String> states = IncludeInCalculationDeliveries.getStates(includeInCalculationDeliveries);

        SearchCriteriaBuilder scb = getDeliveryDD()
                .find()
                .add(SearchRestrictions.le(DeliveryFields.DELIVERY_DATE, coverageToDate))
                .add(SearchRestrictions.in(DeliveryFields.STATE, states))
                .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true));
        if (!coverageLocations.isEmpty()) {
            scb = scb.createAlias(DeliveryFields.LOCATION, DeliveryFields.LOCATION, JoinType.LEFT).add(
                    SearchRestrictions.in(
                            DeliveryFields.LOCATION + L_DOT + L_ID,
                            coverageLocations.stream()
                                    .map(cl -> cl.getBelongsToField(CoverageLocationFields.LOCATION).getId())
                                    .collect(Collectors.toList())));
        }
        return scb
                .list().getEntities();


    }

    private DataDefinition getCoverageRegisterDD() {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT);

    }

    private DataDefinition productionCountingQuantityInputDD() {
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, "productionCountingQuantityInput");
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
    }

}
