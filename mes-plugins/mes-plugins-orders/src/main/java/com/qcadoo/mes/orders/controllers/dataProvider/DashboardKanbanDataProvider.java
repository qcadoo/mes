package com.qcadoo.mes.orders.controllers.dataProvider;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.orders.controllers.dto.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dto.OrderHolder;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionLines.constants.UserFieldsPL;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardKanbanDataProvider {

    private static final String L_ID = "id";

    private static final String L_STATES = "states";

    private static final String L_ORDER_ID = "orderId";

    private static final String L_PRODUCTION_LINE_ID = "productionLineId";

    private static final String L_START_DATE = "startDate";

    public static final int DEFAULT_RECORDS_LIMIT = 50;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ParameterService parameterService;

    public List<OrderHolder> getOrdersPending() {
        Map<String, Object> params = Maps.newHashMap();

        Entity productionLine = getCurrentUserProductionLine();

        if (!Objects.isNull(productionLine)) {
            params.put(L_PRODUCTION_LINE_ID, productionLine.getId());
        }

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.ACCEPTED, OrderStateStringValues.INTERRUPTED));

        return jdbcTemplate.query(getOrdersQuery(productionLine), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    public List<OrderHolder> getOrdersInProgress() {
        Map<String, Object> params = Maps.newHashMap();

        Entity productionLine = getCurrentUserProductionLine();

        if (!Objects.isNull(productionLine)) {
            params.put(L_PRODUCTION_LINE_ID, productionLine.getId());
        }

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.IN_PROGRESS));

        return jdbcTemplate.query(getOrdersQuery(productionLine), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    public List<OrderHolder> getOrdersCompleted() {
        Map<String, Object> params = Maps.newHashMap();

        Entity productionLine = getCurrentUserProductionLine();

        if (!Objects.isNull(productionLine)) {
            params.put(L_PRODUCTION_LINE_ID, productionLine.getId());
        }

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.COMPLETED));

        return jdbcTemplate.query(getOrdersQuery(productionLine), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    public OrderHolder getOrder(final Long orderId) {
        Map<String, Object> params = Maps.newHashMap();

        params.put(L_ID, orderId);

        return (OrderHolder) jdbcTemplate.queryForObject(getOrderQuery(), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    private String getOrderQueryProjections() {
        return "SELECT orderlistdto.id, orderlistdto.number, orderlistdto.name,  orderlistdto.reportedProductionQuantity,  "
                + "orderlistdto.state, orderlistdto.typeofproductionrecording, "
                + "orderlistdto.plannedquantity, orderlistdto.donequantity, mop.masterorderquantity, "
                + "orderlistdto.masterordernumber AS masterOrderNumber, orderlistdto.ordercategory AS orderCategory, "
                + "orderlistdto.productionlinenumber AS productionLineNumber, orderlistdto.productnumber AS productNumber, "
                + "orderlistdto.unit AS productunit, orderlistdto.companyname AS companyName, orderlistdto.addressNumber AS addressNumber, "
                + "orderlistdto.description, orderlistdto.productname AS productName, p.dashboardshowdescription, p.dashboardshowforproduct "
                + "FROM orders_orderlistdto orderlistdto CROSS JOIN basic_parameter p "
                + "LEFT JOIN masterorders_masterorderproduct mop ON mop.product_id = orderlistdto.productid "
                + "AND mop.masterorder_id = orderlistdto.masterorderid ";
    }

    private String getOrdersQuery(final Entity productionLine) {
        String query = getOrderQueryProjections();

        query += "WHERE orderlistdto.active = true AND orderlistdto.state IN (:states) ";
        query += "AND date_trunc('day', orderlistdto.startdate) <= current_date AND current_date <= date_trunc('day', orderlistdto.finishdate) ";

        if (!Objects.isNull(productionLine)) {
            query += "AND orderlistdto.productionlineid = :productionLineId ";
        }

        query += "ORDER BY orderlistdto.productionlinenumber, orderlistdto.";

        String dashboardOrderSorting = parameterService.getParameter().getStringField(ParameterFields.DASHBOARD_ORDER_SORTING);

        if (Objects.nonNull(dashboardOrderSorting)) {
            dashboardOrderSorting = dashboardOrderSorting.replaceAll("[0-9]", "");
        } else {
            dashboardOrderSorting = L_START_DATE;
        }

        query += dashboardOrderSorting;

        if (L_START_DATE.equals(dashboardOrderSorting)) {
            query += " DESC ";
        } else {
            query += " ASC ";
        }

        query += "LIMIT " + getRecordsLimit();

        return query;
    }

    private String getOrderQuery() {
        String query = getOrderQueryProjections();

        query += "WHERE orderlistdto.id = :id ";

        return query;
    }

    private Entity getCurrentUserProductionLine() {
        Entity currentUser = dataDefinitionService
                .get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

        return currentUser.getBelongsToField(UserFieldsPL.PRODUCTION_LINE);
    }

    public List<OperationalTaskHolder> getOperationalTasksPendingForOrder(final Long orderId) {
        String additionalRestrictions = "AND operationaltaskdto.orderid = :orderId AND coalesce(operationaltaskdto.usedquantity, 0) = 0 ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OperationalTaskStateStringValues.FINISHED));
        params.put(L_ORDER_ID, orderId);

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions, false), params,
                new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    public List<OperationalTaskHolder> getOperationalTasksPending() {
        String additionalRestrictions = "AND coalesce(operationaltaskdto.usedquantity, 0) = 0 ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OperationalTaskStateStringValues.PENDING, OperationalTaskStateStringValues.STARTED));

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions, true), params,
                new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    public List<OperationalTaskHolder> getOperationalTasksInProgress() {
        String additionalRestrictions = "AND (operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity < 100) ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OperationalTaskStateStringValues.STARTED));

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions, true), params,
                new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    public List<OperationalTaskHolder> getOperationalTasksCompleted() {
        String additionalRestrictions = "AND ((operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity >= 100) OR operationaltaskdto.state = '03finished') ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED, OperationalTaskStateStringValues.FINISHED));

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions, true), params,
                new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    private String getOperationalTaskQueryProjections() {
        return "SELECT operationaltaskdto.id, operationaltaskdto.number, operationaltaskdto.name, "
                + "operationaltaskdto.plannedquantity, operationaltaskdto.usedquantity, "
                + "operationaltaskdto.state, operationaltaskdto.type, operationaltaskdto.ordernumber AS orderNumber, "
                + "operationaltaskdto.workstationnumber AS workstationNumber, operationaltaskdto.productname AS productName, "
                + "operationaltaskdto.productnumber AS productNumber, operationaltaskdto.productUnit AS productUnit, "
                + "operationaltaskdto.staffname AS staffName, operationaltaskdto.orderid AS orderId, "
                + "product.number AS orderProductNumber, product.name AS orderProductName, operationaltaskdto.description, "
                + "p.dashboardshowdescription, p.dashboardshowforproduct FROM orders_operationaltaskdto operationaltaskdto "
                + "LEFT JOIN orders_order ordersorder ON ordersorder.id = operationaltaskdto.orderid "
                + "LEFT JOIN basic_product product ON product.id = ordersorder.product_id CROSS JOIN basic_parameter p ";
    }

    private String getOperationalTasksQuery(final String additionalRestrictions, final boolean in) {
        String query = getOperationalTaskQueryProjections();

        if (in) {
            query += "WHERE operationaltaskdto.state IN (:states) ";
        } else {
            query += "WHERE operationaltaskdto.state NOT IN (:states) ";
        }

        query += "AND date_trunc('day', operationaltaskdto.startdate) <= current_date AND current_date <= date_trunc('day', operationaltaskdto.finishdate) ";
        query += additionalRestrictions;
        query += "ORDER BY operationaltaskdto.workstationnumber, operationaltaskdto.startdate ";
        query += "LIMIT " + getRecordsLimit();

        return query;
    }


    private Integer getRecordsLimit() {
        Integer numberVisibleOrdersTasksOnDashboard = parameterService.getParameter().getIntegerField("numberVisibleOrdersTasksOnDashboard");
        return Objects.isNull(numberVisibleOrdersTasksOnDashboard) ? DEFAULT_RECORDS_LIMIT : numberVisibleOrdersTasksOnDashboard;
    }
}
