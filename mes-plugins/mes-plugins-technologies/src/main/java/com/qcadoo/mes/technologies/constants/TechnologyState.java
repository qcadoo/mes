/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.technologies.constants;

public enum TechnologyState {
    
    DRAFT("draft") {

        @Override
        public TechnologyState changeState(final String targetState) {
            if (targetState != null && "accepted".equalsIgnoreCase(targetState.trim())) {
                return ACCEPTED;
            } else {
                return DECLINED;
            }
        }
    },
    ACCEPTED("accepted") {

        @Override
        public TechnologyState changeState(final String targetState) {
            if (targetState.trim().isEmpty() || "outdated".equalsIgnoreCase(targetState.trim())) {
                return OUTDATED;
            }
            return this;
        }
    },
    DECLINED("declined") {

        @Override
        public TechnologyState changeState(final String targetState) {
            return this;
        }
    },
    OUTDATED("outdated") {

        @Override
        public TechnologyState changeState(final String targetState) {
            return this;
        }
    };

    private String stringValue;

    private TechnologyState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public abstract TechnologyState changeState(final String targetState);
}
