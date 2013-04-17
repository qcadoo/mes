/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.orders.constants;

public final class OrdersConstants {

    private OrdersConstants() {

    }

    public static final String PLUGIN_IDENTIFIER = "orders";

    // MODEL
    public static final String MODEL_ORDER = "order";

    public static final String MODEL_ORDER_STATE_CHANGE = "orderStateChange";

    public static final String MODEL_REASON_TYPE_CORRECTION_DATE_TO = "reasonTypeCorrectionDateTo";

    public static final String MODEL_REASON_TYPE_CORRECTION_DATE_FROM = "reasonTypeCorrectionDateFrom";

    public static final String MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_START = "reasonTypeDeviationEffectiveStart";

    public static final String MODEL_REASON_TYPE_DEVIATION_EFFECTIVE_END = "reasonTypeDeviationEffectiveEnd";

    public static final String MODEL_REASON_TYPE_OF_CHANGING_ORDER_STATE = "reasonTypeOfChangingOrderState";

    public static final String MODEL_TYPE_OF_CORRECTION_CAUSES = "typeOfCorrectionCauses";

    // VIEW
    public static final String VIEW_ORDER_DETAILS = "orderDetails";

    public static final String VIEW_ORDERS_LIST = "ordersList";

    public static final String BASIC_MODEL_PRODUCT = "product";

    public static final String TECHNOLOGIES_MODEL_TECHNOLOGY = "technology";

    public static final String FIELD_STATE = "state";

    public static final String FIELD_NUMBER = "number";

    public static final String FIELD_FORM = "form";

    public static final String FIELD_GRID = "grid";

    public static final String PLANNED_QUANTITY = "plannedQuantity";

    public static final String FIELD_BATCH_REQUIRED = "batchRequired";

}
