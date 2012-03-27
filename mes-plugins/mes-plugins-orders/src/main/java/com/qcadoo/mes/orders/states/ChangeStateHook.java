/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.orders.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_STATE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;

@Service
public class ChangeStateHook {

    @Autowired
    private OrderStatesChangingService orderStatesChangingService;

    @Autowired
    private OrderStateValidationService orderStateValidationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changedState(final DataDefinition dataDefinition, final Entity newEntity) {
        checkArgument(newEntity != null, "entity is null");
        if (newEntity.getId() == null) {
            return;
        }
        Entity oldEntity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                newEntity.getId());

        if (oldEntity == null) {
            return;
        }
        if (oldEntity.getStringField(FIELD_STATE).equals(newEntity.getStringField(FIELD_STATE))) {
            String state = oldEntity.getStringField(FIELD_STATE);
            List<ChangeOrderStateMessage> errors = Lists.newArrayList();
            if (state.equals(OrderStates.ACCEPTED.getStringValue())) {
                errors.addAll(orderStateValidationService.validationAccepted(newEntity));
            } else if (state.equals(OrderStates.IN_PROGRESS.getStringValue())
                    || state.equals(OrderStates.INTERRUPTED.getStringValue())) {
                errors.addAll(orderStateValidationService.validationInProgress(newEntity));
            } else if (state.equals(OrderStates.COMPLETED.getStringValue())) {
                errors.addAll(orderStateValidationService.validationCompleted(newEntity));
            }
            if (!errors.isEmpty()) {
                for (ChangeOrderStateMessage error : errors) {
                    if (error.getReferenceToField() == null) {
                        newEntity.addError(dataDefinition.getField(error.getReferenceToField()),
                                error.getMessage() + "." + error.getReferenceToField());
                    } else {
                        newEntity.addGlobalError(error.getMessage(), error.getVars());
                    }
                }
            }
            return;
        }
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newEntity, oldEntity);
        if (!errors.isEmpty()) {
            if (errors.size() == 1 && errors.get(0).getType().equals(MessageType.INFO)) {
                return;
            }
            newEntity.setField(FIELD_STATE, oldEntity.getStringField(FIELD_STATE));
            for (ChangeOrderStateMessage error : errors) {
                if (error.getReferenceToField() == null) {
                    newEntity.addGlobalError(error.getMessage(), error.getVars());
                } else {
                    newEntity.addError(dataDefinition.getField(error.getReferenceToField()),
                            error.getMessage() + "." + error.getReferenceToField(), error.getVars());
                }
            }
            return;
        }

        orderStateValidationService.saveLogging(newEntity, oldEntity.getStringField(FIELD_STATE),
                newEntity.getStringField(FIELD_STATE));
    }
}
