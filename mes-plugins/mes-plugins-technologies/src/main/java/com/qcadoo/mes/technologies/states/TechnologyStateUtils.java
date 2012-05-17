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
package com.qcadoo.mes.technologies.states;

import com.qcadoo.mes.technologies.constants.TechnologyState;

public final class TechnologyStateUtils {

    private TechnologyStateUtils() {
    }

    public static TechnologyState getStateFromField(final String fieldValue) {
        if ("01draft".equals(fieldValue)) {
            return TechnologyState.DRAFT;
        }
        if ("02accepted".equals(fieldValue)) {
            return TechnologyState.ACCEPTED;
        }
        if ("03declined".equals(fieldValue)) {
            return TechnologyState.DECLINED;
        }
        if ("04outdated".equals(fieldValue)) {
            return TechnologyState.OUTDATED;
        }
        if ("05checked".equals(fieldValue)) {
            return TechnologyState.CHECKED;
        }

        throw new IllegalArgumentException("Unsupported or unspecified technology state " + fieldValue);
    }
}
