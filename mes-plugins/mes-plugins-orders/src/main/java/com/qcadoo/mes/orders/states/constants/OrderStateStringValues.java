/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class OrderStateStringValues {

    private OrderStateStringValues() {
    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    public static final String PENDING = "01pending";

    public static final String ACCEPTED = "02accepted";

    public static final String IN_PROGRESS = "03inProgress";

    public static final String COMPLETED = "04completed";

    public static final String DECLINED = "05declined";

    public static final String INTERRUPTED = "06interrupted";

    public static final String ABANDONED = "07abandoned";

}
