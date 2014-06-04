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
package com.qcadoo.mes.workPlans.constants;

import java.util.Set;

import com.google.common.collect.Sets;

public final class WorkPlansConstants {

    private WorkPlansConstants() {

    }

    public static final String PLUGIN_IDENTIFIER = "workPlans";

    // MODEL
    public static final String MODEL_WORK_PLAN = "workPlan";

    public static final String MODEL_WORK_PLAN_ORDER_COLUMN = "workPlanOrderColumn";

    public static final String MODEL_COLUMN_FOR_ORDERS = "columnForOrders";

    public static final String MODEL_COLUMN_FOR_INPUT_PRODUCTS = "columnForInputProducts";

    public static final String MODEL_COLUMN_FOR_OUTPUT_PRODUCTS = "columnForOutputProducts";

    public static final String MODEL_PARAMETER_ORDER_COLUMN = "parameterOrderColumn";

    public static final String MODEL_PARAMETER_INPUT_COLUMN = "parameterInputColumn";

    public static final String MODEL_PARAMETER_OUTPUT_COLUMN = "parameterOutputColumn";

    public static final String MODEL_OPERATION_INPUT_COLUMN = "operationInputColumn";

    public static final String MODEL_OPERATION_OUTPUT_COLUMN = "operationOutputColumn";

    public static final String MODEL_TECHNOLOGY_OPERATION_INPUT_COLUMN = "technologyOperationInputColumn";

    public static final String MODEL_TECHNOLOGY_OPERATION_OUTPUT_COLUMN = "technologyOperationOutputColumn";

    // VIEW
    public static final String VIEW_WORK_PLAN_DETAILS = "workPlanDetails";

    public static final String VIEW_WORK_PLANS_LIST = "workPlansList";

    public static final Set<String> FILE_EXTENSIONS = Sets.newHashSet("bmp", "gif", "jpg", "jpeg", "png", "tiff", "wmf", "eps");

}
