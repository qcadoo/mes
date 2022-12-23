package com.qcadoo.mes.orderSupplies.coverage.coverageAnalysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.orderSupplies.constants.CoverageAnalysisForOrderFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageDegreeStringValues;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoverageAnalysisForOrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void coverageAnalysis(Long coverageId) {
        Entity materialRequirementCoverage = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE).get(coverageId);

        List<Entity> selectedOrders = materialRequirementCoverage.getHasManyField("coverageOrders");



        List<CoverageOrderAnalysis> coverageOrderAnalysesEntries = Lists.newArrayList();

        Map<Long, List<ProductState>> productsStateByOrder = getProductStateByOrder(coverageId);
        Map<Long, List<ProductLogging>> inProductLoggingsByProduct = getProductLoggingByProduct(coverageId);

        for (Map.Entry<Long, List<ProductState>> entry : productsStateByOrder.entrySet()) {
            Long orderId = entry.getKey();
            List<ProductState> productsOrderState = entry.getValue();
            boolean allProductsCovered = productsOrderState.stream().allMatch(ps -> ps.getState().equals("01covered"));
            if (allProductsCovered) {
                ProductState productState = productsOrderState.get(0);
                CoverageOrderAnalysis coverageOrderAnalysis = createAnalysesEntry(materialRequirementCoverage.getId(),
                        orderId, productState.getStartDate(), CoverageDegreeStringValues.TOTAL);
                Date deliveryTime = getDeliveryTime(inProductLoggingsByProduct, productsOrderState);
                Date componentProductionTime = getComponentProductionTime(inProductLoggingsByProduct, productsOrderState);
                coverageOrderAnalysis.setDeliveryTime(deliveryTime);
                coverageOrderAnalysis.setComponentsProductionDate(componentProductionTime);
                coverageOrderAnalysis.setOrderedProductId(productState.getOrderedProductId());
                coverageOrderAnalysis.setPlannedQuantity(productState.getPlannedQuantity());
                coverageOrderAnalysesEntries.add(coverageOrderAnalysis);
            } else {
                ProductState productState = productsOrderState.get(0);

                boolean anyProductsCovered = productsOrderState.stream()
                        .anyMatch(ps -> ps.getState().equals("01covered"));
                CoverageOrderAnalysis coverageOrderAnalysis = createAnalysesEntry(materialRequirementCoverage.getId(), orderId, null,
                        anyProductsCovered ? CoverageDegreeStringValues.PARTIAL : CoverageDegreeStringValues.ZERO);

                Date coveredFromTheDay = getCoveredFromTheDay(inProductLoggingsByProduct, productsOrderState);
                coverageOrderAnalysis.setCoveredFromTheDay(coveredFromTheDay);
                Date deliveryTime = getDeliveryTime(inProductLoggingsByProduct, productsOrderState);
                Date componentProductionTime = getComponentProductionTime(inProductLoggingsByProduct, productsOrderState);
                coverageOrderAnalysis.setDeliveryTime(deliveryTime);
                coverageOrderAnalysis.setComponentsProductionDate(componentProductionTime);
                coverageOrderAnalysis.setOrderedProductId(productState.getOrderedProductId());
                coverageOrderAnalysis.setPlannedQuantity(productState.getPlannedQuantity());
                coverageOrderAnalysesEntries.add(coverageOrderAnalysis);
            }
        }
        materialRequirementCoverage.setField(MaterialRequirementCoverageFields.COVERAGE_ANALYSIS_FOR_ORDERS, Lists.newArrayList());
        materialRequirementCoverage = materialRequirementCoverage.getDataDefinition().fastSave(materialRequirementCoverage);
        if(!selectedOrders.isEmpty()) {
            List<Long> ids = selectedOrders.stream().map(Entity::getId).collect(Collectors.toList());
            coverageOrderAnalysesEntries = coverageOrderAnalysesEntries.stream().filter(coa -> ids.contains(coa.getOrderId())).collect(Collectors.toList());
        }
        for (CoverageOrderAnalysis coverageOrderAnalysis : coverageOrderAnalysesEntries) {
            Entity coa = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                    OrderSuppliesConstants.MODEL_COVERAGE_ANALYSIS_FOR_ORDER).create();
            coa.setField(CoverageAnalysisForOrderFields.MATERIAL_REQUIREMENT_COVERAGE, materialRequirementCoverage.getId());
            coa.setField(CoverageAnalysisForOrderFields.ORDER, coverageOrderAnalysis.getOrderId());
            coa.setField(CoverageAnalysisForOrderFields.COVERAGE_DEGREE, coverageOrderAnalysis.getCoverageDegree());
            coa.setField(CoverageAnalysisForOrderFields.COVERED_FROM_THE_DAY, coverageOrderAnalysis.getCoveredFromTheDay());
            coa.setField(CoverageAnalysisForOrderFields.DELIVERY_TIME, coverageOrderAnalysis.getDeliveryTime());
            coa.setField(CoverageAnalysisForOrderFields.COMPONENTS_PRODUCTION_DATE, coverageOrderAnalysis.getComponentsProductionDate());
            coa.setField(CoverageAnalysisForOrderFields.ORDERED_PRODUCT, coverageOrderAnalysis.getOrderedProductId());
            coa.setField(CoverageAnalysisForOrderFields.PLANNED_QUANTITY, coverageOrderAnalysis.getPlannedQuantity());
            coa = coa.getDataDefinition().fastSave(coa);
            coa.isValid();
        }
    }

    private Date getCoveredFromTheDay(Map<Long, List<ProductLogging>> inProductLoggingsByProduct, List<ProductState> productsOrderState) {
        Date coveredFromTheDay = null;
        for (ProductState productState : productsOrderState) {
            Date coveredFromTheDayForProduct = null;

            if (productState.getState().equals("01covered")) {
                coveredFromTheDayForProduct = productState.getStartDate();
            } else {
                coveredFromTheDayForProduct = null;

                List<ProductLogging> productsLogging = inProductLoggingsByProduct.get(productState.getProductId());
                if (Objects.isNull(productsLogging) || productsLogging.isEmpty()) {
                    return null;
                }
                productsLogging.sort(Comparator.comparing(ProductLogging::getDate));

                BigDecimal reserveMissingQuantity = productState.getReserveMissingQuantity();

                for (ProductLogging productLogging : productsLogging) {
                    reserveMissingQuantity = reserveMissingQuantity.add(productLogging.getChanges());
                    if (reserveMissingQuantity.compareTo(BigDecimal.ZERO) < 0) {
                        productLogging.setChanges(BigDecimal.ZERO);
                    } else {
                        productLogging.setChanges(reserveMissingQuantity);
                        if (Objects.isNull(coveredFromTheDayForProduct)) {
                            coveredFromTheDayForProduct = productLogging.getDate();
                        } else if (productLogging.getDate().after(coveredFromTheDayForProduct)) {
                            coveredFromTheDayForProduct = productLogging.getDate();
                        }
                    }
                }

            }
            if(Objects.isNull(coveredFromTheDayForProduct)) {
                return null;
            } else {
                if (Objects.isNull(coveredFromTheDay)) {
                    coveredFromTheDay = coveredFromTheDayForProduct;
                } else if (coveredFromTheDayForProduct.after(coveredFromTheDay)) {
                    coveredFromTheDay = coveredFromTheDayForProduct;
                }
            }
        }
        return coveredFromTheDay;
    }

    private Date getDeliveryTime(Map<Long, List<ProductLogging>> inProductLoggingsByProduct, List<ProductState> productsOrderState) {
        Date deliveryTime = null;
        for (ProductState productState : productsOrderState) {
            List<ProductLogging> productsLogging = inProductLoggingsByProduct.get(productState.getProductId());
            if (Objects.isNull(productsLogging) || productsLogging.isEmpty()) {
                continue;
            }
            productsLogging.sort(Comparator.comparing(ProductLogging::getDate));

            Optional<Date> productDate = productsLogging.stream().filter(pl -> pl.getType().equals("02delivery"))
                    .map(ProductLogging::getDate)
                    .max(Date::compareTo);
            if(productDate.isPresent()) {
                if(Objects.isNull(deliveryTime)) {
                    deliveryTime = productDate.get();
                } else if (productDate.get().after(deliveryTime)) {
                    deliveryTime = productDate.get();
                }
            }

        }
        return deliveryTime;
    }

    private Date getComponentProductionTime(Map<Long, List<ProductLogging>> inProductLoggingsByProduct, List<ProductState> productsOrderState) {
        Date deliveryTime = null;
        for (ProductState productState : productsOrderState) {
            List<ProductLogging> productsLogging = inProductLoggingsByProduct.get(productState.getProductId());
            if (Objects.isNull(productsLogging) || productsLogging.isEmpty()) {
                continue;
            }
            productsLogging.sort(Comparator.comparing(ProductLogging::getDate));

            Optional<Date> productDate = productsLogging.stream().filter(pl -> pl.getType().equals("05orderOutput"))
                    .map(ProductLogging::getDate)
                    .max(Date::compareTo);
            if(productDate.isPresent()) {
                if(Objects.isNull(deliveryTime)) {
                    deliveryTime = productDate.get();
                } else if (productDate.get().after(deliveryTime)) {
                    deliveryTime = productDate.get();
                }
            }

        }
        return deliveryTime;
    }

    private CoverageOrderAnalysis createAnalysesEntry(Long coverageId, Long orderId, Date coveredFromTheDay, String coverageDegree) {
        CoverageOrderAnalysis coverageOrderAnalysis = new CoverageOrderAnalysis();
        coverageOrderAnalysis.setOrderId(orderId);
        coverageOrderAnalysis.setCoveredFromTheDay(coveredFromTheDay);
        coverageOrderAnalysis.setCoverageDegree(coverageDegree);
        coverageOrderAnalysis.setCoverageId(coverageId);
        return coverageOrderAnalysis;
    }

    Map<Long, List<ProductState>> getProductStateByOrder(Long coverageId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("coverageId", coverageId);

        StringBuilder query = new StringBuilder();
        query.append("SELECT cpl.order_id as orderId, cp.product_id as productId, cpl.reservemissingquantity, cpl.state, ");
        query.append("o.startDate as startDate, o.product_id as orderedProductId, o.plannedQuantity ");
        query.append("FROM ordersupplies_coverageproductlogging cpl ");
        query.append("JOIN ordersupplies_coverageproduct cp ON cp.id = cpl.coverageproduct_id ");
        query.append("JOIN orders_order o ON o.id = cpl.order_id ");
        query.append("WHERE cpl.order_id is not null AND cp.materialrequirementcoverage_id = :coverageId ");
        query.append("AND cpl.eventType IN ('04orderInput', '03operationInput') ");
        query.append("GROUP BY cpl.order_id, cp.product_id, cpl.state, cpl.reservemissingquantity, o.startDate, o.product_id, o.plannedQuantity ");
        query.append("ORDER BY o.startDate ");
        List<ProductState> productStates = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(ProductState.class));

        return productStates.stream().collect(Collectors.groupingBy(ProductState::getOrderId));
    }

    Map<Long, List<ProductLogging>> getProductLoggingByProduct(Long coverageId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("coverageId", coverageId);

        StringBuilder query = new StringBuilder();
        query.append("SELECT cp.product_id as productId, cpl.id as loggingId, cpl.date, cpl.reservemissingquantity, cpl.changes, cpl.state, cpl.eventType as type ");
        query.append("FROM ordersupplies_coverageproductlogging cpl  ");
        query.append("JOIN ordersupplies_coverageproduct cp ON cp.id = cpl.coverageproduct_id ");
        query.append("WHERE cpl.eventType IN ('02delivery', '05orderOutput') ");
        query.append("AND cp.materialrequirementcoverage_id = :coverageId ");

        List<ProductLogging> productStates = jdbcTemplate.query(query.toString(), parameters, new BeanPropertyRowMapper(ProductLogging.class));

        return productStates.stream().collect(Collectors.groupingBy(ProductLogging::getProductId));
    }

}
