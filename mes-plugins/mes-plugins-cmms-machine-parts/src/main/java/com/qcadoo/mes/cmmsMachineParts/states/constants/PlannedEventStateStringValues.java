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
package com.qcadoo.mes.cmmsMachineParts.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class PlannedEventStateStringValues {

    public static final String NEW = "01new";

    public static final String IN_PLAN = "02inPlan";

    public static final String PLANNED = "03planned";

    public static final String IN_REALIZATION = "04inRealization";

    public static final String IN_EDITING = "08inEditing";

    public static final String REALIZED = "05realized";

    public static final String CANCELED = "06canceled";

    public static final String ACCEPTED = "07accepted";

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    private PlannedEventStateStringValues() {
    }

}
