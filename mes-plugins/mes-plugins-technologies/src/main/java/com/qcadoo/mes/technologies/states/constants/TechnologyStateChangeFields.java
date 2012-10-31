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
package com.qcadoo.mes.technologies.states.constants;

public final class TechnologyStateChangeFields {

    private TechnologyStateChangeFields() {
    }

    public static final String SHIFT = "shift";

    public static final String WORKER = "worker";

    public static final String STATUS = "status";

    public static final String TECHNOLOGY = "technology";

    public static final String TARGET_STATE = "targetState";

    public static final String SOURCE_STATE = "sourceState";

    public static final String MESSAGES = "messages";

    public static final String ADDITIONAL_INFO = "additionalInformation";

    public static final String REASON_REQUIRED = "reasonRequired";

    public static final String REASON_TYPE = "reasonType";

}
