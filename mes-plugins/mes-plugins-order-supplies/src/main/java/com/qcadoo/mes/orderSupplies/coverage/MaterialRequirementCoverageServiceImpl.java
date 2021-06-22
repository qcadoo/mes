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
package com.qcadoo.mes.orderSupplies.coverage;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageOrderStateFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingEventType;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingState;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductState;
import com.qcadoo.mes.orderSupplies.constants.CoverageRegisterFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageType;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orderSupplies.constants.ProductType;
import com.qcadoo.mes.orderSupplies.register.RegisterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialRequirementCoverageServiceImpl implements MaterialRequirementCoverageService {

    private static final Logger LOG = LoggerFactory.getLogger(MaterialRequirementCoverageServiceImpl.class);

    private static final String L_ORDER = "order";

    private static final String L_PRODUCT_TYPE = "productType";

    private static final String L_PLANNED_QUANTITY = "planedQuantity";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private MaterialRequirementCoverageHelper materialRequirementCoverageHelper;

    @Transactional
    @Override
    public void estimateProductCoverageInTime(final Entity materialRequirementCoverage) {
        LOG.info("Start generation material requirement - id : " + materialRequirementCoverage.getId());

        boolean coverageBasedOnProductionCounting = parameterService.getParameter().getBooleanField(
                "coverageBasedOnProductionCounting");

        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);
        Date actualDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.ACTUAL_DATE);

        String coverageType = materialRequirementCoverage.getStringField(MaterialRequirementCoverageFields.COVERAGE_TYPE);

        boolean includeDraftDeliveries = materialRequirementCoverage
                .getBooleanField(MaterialRequirementCoverageFields.INCLUDE_DRAFT_DELIVERIES);

        List<Entity> coverageLocations = materialRequirementCoverage
                .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS);

        List<Entity> includedDeliveries = getDeliveriesFromDB(coverageToDate, includeDraftDeliveries);

        Map<Long, Entity> productAndCoverageProducts = Maps.newHashMap();

        List<Entity> orderStates = materialRequirementCoverage
                .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDER_STATES);
        List<Entity> selectedOrders = materialRequirementCoverage.getHasManyField("coverageOrders");

        if (!selectedOrders.isEmpty()) {
            orderStates = Collections.emptyList();
        }

        if (coverageBasedOnProductionCounting) {
            Entity assignedOrder = materialRequirementCoverage.getBelongsToField(L_ORDER);
            fillFromProductionCounting(productAndCoverageProducts, assignedOrder, coverageToDate, actualDate, orderStates);
        } else {
            fillFromRegistry(productAndCoverageProducts, coverageToDate, actualDate, orderStates);

            Entity assignedOrder = materialRequirementCoverage.getBelongsToField(L_ORDER);

            if (!orderStates.isEmpty() && Objects.nonNull(assignedOrder)) {
                Optional<Entity> maybeState = orderStates
                        .stream()
                        .filter(state -> state.getStringField(CoverageOrderStateFields.STATE).equals(
                                assignedOrder.getStringField(OrderFields.STATE))).findAny();
                if (!maybeState.isPresent()) {
                    fillFromRegistryAssignedOrder(productAndCoverageProducts, assignedOrder, coverageToDate, actualDate);
                }
            }

        }

        estimateProductLocationsInTime(materialRequirementCoverage, productAndCoverageProducts, coverageLocations, actualDate);

        estimateProductDeliveriesInTime(materialRequirementCoverage, productAndCoverageProducts, includedDeliveries, actualDate,
                coverageToDate, includeDraftDeliveries);

        if (coverageBasedOnProductionCounting) {
            estimateProductProducedInTimeFromPQ(productAndCoverageProducts, coverageToDate, actualDate, orderStates);
        } else {
            estimateProductProducedInTime(productAndCoverageProducts, coverageToDate, actualDate, orderStates);
        }

        additionalProcessProductCoverage(materialRequirementCoverage, productAndCoverageProducts);

        fillCoverageProductStatesAndQuantities(productAndCoverageProducts);

        fillCoverageProductSupplier(productAndCoverageProducts);

        materialRequirementCoverage.getDataDefinition().save(materialRequirementCoverage);

        saveCoverage(materialRequirementCoverage, filterCoverageProducts(productAndCoverageProducts, coverageType));

        LOG.info("Finish generation material requirement - id : " + materialRequirementCoverage.getId());
    }

    private void estimateProductProducedInTimeFromPQ(final Map<Long, Entity> productAndCoverageProducts,
            final Date coverageToDate, final Date actualDate, final List<Entity> orderStates) {

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
                Entity coverageProductLogging = materialRequirementCoverageHelper.createProductLoggingForOrderProduced(reg, actualDate, coverageToDate);

                materialRequirementCoverageHelper.fillCoverageProductForOrderProduced(productAndCoverageProducts, Long.valueOf(reg.getIntegerField("productId")),
                        coverageProductLogging);
            }
        }
    }

    private void estimateProductProducedInTime(final Map<Long, Entity> productAndCoverageProducts, final Date coverageToDate,
            final Date actualDate, final List<Entity> orderStates) {
        List<String> states = Lists.newArrayList();

        if (orderStates != null && !orderStates.isEmpty()) {
            states = orderStates.stream().map(o -> o.getStringField(CoverageOrderStateFields.STATE)).collect(Collectors.toList());
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT registry FROM #orderSupplies_coverageRegister AS registry ");

        if (!states.isEmpty()) {
            query.append("JOIN registry.order AS ord ");
        }

        query.append("WHERE registry.date <= :dateTo AND eventType IN ('05orderOutput') ");

        if (!states.isEmpty()) {
            query.append("AND ord.state IN (:states)");
        }

        SearchQueryBuilder queryBuilder = getCoverageRegisterDD().find(query.toString()).setParameter("dateTo", coverageToDate);

        if (!states.isEmpty()) {
            queryBuilder.setParameterList("states", states);
        }

        List<Entity> regs = queryBuilder.list().getEntities();

        for (Entity reg : regs) {
            if (BigDecimal.ZERO.compareTo(reg.getDecimalField(CoverageRegisterFields.QUANTITY)) < 0) {
                Entity coverageProductLogging = createProductLoggingForOrderProduced(reg, actualDate, coverageToDate);

                fillCoverageProductForOrderProduced(productAndCoverageProducts, reg.getBelongsToField("product"),
                        coverageProductLogging);
            }
        }
    }

    private void fillCoverageProductForOrderProduced(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
            final Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(product.getId())) {
                updateCoverageProductForOrderProduced(productAndCoverageProducts, product, coverageProductLogging);
            }
        }
    }

    private void updateCoverageProductForOrderProduced(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
            final Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(product.getId());

        BigDecimal demandQuantity = BigDecimalUtils.convertNullToZero(addedCoverageProduct
                .getDecimalField(CoverageProductFields.PRODUCE_QUANTITY));

        demandQuantity = demandQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists.newArrayList(addedCoverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.PRODUCE_QUANTITY,
                numberService.setScaleWithDefaultMathContext(demandQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);

        productAndCoverageProducts.put(product.getId(), addedCoverageProduct);
    }

    private Entity createProductLoggingForOrderProduced(final Entity registerEntry, final Date actualDate,
            final Date coverageToDate) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging
                .setField(
                        CoverageProductLoggingFields.DATE,
                        getCoverageProductLoggingDateForOrderProduced(registerEntry.getDateField(CoverageRegisterFields.DATE),
                                actualDate));
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER, registerEntry.getBelongsToField("order"));
        coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION, registerEntry.getBelongsToField("operation"));
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(registerEntry.getDecimalField("quantity")));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, registerEntry.getStringField("eventType"));

        return coverageProductLogging;
    }

    private Date getCoverageProductLoggingDateForOrderProduced(final Date finishDate, final Date actualDate) {
        Date coverageDate = null;

        if (finishDate.before(actualDate)) {
            coverageDate = new DateTime(actualDate).plusSeconds(1).toDate();
        } else {
            coverageDate = finishDate;
        }

        return coverageDate;
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
                + "productunit, produceQuantity, fromSelectedOrder, allProductsType, company_id) "
                + "VALUES (:materialrequirementcoverage_id, :product_id, :lackfromdate, :demandquantity, :coveredquantity, "
                + ":reservemissingquantity, :deliveredquantity, :locationsquantity, :state, :productnumber, :productname, "
                + ":productunit, :produceQuantity, :fromSelectedOrder, :allProductsType, :company_id)";

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
        parameters.put("allProductsType", coverageProduct.getStringField(CoverageProductFields.ALL_PRODUCTS_TYPE));

        SqlParameterSource namedParameters = new MapSqlParameterSource(parameters);

        jdbcTemplate.update(sql, namedParameters);
    }

    // Do not remove, around by aspect
    public void additionalProcessProductCoverage(final Entity materialRequirementCoverage,
            final Map<Long, Entity> productAndCoverageProducts) {
        List<Entity> selectedOrders = materialRequirementCoverage.getHasManyField("coverageOrders");

        if (!selectedOrders.isEmpty()) {
            for (Entity order : selectedOrders) {
                List<Entity> entries = registerService.getRegisterEntriesForOrder(order);

                for (Entity entry : entries) {
                    Entity product = entry.getBelongsToField(CoverageRegisterFields.PRODUCT);

                    Entity coverageProduct = productAndCoverageProducts.get(product.getId());
                    if (coverageProduct == null) {
                        continue;
                    }
                    if (checkIfProductsAreSame(order, product.getId())) {
                        coverageProduct.setField(L_PRODUCT_TYPE, null);
                        continue;
                    }

                    List<Entity> technologiesForProduct = getTechnologyDD().find()
                            .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product))
                            .add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                            .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.ACCEPTED.getStringValue()))
                            .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).list().getEntities();

                    if (technologiesForProduct.isEmpty()) {
                        coverageProduct.setField(L_PRODUCT_TYPE, ProductType.COMPONENT.getStringValue());
                        coverageProduct.setField(L_PLANNED_QUANTITY,
                                entry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES));

                    } else {
                        coverageProduct.setField(L_PRODUCT_TYPE, ProductType.INTERMEDIATE.getStringValue());
                        coverageProduct.setField(L_PLANNED_QUANTITY,
                                entry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES));
                    }
                }
            }

            List<Number> orderIds = getIdsFromCoverageOrders(selectedOrders);

            boolean coverageBasedOnProductionCounting = parameterService.getParameter().getBooleanField(
                    "coverageBasedOnProductionCounting");

            String sql = "";

            if (coverageBasedOnProductionCounting) {
                sql = "SELECT distinct registry.productId AS productId FROM #orderSupplies_productionCountingQuantityInput AS registry "
                        + "WHERE registry.orderId IN :ids AND eventType IN ('04orderInput','03operationInput')";
                List<Entity> regs = getCoverageRegisterDD().find(sql).setParameterList("ids", orderIds.stream().map(x -> x.intValue()).collect(
                        Collectors.toList())).list().getEntities();

                List<Long> pids = getIdsFromRegisterProduct(regs);

                for (Entry<Long, Entity> productAndCoverageProduct : productAndCoverageProducts.entrySet()) {
                    Entity addedCoverageProduct = productAndCoverageProduct.getValue();

                    if (pids.contains(productAndCoverageProduct.getKey())) {
                        addedCoverageProduct.setField(CoverageProductFields.FROM_SELECTED_ORDER, true);
                    } else {
                        addedCoverageProduct.setField(CoverageProductFields.FROM_SELECTED_ORDER, false);
                    }
                }
            } else {
                sql = "SELECT distinct registry.product.id AS productId FROM #orderSupplies_coverageRegister AS registry "
                        + "WHERE registry.order.id IN :ids AND eventType IN ('04orderInput','03operationInput')";
                List<Entity> regs = getCoverageRegisterDD().find(sql).setParameterList("ids", orderIds.stream().map(x -> x.longValue()).collect(
                        Collectors.toList())).list().getEntities();

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
    }

    private List<Number> getIdsFromCoverageOrders(final List<Entity> selectedOrders) {
        return selectedOrders.stream().map(Entity::getId).collect(Collectors.toList());
    }

    private List<Long> getIdsFromRegisterProduct(List<Entity> registerProducts) {

        return registerProducts.stream().map(p -> ((Number) p.getField("productId")).longValue()).collect(Collectors.toList());
    }

    private void fillFromProductionCounting(final Map<Long, Entity> productAndCoverageProducts, Entity assignedOrder, final Date coverageToDate,
            final Date actualDate, final List<Entity> orderStates) {
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
            if(Objects.nonNull(assignedOrder)) {
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

        SearchQueryBuilder queryBuilder = productionCountingQuantityInputDD().find(query.toString()).setParameter("dateTo", coverageToDate);

        if (!states.isEmpty()) {
            queryBuilder.setParameterList("states", states);
        }

        if(appendOrderId) {
            queryBuilder.setLong("orderId", assignedOrder.getId());
        }

        List<Entity> regs = queryBuilder.list().getEntities();

        for (Entity reg : regs) {
            if (BigDecimal.ZERO.compareTo(reg.getDecimalField(CoverageRegisterFields.QUANTITY)) < 0) {
                Entity coverageProductLogging = materialRequirementCoverageHelper.createCoverageProductLoggingForOrder(reg,
                        actualDate);

                materialRequirementCoverageHelper.fillCoverageProductForOrder(productAndCoverageProducts,
                        reg.getIntegerField("productId"), reg.getStringField("productType"), coverageProductLogging);
            }
        }
    }

    private void fillFromRegistry(final Map<Long, Entity> productAndCoverageProducts, final Date coverageToDate,
            final Date actualDate, final List<Entity> orderStates) {
        List<String> states = Lists.newArrayList();

        if (!orderStates.isEmpty()) {
            states = orderStates.stream().map(order -> order.getStringField(CoverageOrderStateFields.STATE))
                    .collect(Collectors.toList());
        }

        StringBuilder query = new StringBuilder();
        query.append("SELECT registry FROM #orderSupplies_coverageRegister AS registry ");

        if (!states.isEmpty()) {
            query.append("JOIN registry.order AS ord ");
        }

        query.append("WHERE registry.date <= :dateTo AND eventType IN ('04orderInput','03operationInput') ");

        if (!states.isEmpty()) {
            query.append("AND ord.state IN (:states)");
        }

        SearchQueryBuilder queryBuilder = getCoverageRegisterDD().find(query.toString()).setParameter("dateTo", coverageToDate);

        if (!states.isEmpty()) {
            queryBuilder.setParameterList("states", states);
        }

        List<Entity> regs = queryBuilder.list().getEntities();

        for (Entity reg : regs) {
            if (BigDecimal.ZERO.compareTo(reg.getDecimalField(CoverageRegisterFields.QUANTITY)) < 0) {
                Entity coverageProductLogging = createCoverageProductLoggingForOrder(reg, actualDate);

                fillCoverageProductForOrder(productAndCoverageProducts, reg.getBelongsToField("product"),
                        reg.getStringField("productType"), coverageProductLogging);
            }
        }
    }

    private void fillFromRegistryAssignedOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity assignedOrder,
            final Date coverageToDate, final Date actualDate) {
        String query = "SELECT registry FROM #orderSupplies_coverageRegister AS registry "
                + "WHERE registry.date <= :dateTo AND eventType IN ('04orderInput','03operationInput') "
                + "AND order_id = :orderId ";
        SearchQueryBuilder queryBuilder = getCoverageRegisterDD().find(query).setParameter("dateTo", coverageToDate);

        queryBuilder.setParameter("orderId", assignedOrder.getId());

        List<Entity> regs = queryBuilder.list().getEntities();

        for (Entity reg : regs) {
            if (BigDecimal.ZERO.compareTo(reg.getDecimalField(CoverageRegisterFields.QUANTITY)) < 0) {
                Entity coverageProductLogging = createCoverageProductLoggingForOrder(reg, actualDate);
                fillCoverageProductForOrder(productAndCoverageProducts, reg.getBelongsToField("product"),
                        reg.getStringField("productType"), coverageProductLogging);
            }
        }
    }

    private Entity createCoverageProductLoggingForOrder(final Entity registerEntry, final Date actualDate) {
        Entity coverageProductLogging = orderSuppliesService.getCoverageProductLoggingDD().create();

        coverageProductLogging.setField(CoverageProductLoggingFields.DATE,
                getCoverageProductLoggingDateForOrder(registerEntry, actualDate));
        coverageProductLogging.setField(CoverageProductLoggingFields.ORDER, registerEntry.getBelongsToField("order"));
        coverageProductLogging.setField(CoverageProductLoggingFields.OPERATION, registerEntry.getBelongsToField("operation"));
        coverageProductLogging.setField(CoverageProductLoggingFields.CHANGES,
                numberService.setScaleWithDefaultMathContext(registerEntry.getDecimalField("quantity")));
        coverageProductLogging.setField(CoverageProductLoggingFields.EVENT_TYPE, registerEntry.getStringField("eventType"));

        return coverageProductLogging;
    }

    private Date getCoverageProductLoggingDateForOrder(final Entity registerEntry, final Date actualDate) {
        Date startDate = registerEntry.getDateField("date");
        Date coverageDate;

        if (startDate.before(actualDate)) {
            coverageDate = new DateTime(actualDate).plusSeconds(3).toDate();
        } else {
            coverageDate = startDate;
        }

        return coverageDate;
    }

    private void estimateProductDeliveriesInTime(final Entity materialRequirementCoverage,
            final Map<Long, Entity> productAndCoverageProducts, final List<Entity> includedDeliveries, final Date actualDate,
            final Date coverageToDate, final Boolean includeDraftDeliveries) {
        for (Entity delivery : includedDeliveries) {
            Date coverageDate = getCoverageProductLoggingDateForDelivery(delivery, actualDate);

            List<Entity> deliveryProducts;

            if (DeliveryStateStringValues.RECEIVE_CONFIRM_WAITING.equals(delivery.getStringField(DeliveryFields.STATE))) {
                deliveryProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
            } else {
                deliveryProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);
            }

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

    private Entity createCoverageProductLoggingForDelivery(final CoverageProductForDelivery coverageProductForDelivery) {
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
        Entity coverageProduct = orderSuppliesService.getCoverageProductDD().create();

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

    private void fillCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
            final String productType, final Entity coverageProductLogging) {
        if (coverageProductLogging != null) {
            if (productAndCoverageProducts.containsKey(product.getId())) {
                updateCoverageProductForOrder(productAndCoverageProducts, product, productType, coverageProductLogging);
            } else {
                addCoverageProductForOrder(productAndCoverageProducts, product, productType, coverageProductLogging);
            }
        }
    }

    private void addCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
            final String productType, final Entity coverageProductLogging) {
        Entity coverageProduct = orderSuppliesService.getCoverageProductDD().create();

        coverageProduct.setField(CoverageProductFields.PRODUCT, product);
        coverageProduct.setField(CoverageProductFields.PRODUCT_TYPE, productType);
        coverageProduct.setField(CoverageProductFields.ALL_PRODUCTS_TYPE, productType);
        coverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY, numberService
                .setScaleWithDefaultMathContext(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES)));
        coverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, Lists.newArrayList(coverageProductLogging));

        productAndCoverageProducts.put(product.getId(), coverageProduct);
    }

    private void updateCoverageProductForOrder(final Map<Long, Entity> productAndCoverageProducts, final Entity product,
            final String productType, final Entity coverageProductLogging) {
        Entity addedCoverageProduct = productAndCoverageProducts.get(product.getId());

        BigDecimal demandQuantity = BigDecimalUtils.convertNullToZero(addedCoverageProduct
                .getDecimalField(CoverageProductFields.DEMAND_QUANTITY));

        demandQuantity = demandQuantity.add(coverageProductLogging.getDecimalField(CoverageProductLoggingFields.CHANGES),
                numberService.getMathContext());

        List<Entity> coverageProductLoggings = Lists.newArrayList(addedCoverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        coverageProductLoggings.add(coverageProductLogging);

        addedCoverageProduct.setField(CoverageProductFields.DEMAND_QUANTITY,
                numberService.setScaleWithDefaultMathContext(demandQuantity));
        addedCoverageProduct.setField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS, coverageProductLoggings);

        String types = addedCoverageProduct.getStringField(CoverageProductFields.ALL_PRODUCTS_TYPE);

        if (!types.contains(productType)) {
            addedCoverageProduct.setField(CoverageProductFields.PRODUCT_TYPE, productType);
            addedCoverageProduct.setField(CoverageProductFields.ALL_PRODUCTS_TYPE, "01component_02intermediate");
        }

        productAndCoverageProducts.put(product.getId(), addedCoverageProduct);
    }

    private void estimateProductLocationsInTime(final Entity materialRequirementCoverage,
            final Map<Long, Entity> productAndCoverageProducts, final List<Entity> coverageLocations, final Date actualDate) {
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
        productAndCoverageProducts
                .values()
                .stream()
                .filter(coverageProduct -> CoverageProductState.LACK.getStringValue().equals(
                        coverageProduct.getStringField(CoverageProductFields.STATE)))
                .forEach(
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
        List<Entity> coverageProductLoggings = Lists.newLinkedList(coverageProduct
                .getHasManyField(CoverageProductFields.COVERAGE_PRODUCT_LOGGINGS));
        Collections.sort(coverageProductLoggings, new Comparator<Entity>() {

            @Override
            public int compare(final Entity entity1, final Entity entity2) {
                return ComparisonChain
                        .start()
                        .compare(entity1.getDateField(CoverageProductLoggingFields.DATE),
                                entity2.getDateField(CoverageProductLoggingFields.DATE))
                        .compare(entity2.getStringField(CoverageProductLoggingFields.EVENT_TYPE),
                                entity1.getStringField(CoverageProductLoggingFields.EVENT_TYPE)).result();
            }
        });

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

    private List<Entity> getDeliveriesFromDB(final Date coverageToDate, final boolean includeDraftDeliveries) {
        if (includeDraftDeliveries) {
            return getDeliveryDD()
                    .find()
                    .add(SearchRestrictions.le(DeliveryFields.DELIVERY_DATE, coverageToDate))
                    .add(SearchRestrictions.or(SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.PREPARED),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.DURING_CORRECTION),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.APPROVED),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.RECEIVE_CONFIRM_WAITING)))
                    .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true)).list().getEntities();
        } else {
            return getDeliveryDD()
                    .find()
                    .add(SearchRestrictions.le(DeliveryFields.DELIVERY_DATE, coverageToDate))
                    .add(SearchRestrictions.or(SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.APPROVED),
                            SearchRestrictions.eq(DeliveryFields.STATE, DeliveryStateStringValues.RECEIVE_CONFIRM_WAITING)))
                    .add(SearchRestrictions.eq(DeliveryFields.ACTIVE, true)).list().getEntities();
        }
    }

    private boolean checkIfProductsAreSame(final Entity order, final Long product) {
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        if (orderProduct == null) {
            return false;
        } else {
            return product.equals(orderProduct.getId());
        }
    }

    private DataDefinition getCoverageRegisterDD() {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER);

    }
    private DataDefinition productionCountingQuantityInputDD() {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,"productionCountingQuantityInput");
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getDeliveryDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY);
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

}
