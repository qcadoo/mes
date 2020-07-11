package com.qcadoo.mes.orders.controllers.dataProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OperationalTaskDtoFields;
import com.qcadoo.mes.orders.constants.OrderListDtoFields;
import com.qcadoo.mes.orders.controllers.dao.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dao.OrderHolder;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;

@Service
public class DashboardKanbanDataProvider {

    private static final String L_ID = "id";

    private static final String L_STATES = "states";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<OrderHolder> getOrdersPending() {
        List<OrderHolder> orders = Lists.newArrayList();

        String query = getOrdersQuery();

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.ACCEPTED, OrderStateStringValues.INTERRUPTED));

        try {
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query, params);

            queryForList.forEach(stringObjectMap -> orders.add(createOrderHolder(stringObjectMap)));

            return orders;
        } catch (EmptyResultDataAccessException e) {
            return orders;
        }
    }

    public List<OrderHolder> getOrdersInProgress() {
        List<OrderHolder> orders = Lists.newArrayList();

        String query = getOrdersQuery();

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.IN_PROGRESS));

        try {
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query, params);

            queryForList.forEach(stringObjectMap -> orders.add(createOrderHolder(stringObjectMap)));

            return orders;
        } catch (EmptyResultDataAccessException e) {
            return orders;
        }
    }

    public List<OrderHolder> getOrdersCompleted() {
        List<OrderHolder> orders = Lists.newArrayList();

        String query = getOrdersQuery();

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES, Sets.newHashSet(OrderStateStringValues.COMPLETED));

        try {
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query, params);

            queryForList.forEach(stringObjectMap -> orders.add(createOrderHolder(stringObjectMap)));

            return orders;
        } catch (EmptyResultDataAccessException e) {
            return orders;
        }
    }

    private String getOrdersQuery() {
        StringBuilder query = new StringBuilder();

        query.append("SELECT orderlistdto.id, orderlistdto.number, orderlistdto.name,  ");
        query.append("orderlistdto.state, orderlistdto.typeofproductionrecording, ");
        query.append("orderlistdto.plannedquantity, orderlistdto.donequantity, ");
        query.append("orderlistdto.masterordernumber AS masterOrderNumber, ");
        query.append("orderlistdto.productionlinenumber AS productionLineNumber, ");
        query.append("orderlistdto.productnumber AS productNumber, ");
        query.append("orderlistdto.unit AS unit, ");
        query.append("orderlistdto.companyname AS companyName ");
        query.append("FROM orders_orderlistdto orderlistdto ");
        query.append("WHERE orderlistdto.state IN (:states) ");
        query.append(
                "AND date_trunc('day', orderlistdto.startdate) <= current_date AND current_date <= date_trunc('day', orderlistdto.finishdate)");

        return query.toString();
    }

    private OrderHolder createOrderHolder(final Map<String, Object> stringObjectMap) {
        OrderHolder orderHolder = new OrderHolder();

        orderHolder.setId((Long) stringObjectMap.get(L_ID));
        orderHolder.setNumber((String) stringObjectMap.get(OrderListDtoFields.NUMBER));
        orderHolder.setName((String) stringObjectMap.get(OrderListDtoFields.NAME));
        orderHolder.setState((String) stringObjectMap.get(OrderListDtoFields.STATE));
        orderHolder.setTypeOfProductionRecording((String) stringObjectMap.get(OrderListDtoFields.TYPE_OF_PRODUCTION_RECORDING));
        orderHolder.setPlannedQuantity((BigDecimal) stringObjectMap.get(OrderListDtoFields.PLANNED_QUANTITY));
        orderHolder.setDoneQuantity((BigDecimal) stringObjectMap.get(OrderListDtoFields.DONE_QUANTITY));
        orderHolder.setMasterOrderNumber((String) stringObjectMap.get(OrderListDtoFields.MASTER_ORDER_NUMBER));
        orderHolder.setProductionLineNumber((String) stringObjectMap.get(OrderListDtoFields.PRODUCTION_LINE_NUMBER));
        orderHolder.setProductNumber((String) stringObjectMap.get(OrderListDtoFields.PRODUCT_NUMBER));
        orderHolder.setProductUnit((String) stringObjectMap.get(OrderListDtoFields.UNIT));
        orderHolder.setCompanyName((String) stringObjectMap.get(OrderListDtoFields.COMPANY_NAME));

        return orderHolder;
    }

    public List<OperationalTaskHolder> getOperationalTasksPending() {
        List<OperationalTaskHolder> operationalTasks = Lists.newArrayList();

        String additionalRestrictions = "AND coalesce(operationaltaskdto.usedquantity, 0) = 0";

        String query = getOperationalTasksQuery(additionalRestrictions);

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED, OperationalTaskStateStringValues.FINISHED));

        try {
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query, params);

            queryForList.forEach(stringObjectMap -> operationalTasks.add(createOperationalTaskHolder(stringObjectMap)));

            return operationalTasks;
        } catch (EmptyResultDataAccessException e) {
            return operationalTasks;
        }
    }

    public List<OperationalTaskHolder> getOperationalTasksInProgress() {
        List<OperationalTaskHolder> operationalTasks = Lists.newArrayList();

        String additionalRestrictions = "AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity > 0 AND operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity < 100";

        String query = getOperationalTasksQuery(additionalRestrictions);

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED, OperationalTaskStateStringValues.FINISHED));

        try {
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query, params);

            queryForList.forEach(stringObjectMap -> operationalTasks.add(createOperationalTaskHolder(stringObjectMap)));

            return operationalTasks;
        } catch (EmptyResultDataAccessException e) {
            return operationalTasks;
        }
    }

    public List<OperationalTaskHolder> getOperationalTasksCompleted() {
        List<OperationalTaskHolder> operationalTasks = Lists.newArrayList();

        String additionalRestrictions = "AND (operationaltaskdto.usedquantity * 100 / operationaltaskdto.plannedquantity >= 100 OR operationaltaskdto.state = '03finished')";

        String query = getOperationalTasksQuery(additionalRestrictions);

        Map<String, Object> params = Maps.newHashMap();

        params.put(L_STATES,
                Sets.newHashSet(OperationalTaskStateStringValues.STARTED, OperationalTaskStateStringValues.FINISHED));

        try {
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(query, params);

            queryForList.forEach(stringObjectMap -> operationalTasks.add(createOperationalTaskHolder(stringObjectMap)));

            return operationalTasks;
        } catch (EmptyResultDataAccessException e) {
            return operationalTasks;
        }
    }

    private String getOperationalTasksQuery(final String additionalRestrictions) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT operationaltaskdto.id, operationaltaskdto.number, operationaltaskdto.name, ");
        query.append("operationaltaskdto.plannedquantity, operationaltaskdto.usedquantity, ");
        query.append("operationaltaskdto.state, operationaltaskdto.type, ");
        query.append("operationaltaskdto.ordernumber AS orderNumber, ");
        query.append("operationaltaskdto.workstationnumber AS workstationNumber, ");
        query.append("operationaltaskdto.productnumber AS productNumber, ");
        query.append("operationaltaskdto.productUnit AS productUnit, ");
        query.append("operationaltaskdto.staffname AS staffName, ");
        query.append("product.number AS orderProductNumber ");
        query.append("FROM orders_operationaltaskdto operationaltaskdto ");
        query.append("LEFT JOIN orders_order ordersorder ");
        query.append("ON ordersorder.id = operationaltaskdto.orderid ");
        query.append("LEFT JOIN basic_product product ");
        query.append("ON product.id = ordersorder.product_id ");
        query.append("WHERE operationaltaskdto.state IN (:states) ");
        query.append(
                "AND date_trunc('day', operationaltaskdto.startdate) <= current_date AND current_date <= date_trunc('day', operationaltaskdto.finishdate) ");
        query.append(additionalRestrictions);

        return query.toString();
    }

    private OperationalTaskHolder createOperationalTaskHolder(final Map<String, Object> stringObjectMap) {
        OperationalTaskHolder operationalTaskHolder = new OperationalTaskHolder();

        operationalTaskHolder.setId((Long) stringObjectMap.get(L_ID));
        operationalTaskHolder.setNumber((String) stringObjectMap.get(OperationalTaskDtoFields.NUMBER));
        operationalTaskHolder.setName((String) stringObjectMap.get(OperationalTaskDtoFields.NAME));
        operationalTaskHolder.setState((String) stringObjectMap.get(OperationalTaskDtoFields.STATE));
        operationalTaskHolder.setType((String) stringObjectMap.get(OperationalTaskDtoFields.TYPE));
        operationalTaskHolder.setPlannedQuantity((BigDecimal) stringObjectMap.get(OperationalTaskDtoFields.PLANNED_QUANTITY));
        operationalTaskHolder.setUsedQuantity((BigDecimal) stringObjectMap.get(OperationalTaskDtoFields.USED_QUANTITY));
        operationalTaskHolder.setOrderNumber((String) stringObjectMap.get(OperationalTaskDtoFields.ORDER_NUMBER));
        operationalTaskHolder.setWorkstationNumber((String) stringObjectMap.get(OperationalTaskDtoFields.WORKSTATION_NUMBER));
        operationalTaskHolder.setProductNumber((String) stringObjectMap.get(OperationalTaskDtoFields.PRODUCT_NUMBER));
        operationalTaskHolder.setProductUnit((String) stringObjectMap.get(OperationalTaskDtoFields.PRODUCT_UNIT));
        operationalTaskHolder.setStaffName((String) stringObjectMap.get(OperationalTaskDtoFields.STAFF_NAME));
        operationalTaskHolder.setOrderProductNumber((String) stringObjectMap.get(OperationalTaskDtoFields.ORDER_PRODUCT_NUMBER));

        return operationalTaskHolder;
    }

}
