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
package com.qcadoo.mes.workPlans.hooks;

import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ParameterHooksWP {

    public void onCreate(final DataDefinition parameterDD, final Entity parameter) {
        addFieldsForParameter(parameter);
    }

    private void addFieldsForParameter(final Entity parameter) {
        parameter.setField(ParameterFieldsWP.DONT_PRINT_ORDERS_IN_WORK_PLANS, false);
        parameter.setField(ParameterFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS, false);
        parameter.setField(ParameterFieldsWP.HIDE_DETAILS_IN_WORK_PLANS, false);
        parameter.setField(ParameterFieldsWP.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS, false);
        parameter.setField(ParameterFieldsWP.IMAGE_URL_IN_WORK_PLAN, false);
        parameter.setField(ParameterFieldsWP.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS, false);
        parameter.setField(ParameterFieldsWP.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS, false);
        parameter.setField(ParameterFieldsWP.ADDITIONAL_INPUT_ROWS, 0);
        parameter.setField(ParameterFieldsWP.ADDITIONAL_OUTPUT_ROWS, 0);
    }

}
