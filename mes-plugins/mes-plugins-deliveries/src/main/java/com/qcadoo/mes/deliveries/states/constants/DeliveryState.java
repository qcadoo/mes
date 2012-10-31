/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum DeliveryState implements StateEnum {

    DRAFT(DeliveryStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return PREPARED.equals(targetState) || APPROVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    PREPARED(DeliveryStateStringValues.PREPARED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_CORRECTION.equals(targetState) || APPROVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    DURING_CORRECTION(DeliveryStateStringValues.DURING_CORRECTION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return APPROVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    DECLINED(DeliveryStateStringValues.DECLINED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },
    APPROVED(DeliveryStateStringValues.APPROVED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return RECEIVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    RECEIVED(DeliveryStateStringValues.RECEIVED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    private DeliveryState(final String state) {
        this.stringValue = state;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    public static DeliveryState parseString(final String string) {
        DeliveryState parsedStatus = null;
        for (DeliveryState status : DeliveryState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse DeliveryState from string '" + string + "'");
        return parsedStatus;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);

}
