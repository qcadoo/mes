/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.orders.hooks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderTechnologicalProcessHooks {

    private static final String L_QUANTITY_SUM = "quantitySum";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition orderTechnologicalProcessDD, final Entity orderTechnologicalProcess) {
        if (checkIfShouldInsertNumber(orderTechnologicalProcess)) {
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.NUMBER, setNumberFromSequence());
        }

        Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

        if (Objects.nonNull(orderTechnologicalProcessId)) {
            Entity orderPack = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);
            Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);
            Entity technologicalProcess = orderTechnologicalProcess
                    .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

            BigDecimal quantity = orderTechnologicalProcess.getDecimalField(OrderTechnologicalProcessFields.QUANTITY);

            BigDecimal orderTechnologicalProcessQuantity = getOrderTechnologicalProcessQuantity(orderTechnologicalProcessDD,
                    orderPack, order, technologicalProcess, orderTechnologicalProcessId);

            orderTechnologicalProcessQuantity = orderTechnologicalProcessQuantity.add(quantity, numberService.getMathContext());

            BigDecimal plannedQuantity;

            if (Objects.nonNull(orderPack)) {
                plannedQuantity = orderPack.getDecimalField(OrderPackFields.QUANTITY);
            } else {
                plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            }

            if (orderTechnologicalProcessQuantity.compareTo(plannedQuantity) > 0) {
                orderTechnologicalProcess
                        .addGlobalMessage("orders.orderTechnologicalProcess.quantity.greaterThanPlannedQuantity");
            }
        } else {
            orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.CREATE_DATE, DateTime.now().toDate());
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity orderTechnologicalProcess) {
        if (Objects.nonNull(orderTechnologicalProcess.getId())) {
            return false;
        }

        return StringUtils.isEmpty(orderTechnologicalProcess.getStringField(OrderTechnologicalProcessFields.NUMBER));
    }

    private String setNumberFromSequence() {
        return jdbcTemplate.queryForObject("SELECT generate_ordertechnologicalprocess_number()", Maps.newHashMap(), String.class);
    }

    private BigDecimal getOrderTechnologicalProcessQuantity(final DataDefinition orderTechnologicalProcessDD,
            final Entity orderPack, final Entity order, final Entity technologicalProcess,
            final Long orderTechnologicalProcessId) {
        SearchCriteriaBuilder searchCriteriaBuilder = orderTechnologicalProcessDD.find()
                .add(SearchRestrictions.belongsTo(OrderTechnologicalProcessFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS, technologicalProcess))
                .add(SearchRestrictions.idNe(orderTechnologicalProcessId));

        if (Objects.nonNull(orderPack)) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OrderTechnologicalProcessFields.ORDER_PACK, orderPack));
        }

        searchCriteriaBuilder.setProjection(SearchProjections.list()
                .add(SearchProjections.alias(SearchProjections.sum(OrderTechnologicalProcessFields.QUANTITY), L_QUANTITY_SUM))
                .add(SearchProjections.rowCount()));

        searchCriteriaBuilder.addOrder(SearchOrders.asc(L_QUANTITY_SUM));

        Entity orderTechnologicalProcess = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        return BigDecimalUtils.convertNullToZero(orderTechnologicalProcess.getDecimalField(L_QUANTITY_SUM));
    }

    public boolean onDelete(final DataDefinition orderTechnologicalProcessDD, final Entity orderTechnologicalProcess) {
        Entity orderPack = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);
        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

        if (Objects.nonNull(orderPack)) {
            List<Entity> orderTechnologicalProcesses = orderPack.getHasManyField(OrderPackFields.ORDER_TECHNOLOGICAL_PROCESSES);

            return deleteOrderTechnologicalProcesses(orderTechnologicalProcess, orderTechnologicalProcesses);
        } else {
            List<Entity> orderTechnologicalProcesses = order.getHasManyField(OrderFields.ORDER_TECHNOLOGICAL_PROCESSES);

            return deleteOrderTechnologicalProcesses(orderTechnologicalProcess, orderTechnologicalProcesses);
        }
    }

    private boolean deleteOrderTechnologicalProcesses(final Entity orderTechnologicalProcess,
            final List<Entity> orderTechnologicalProcesses) {
        boolean allowChangeOrDeleteOrderTechnologicalProcess = parameterService.getParameter()
                .getBooleanField(ParameterFieldsO.ALLOW_CHANGE_OR_DELETE_ORDER_TECHNOLOGICAL_PROCESS);
        boolean areCompleted = orderTechnologicalProcesses.stream().anyMatch(this::filterCompleted);

        if (allowChangeOrDeleteOrderTechnologicalProcess || !areCompleted) {
            deleteOrderTechnologicalProcesses(orderTechnologicalProcess.getId(), orderTechnologicalProcesses);

            return true;
        } else {
            orderTechnologicalProcess.addGlobalError("orders.orderTechnologicalProcess.error.areCompleted");

            return false;
        }
    }

    private boolean filterCompleted(final Entity orderTechnologicalProcess) {
        Date date = orderTechnologicalProcess.getDateField(OrderTechnologicalProcessFields.DATE);
        Entity worker = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.WORKER);

        return Objects.nonNull(date) && Objects.nonNull(worker);
    }

    private void deleteOrderTechnologicalProcesses(final Long orderTechnologicalProcessId,
            final List<Entity> orderTechnologicalProcesses) {
        List<Long> ids = orderTechnologicalProcesses.stream()
                .filter(orderTechnologicalProcess -> filterCurrent(orderTechnologicalProcess, orderTechnologicalProcessId))
                .map(Entity::getId).collect(Collectors.toList());

        if (!ids.isEmpty()) {
            Map<String, Object> params = Maps.newHashMap();

            params.put("ids", ids);

            jdbcTemplate.update("DELETE FROM orders_ordertechnologicalprocess WHERE id IN (:ids);", params);
        }
    }

    private boolean filterCurrent(final Entity orderTechnologicalProcess, final Long orderTechnologicalProcessId) {
        return !orderTechnologicalProcess.getId().equals(orderTechnologicalProcessId);
    }

}
