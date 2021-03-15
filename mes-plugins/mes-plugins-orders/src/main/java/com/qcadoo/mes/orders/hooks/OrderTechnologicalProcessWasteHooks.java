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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessWasteFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderTechnologicalProcessWasteHooks {

    private static final String L_WASTES_QUANTITY = "wastesQuantity";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition orderTechnologicalProcessWasteDD, final Entity orderTechnologicalProcessWaste) {
        if (checkIfShouldInsertNumber(orderTechnologicalProcessWaste)) {
            orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.NUMBER, setNumberFromSequence());
        }

        Entity orderTechnologicalProcess = orderTechnologicalProcessWaste
                .getBelongsToField(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS);

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);
            Entity technologicalProcess = orderTechnologicalProcess
                    .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

            if (Objects.nonNull(order)) {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.ORDER, order);
                orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.PRODUCT, product);
            }
            if (Objects.nonNull(technologicalProcess)) {
                orderTechnologicalProcessWaste.setField(OrderTechnologicalProcessWasteFields.TECHNOLOGICAL_PROCESS,
                        technologicalProcess);
            }
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity orderTechnologicalProcessWaste) {
        if (Objects.nonNull(orderTechnologicalProcessWaste.getId())) {
            return false;
        }

        return StringUtils.isEmpty(orderTechnologicalProcessWaste.getStringField(OrderTechnologicalProcessWasteFields.NUMBER));
    }

    private String setNumberFromSequence() {
        return jdbcTemplate.queryForObject("SELECT generate_ordertechnologicalprocesswaste_number()", Maps.newHashMap(),
                String.class);
    }

    public boolean validatesWith(final DataDefinition orderTechnologicalProcessWasteDD,
            final Entity orderTechnologicalProcessWaste) {
        Entity orderTechnologicalProcess = orderTechnologicalProcessWaste
                .getBelongsToField(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS);
        BigDecimal wasteQuantity = orderTechnologicalProcessWaste
                .getDecimalField(OrderTechnologicalProcessWasteFields.WASTE_QUANTITY);

        if (Objects.nonNull(orderTechnologicalProcess)) {
            BigDecimal quantity = orderTechnologicalProcess.getDecimalField(OrderTechnologicalProcessFields.QUANTITY);

            BigDecimal wastesQuantity = getWastesQuantity(orderTechnologicalProcessWasteDD, orderTechnologicalProcess,
                    orderTechnologicalProcessWaste.getId());

            wastesQuantity = wastesQuantity.add(wasteQuantity, numberService.getMathContext());

            if (wastesQuantity.compareTo(quantity) > 0) {
                orderTechnologicalProcessWaste
                        .addGlobalError("orders.orderTechnologicalProcessWaste.wasteQuantity.greaterThanQuantity");

                return false;
            }
        }

        return true;
    }

    private BigDecimal getWastesQuantity(final DataDefinition orderTechnologicalProcessWasteDD,
            final Entity orderTechnologicalProcess, final Long orderTechnologicalProcessWasteId) {
        SearchCriteriaBuilder searchCriteriaBuilder = orderTechnologicalProcessWasteDD.find().add(SearchRestrictions
                .belongsTo(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS, orderTechnologicalProcess));

        if (Objects.nonNull(orderTechnologicalProcessWasteId)) {
            searchCriteriaBuilder.add(SearchRestrictions.idNe(orderTechnologicalProcessWasteId));
        }

        searchCriteriaBuilder.setProjection(SearchProjections.list().add(SearchProjections
                .alias(SearchProjections.sum(OrderTechnologicalProcessWasteFields.WASTE_QUANTITY), L_WASTES_QUANTITY))
                .add(SearchProjections.rowCount()));

        searchCriteriaBuilder.addOrder(SearchOrders.asc(L_WASTES_QUANTITY));

        Entity orderTechnologicalProcessWaste = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        return BigDecimalUtils.convertNullToZero(orderTechnologicalProcessWaste.getDecimalField(L_WASTES_QUANTITY));
    }

}
