/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

public final class OrderStateChangeFields {

    private OrderStateChangeFields() {
    }

    public static final String SHIFT = "shift";

    public static final String WORKER = "worker";

    public static final String STATUS = "status";

    public static final String ORDER = "order";

    public static final String TARGET_STATE = "targetState";

    public static final String SOURCE_STATE = "sourceState";

    public static final String MESSAGES = "messages";

    public static final String ADDITIONAL_INFO = "additionalInformation";

    public static final String REASON_REQUIRED = "reasonRequired";

    public static final String REASON_TYPES = "reasonTypes";

    public static final String SOURCE_CORRECTED_DATE_FROM = "sourceCorrectedDateFrom";

    public static final String SOURCE_CORRECTED_DATE_TO = "sourceCorrectedDateTo";

    public static final String SOURCE_START_DATE = "sourceStartDate";

    public static final String SOURCE_FINISH_DATE = "sourceFinishDate";

    public static final String TARGET_CORRECTED_DATE_FROM = "targetCorrectedDateFrom";

    public static final String TARGET_CORRECTED_DATE_TO = "targetCorrectedDateTo";

    public static final String TARGET_START_DATE = "targetStartDate";

    public static final String TARGET_FINISH_DATE = "targetFinishDate";

    public static final String DATES_CHANGED = "datesChanged";

}
