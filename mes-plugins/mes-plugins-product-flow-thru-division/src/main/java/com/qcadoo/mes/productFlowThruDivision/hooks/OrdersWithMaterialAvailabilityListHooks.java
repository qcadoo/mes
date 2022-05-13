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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPlanningListDtoFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.OrderMaterialAvailability;
import com.qcadoo.mes.productFlowThruDivision.constants.AvailabilityOfMaterialAvailability;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrdersWithMaterialAvailabilityListHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderMaterialAvailability orderMaterialAvailability;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        String ordersIds = view.getJsonContext().get("window.mainTab.ordersIds").toString();
        Set<Long> ids = Lists.newArrayList(ordersIds.split(",")).stream().map(Long::valueOf).collect(Collectors.toSet());

        Map<Long, String> ordersAvailabilities = orderMaterialAvailability.generateMaterialAvailabilityForOrders(ids);
        List<Entity> orderPlanningListDtos = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PLANNING_LIST_DTO)
                .find().add(SearchRestrictions.in("id", ids)).list().getEntities().stream()
                .sorted(Comparator.comparing(e -> e.getDateField(OrderPlanningListDtoFields.DATE_FROM), nullsFirst(naturalOrder()))).collect(Collectors.toList());
        for (Entity orderPlanningListDto : orderPlanningListDtos) {
            String availability = ordersAvailabilities.get(orderPlanningListDto.getId());
            orderPlanningListDto.setField("availability", availability);
            if (AvailabilityOfMaterialAvailability.FULL.getStrValue().equals(availability)) {
                orderPlanningListDto.setField("availabilityCellColor", "green-cell");
            } else if (AvailabilityOfMaterialAvailability.PARTIAL.getStrValue().equals(availability)) {
                orderPlanningListDto.setField("availabilityCellColor", "orange-cell");
            } else {
                orderPlanningListDto.setField("availability", AvailabilityOfMaterialAvailability.NONE.getStrValue());
                orderPlanningListDto.setField("availabilityCellColor", "red-cell");
            }
        }

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        grid.setEntities(orderPlanningListDtos);
    }
}
