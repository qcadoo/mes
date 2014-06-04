/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.orders.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.Entity;

public enum OrderState implements StateEnum {

    PENDING(OrderStateStringValues.PENDING) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    ACCEPTED(OrderStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return IN_PROGRESS.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    IN_PROGRESS(OrderStateStringValues.IN_PROGRESS) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return COMPLETED.equals(targetState) || INTERRUPTED.equals(targetState) || ABANDONED.equals(targetState);
        }
    },
    COMPLETED(OrderStateStringValues.COMPLETED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },
    DECLINED(OrderStateStringValues.DECLINED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },
    INTERRUPTED(OrderStateStringValues.INTERRUPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ABANDONED.equals(targetState) || IN_PROGRESS.equals(targetState);
        }
    },
    ABANDONED(OrderStateStringValues.ABANDONED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    private OrderState(final String state) {
        this.stringValue = state;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    public static OrderState of(final Entity order) {
        if (order == null) {
            return null;
        }
        return parseString(order.getStringField(OrderFields.STATE));
    }

    public static OrderState parseString(final String string) {
        OrderState parsedStatus = null;
        for (OrderState status : OrderState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse OrderState from string '" + string + "'");
        return parsedStatus;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);

}
