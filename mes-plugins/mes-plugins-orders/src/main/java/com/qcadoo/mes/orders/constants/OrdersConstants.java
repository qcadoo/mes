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
package com.qcadoo.mes.orders.constants;

public interface OrdersConstants {

    String PLUGIN_IDENTIFIER = "orders";

    // MODEL
    String MODEL_ORDER = "order";

    String MODEL_ORDER_STATE_CHANGE = "orderStateChange";

    String MODEL_LOGGING = "logging";

    // VIEW
    String VIEW_ORDER_DETAILS = "orderDetails";

    String VIEW_ORDERS_LIST = "ordersList";

    String BASIC_MODEL_PRODUCT = "product";

    String TECHNOLOGIES_MODEL_TECHNOLOGY = "technology";

    String FIELD_STATE = "state";

    String FIELD_NUMBER = "number";

    String FIELD_FORM = "form";

    String FIELD_GRID = "grid";

    String PLANNED_QUANTITY = "plannedQuantity";

    String FIELD_BATCH_REQUIRED = "batchRequired";
}
