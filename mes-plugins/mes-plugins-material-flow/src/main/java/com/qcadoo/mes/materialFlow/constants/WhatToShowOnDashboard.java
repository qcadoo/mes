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
package com.qcadoo.mes.materialFlow.constants;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum WhatToShowOnDashboard {

    ORDERS("01orders"), OPERATIONAL_TASKS("02operationalTasks");

    private final String whatToShowOnDashboard;

    private WhatToShowOnDashboard(final String whatToShowOnDashboard) {
        this.whatToShowOnDashboard = whatToShowOnDashboard;
    }

    public String getStringValue() {
        return whatToShowOnDashboard;
    }

    public static WhatToShowOnDashboard of(final Entity parameter) {
        Preconditions.checkArgument(Objects.nonNull(parameter), "Missing entity");

        return parseString(parameter.getStringField(ParameterFieldsMF.WHAT_TO_SHOW_ON_DASHBOARD));
    }

    public static WhatToShowOnDashboard parseString(final String rawWhatToShowOnDashboard) {
        String whatToShowOnDashboard = StringUtils.trim(rawWhatToShowOnDashboard);

        for (WhatToShowOnDashboard value : values()) {
            if (StringUtils.equalsIgnoreCase(value.getStringValue(), whatToShowOnDashboard)) {
                return value;
            }
        }

        throw new IllegalStateException("Unsupported WhatToShowOnDashboard: " + whatToShowOnDashboard);
    }

}
