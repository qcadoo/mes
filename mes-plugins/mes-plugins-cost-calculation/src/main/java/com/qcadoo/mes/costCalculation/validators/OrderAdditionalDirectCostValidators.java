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
package com.qcadoo.mes.costCalculation.validators;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.OrderAdditionalDirectCostFields;
import com.qcadoo.mes.costCalculation.constants.OrderFieldsCC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderAdditionalDirectCostValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {

        return checkIfAdditionalDirectCostIsNotAlreadyAdded(dataDefinition, entity);
    }

    private boolean checkIfAdditionalDirectCostIsNotAlreadyAdded(final DataDefinition dataDefinition, final Entity entity) {
        boolean isNotAlreadyAdded = true;

        Long entityId = entity.getId();
        Entity order = entity.getBelongsToField(OrderAdditionalDirectCostFields.ORDER);
        Entity additionalDirectCost = entity.getBelongsToField(OrderAdditionalDirectCostFields.ADDITIONAL_DIRECT_COST);

        if (!Objects.isNull(order) && !Objects.isNull(additionalDirectCost)) {
            Entity orderFromDB = order.getDataDefinition().get(order.getId());

            List<Entity> orderAdditionalDirectCosts = orderFromDB.getHasManyField(OrderFieldsCC.ORDER_ADDITIONAL_DIRECT_COSTS);

            if (!Objects.isNull(entityId)) {
                orderAdditionalDirectCosts = filterCurrentCost(entity, orderAdditionalDirectCosts);
            }

            if (checkIfCostIsAlreadyAdded(additionalDirectCost, orderAdditionalDirectCosts)) {
                entity.addError(dataDefinition.getField(OrderAdditionalDirectCostFields.ADDITIONAL_DIRECT_COST), "costCalculation.orderAdditionalDirectCost.error.additionalDirectCost.alreadyAdded");

                isNotAlreadyAdded = false;
            }
        }

        return isNotAlreadyAdded;
    }

    private List<Entity> filterCurrentCost(final Entity entity, final List<Entity> orderAdditionalDirectCosts) {
        return orderAdditionalDirectCosts.stream().filter(addedCost -> !addedCost.getId().equals(entity.getId())).collect(Collectors.toList());
    }

    private boolean checkIfCostIsAlreadyAdded(final Entity additionalDirectCost, final List<Entity> orderAdditionalDirectCosts) {
        return orderAdditionalDirectCosts.stream().anyMatch(addedCost -> addedCost.getBelongsToField(OrderAdditionalDirectCostFields.ADDITIONAL_DIRECT_COST).getId().equals(additionalDirectCost.getId()));
    }

}
