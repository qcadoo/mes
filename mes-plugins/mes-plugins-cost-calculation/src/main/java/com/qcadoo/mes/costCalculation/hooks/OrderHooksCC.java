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
package com.qcadoo.mes.costCalculation.hooks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.OrderAdditionalDirectCostFields;
import com.qcadoo.mes.costCalculation.constants.OrderFieldsCC;
import com.qcadoo.mes.costCalculation.constants.TechnologyFieldsCC;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooksCC {

    @Autowired
    private OrderAdditionalDirectCostDetails orderAdditionalDirectCostDetails;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyAdditionalDirectCostsFromTechnology(final DataDefinition orderDD, final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology != null) {
            List<Entity> costs = new ArrayList<>();
            DataDefinition orderAdditionalDirectCostDD = dataDefinitionService
                    .get(CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_ORDER_ADDITIONAL_DIRECT_COST);
            Date startDate = order.getDateField(OrderFields.START_DATE);
            for (Entity additionalDirectCost : technology.getManyToManyField(TechnologyFieldsCC.ADDITIONAL_DIRECT_COSTS)) {
                if (additionalDirectCost.isActive()) {
                    Entity orderAdditionalDirectCost = orderAdditionalDirectCostDD.create();
                    orderAdditionalDirectCost.setField(OrderAdditionalDirectCostFields.ADDITIONAL_DIRECT_COST, additionalDirectCost);
                    if (startDate != null) {
                        BigDecimal currentCost = orderAdditionalDirectCostDetails.findCurrentCost(additionalDirectCost.getId(), startDate);
                        orderAdditionalDirectCost.setField(OrderAdditionalDirectCostFields.ACTUAL_COST, currentCost);
                    }
                    costs.add(orderAdditionalDirectCost);
                }
            }
            order.setField(OrderFieldsCC.ORDER_ADDITIONAL_DIRECT_COSTS, costs);
        }
    }
}
