/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.technologies.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum TechnologyState implements StateEnum {

    DRAFT(TechnologyStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || DECLINED.equals(targetState) || CHECKED.equals(targetState);
        }
    },
    ACCEPTED(TechnologyStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return OUTDATED.equals(targetState);
        }
    },
    DECLINED(TechnologyStateStringValues.DECLINED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }

    },
    OUTDATED(TechnologyStateStringValues.OUTDATED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },
    CHECKED(TechnologyStateStringValues.CHECKED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || DRAFT.equals(targetState) || DECLINED.equals(targetState);
        }

    };

    private String stringValue;

    private TechnologyState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);

    public String getStringValue() {
        return stringValue;
    }

    public static TechnologyState parseString(final String string) {
        TechnologyState parsedStatus = null;
        for (TechnologyState status : TechnologyState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse '" + string + "'");
        return parsedStatus;
    }

}
