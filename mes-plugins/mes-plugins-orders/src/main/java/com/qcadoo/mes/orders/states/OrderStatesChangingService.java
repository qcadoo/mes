/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_STATE;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;

@Service
public class OrderStatesChangingService {

    @Autowired
    OrderStateValidationService orderStateValidationService;

    private final Set<OrderStateListener> listeners = Sets.newHashSet();

    public void addOrderStateListener(OrderStateListener listener) {
        listeners.add(listener);
    }

    public void removeOrderStateListener(OrderStateListener listener) {
        listeners.remove(listener);
    }

    List<ChangeOrderStateMessage> performChangeState(final Entity newEntity, final Entity oldEntity) {
        if (oldEntity == null && !newEntity.getStringField(FIELD_STATE).equals(OrderStates.PENDING.getStringValue())) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && newEntity.getStringField(FIELD_STATE).equals(OrderStates.ACCEPTED.getStringValue())
                && !oldEntity.getStringField(FIELD_STATE).equals(OrderStates.PENDING.getStringValue())) {
            throw new IllegalStateException();
        }

        String newState = newEntity.getStringField(FIELD_STATE);
        if (newState.equals(OrderStates.ACCEPTED.getStringValue())) {
            return performAccepted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.IN_PROGRESS.getStringValue())) {
            return performInProgress(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.COMPLETED.getStringValue())) {
            return performCompleted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.INTERRUPTED.getStringValue())) {
            return performInterrupted(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.DECLINED.getStringValue())) {
            return performDeclined(newEntity, oldEntity);
        } else if (newState.equals(OrderStates.ABANDONED.getStringValue())) {
            return performAbandoned(newEntity, oldEntity);
        }

        return null;
    }

    List<ChangeOrderStateMessage> performAccepted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationAccepted(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onAccepted(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performInProgress(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationInProgress(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onInProgress(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performCompleted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationCompleted(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onCompleted(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performInterrupted(final Entity newEntity, final Entity oldEntity) {
        List<ChangeOrderStateMessage> errorMessages = orderStateValidationService.validationInProgress(newEntity);
        if (errorMessages.size() > 0) {
            return errorMessages;
        }
        for (OrderStateListener listener : listeners) {
            errorMessages = listener.onInterrupted(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performDeclined(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onDeclined(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

    List<ChangeOrderStateMessage> performAbandoned(final Entity newEntity, final Entity oldEntity) {
        for (OrderStateListener listener : listeners) {
            List<ChangeOrderStateMessage> errorMessages = listener.onAbandoned(newEntity);
            if (errorMessages != null && errorMessages.size() > 0) {
                return errorMessages;
            }
        }
        return null;
    }

}
