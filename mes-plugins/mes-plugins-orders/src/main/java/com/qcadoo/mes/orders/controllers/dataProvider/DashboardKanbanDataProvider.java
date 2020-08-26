package com.qcadoo.mes.orders.controllers.dataProvider;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.controllers.dto.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dto.OrderHolder;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardKanbanDataProvider {

    private static final String L_ID = "id";

    private static final String L_STATES = "states";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<OrderHolder> getOrdersPending() {
        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.ACCEPTED, OrderStateStringValues.INTERRUPTED));

        return jdbcTemplate.query(getOrdersQuery(), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    public List<OrderHolder> getOrdersInProgress() {
        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.IN_PROGRESS));

        return jdbcTemplate.query(getOrdersQuery(), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    public List<OrderHolder> getOrdersCompleted() {
        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.COMPLETED));

        return jdbcTemplate.query(getOrdersQuery(), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    public OrderHolder getOrder(Long orderId) {
        Map<String, Object> params = Maps.newHashMap();

        params.put(L_ID, orderId);

        return (OrderHolder) jdbcTemplate.queryForObject(getOrderQuery(), params, new BeanPropertyRowMapper(OrderHolder.class));
    }

    private String getOrderQueryProjections() {
        return "SELECT orderlistdto.id, orderlistdto.number, orderlistdto.name,  "
                + "orderlistdto.state, orderlistdto.typeofproductionrecording, "
                + "orderlistdto.plannedquantity, orderlistdto.donequantity, "
                + "orderlistdto.masterordernumber AS masterOrderNumber, "
                + "orderlistdto.productionlinenumber AS productionLineNumber, orderlistdto.productnumber AS productNumber, "
                + "orderlistdto.unit AS productunit, orderlistdto.companyname AS companyName "
                + "FROM orders_orderlistdto orderlistdto ";

    }

    private String getOrdersQuery() {
        String query = getOrderQueryProjections();
        query += "WHERE orderlistdto.state IN (:states) ";
        query += "AND date_trunc('day', orderlistdto.startdate) <= current_date AND current_date <= date_trunc('day', orderlistdto.finishdate) ";
        query += "ORDER BY orderlistdto.productionlinenumber, orderlistdto.startdate";

        return query;
    }

    private String getOrderQuery() {
        String query = getOrderQueryProjections();
        query += "WHERE orderlistdto.id = :id ";

        return query;
    }

    public List<OperationalTaskHolder> getOperationalTasksPending() {
        String additionalRestrictions = "AND coalesce(operationaltaskdto.usedquantity, 0) = 0 ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED));

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions), params, new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    public List<OperationalTaskHolder> getOperationalTasksInProgress() {
        String additionalRestrictions = "AND operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity < 100 ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED, OperationalTaskStateStringValues.FINISHED));

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions), params, new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    public List<OperationalTaskHolder> getOperationalTasksCompleted() {
        String additionalRestrictions = "AND (operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity >= 100) OR operationaltaskdto.state = '03finished' ";

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED, OperationalTaskStateStringValues.FINISHED));

        return jdbcTemplate.query(getOperationalTasksQuery(additionalRestrictions), params, new BeanPropertyRowMapper(OperationalTaskHolder.class));
    }

    private String getOperationalTaskQueryProjections() {
        return "SELECT operationaltaskdto.id, operationaltaskdto.number, operationaltaskdto.name, "
                + "operationaltaskdto.plannedquantity, operationaltaskdto.usedquantity, "
                + "operationaltaskdto.state, operationaltaskdto.type, operationaltaskdto.ordernumber AS orderNumber, "
                + "operationaltaskdto.workstationnumber AS workstationNumber, "
                + "operationaltaskdto.productnumber AS productNumber, operationaltaskdto.productUnit AS productUnit, "
                + "operationaltaskdto.staffname AS staffName, operationaltaskdto.orderid AS orderId, "
                + "product.number AS orderProductNumber FROM orders_operationaltaskdto operationaltaskdto "
                + "LEFT JOIN orders_order ordersorder ON ordersorder.id = operationaltaskdto.orderid "
                + "LEFT JOIN basic_product product ON product.id = ordersorder.product_id ";
    }

    private String getOperationalTasksQuery(final String additionalRestrictions) {
        String query = getOperationalTaskQueryProjections();
        query += "WHERE operationaltaskdto.state IN (:states) ";
        query += "AND date_trunc('day', operationaltaskdto.startdate) <= current_date AND current_date <= date_trunc('day', operationaltaskdto.finishdate) ";
        query += additionalRestrictions;
        query += "ORDER BY operationaltaskdto.workstationnumber, operationaltaskdto.startdate";

        return query;
    }

}
