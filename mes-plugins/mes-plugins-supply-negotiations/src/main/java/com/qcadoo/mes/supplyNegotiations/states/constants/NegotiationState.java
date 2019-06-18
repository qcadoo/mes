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
package com.qcadoo.mes.supplyNegotiations.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum NegotiationState implements StateEnum {

    DRAFT(NegotiationStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return GENERATED_REQUESTS.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    GENERATED_REQUESTS(NegotiationStateStringValues.GENERATED_REQUESTS) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return COMPLETED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    DECLINED(NegotiationStateStringValues.DECLINED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },
    COMPLETED(NegotiationStateStringValues.COMPLETED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    private NegotiationState(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public abstract boolean canChangeTo(final StateEnum targetState);

    public static NegotiationState parseString(final String string) {
        NegotiationState parsedStatus = null;
        for (NegotiationState status : NegotiationState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse '" + string + "'");
        return parsedStatus;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

}
