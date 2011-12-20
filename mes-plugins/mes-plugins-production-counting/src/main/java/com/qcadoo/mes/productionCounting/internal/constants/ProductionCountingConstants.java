/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

public interface ProductionCountingConstants {

    String PLUGIN_IDENTIFIER = "productionCounting";

    // MODEL
    String MODEL_PRODUCTION_RECORD = "productionRecord";

    String MODEL_PRODUCTION_BALANCE = "productionBalance";

    String MODEL_PRODUCTION_COUNTING = "productionCounting";

    String MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT = "recordOperationProductInComponent";

    String MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT = "recordOperationProductOutComponent";

    // VIEW
    String VIEW_PRODUCTION_RECORDS_LIST = "productionRecordsList";

    String VIEW_PRODUCTION_RECORD_DETAILS = "productionRecordDetails";

    String VIEW_PRODUCTION_BALANCES_LIST = "productionBalancesList";

    String VIEW_PRODUCTION_BALANCE_DETAILS = "productionBalanceDetails";

    String VIEW_PRODUCTION_COUNTINGS_LIST = "productionCountingsList";

    String VIEW_PRODUCTION_COUNTING_DETAILS = "productionCountingDetails";

    String VIEW_RECORD_OPERATION_PRODUCT_IN_COMPONENT_DETAILS = "recordOperationProductInComponentDetails";

    String VIEW_RECORD_OPERATION_PRODUCT_OUT_COMPONENT_DETAILS = "recordOperationProductOutComponentDetails";

    // RECORDING PARAMETERS
    String PARAM_REGISTER_OUT_PRODUCTS = "registerQuantityOutProduct";

    String PARAM_REGISTER_IN_PRODUCTS = "registerQuantityInProduct";

    String PARAM_REGISTER_TIME = "registerProductionTime";

    String PARAM_RECORDING_TYPE_NONE = "01none";

    String PARAM_RECORDING_TYPE_CUMULATED = "02cumulated";

    String PARAM_RECORDING_TYPE_FOREACH = "03forEach";

    String PARAM_RECORDING_TYPE_BASIC = "01basic";
}
