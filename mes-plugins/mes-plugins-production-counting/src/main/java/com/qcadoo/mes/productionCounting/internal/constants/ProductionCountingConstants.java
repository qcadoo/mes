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
package com.qcadoo.mes.productionCounting.internal.constants;

public final class ProductionCountingConstants {

    private ProductionCountingConstants() {
    }

    public static final String PLUGIN_IDENTIFIER = "productionCounting";

    // MODEL
    public static final String MODEL_PRODUCTION_RECORD = "productionRecord";

    public static final String MODEL_PRODUCTION_RECORD_STATE_CHANGE = "productionRecordStateChange";

    public static final String MODEL_PRODUCTION_BALANCE = "productionBalance";

    public static final String MODEL_PRODUCTION_COUNTING = "productionCounting";

    public static final String MODEL_OPERATION_TIME_COMPONENT = "operationTimeComponent";

    public static final String MODEL_OPERATION_PIECEWORK_COMPONENT = "operationPieceworkComponent";

    public static final String MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT = "recordOperationProductInComponent";

    public static final String MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT = "recordOperationProductOutComponent";

    public static final String MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT = "balanceOperationProductInComponent";

    public static final String MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT = "balanceOperationProductOutComponent";

    // VIEW
    public static final String VIEW_PRODUCTION_RECORDS_LIST = "productionRecordsList";

    public static final String VIEW_PRODUCTION_RECORD_DETAILS = "productionRecordDetails";

    public static final String VIEW_PRODUCTION_BALANCES_LIST = "productionBalancesList";

    public static final String VIEW_PRODUCTION_BALANCE_DETAILS = "productionBalanceDetails";

    public static final String VIEW_PRODUCTION_COUNTINGS_LIST = "productionCountingsList";

    public static final String VIEW_PRODUCTION_COUNTING_DETAILS = "productionCountingDetails";

    public static final String VIEW_RECORD_OPERATION_PRODUCT_IN_COMPONENT_DETAILS = "recordOperationProductInComponentDetails";

    public static final String VIEW_RECORD_OPERATION_PRODUCT_OUT_COMPONENT_DETAILS = "recordOperationProductOutComponentDetails";
}
