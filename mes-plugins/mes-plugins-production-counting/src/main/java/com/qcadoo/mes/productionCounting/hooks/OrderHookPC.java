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
package com.qcadoo.mes.productionCounting.hooks;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.ALLOW_TO_CLOSE;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.AUTO_CLOSE_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.JUST_ONE;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHookPC {

    @Autowired
    private ParameterService parameterService;

    public void setOrderWithDefaultProductionCountingValues(final DataDefinition dataDefinition, final Entity order) {
        for (String fieldName : Arrays.asList(TYPE_OF_PRODUCTION_RECORDING, REGISTER_PIECEWORK, REGISTER_QUANTITY_IN_PRODUCT,
                REGISTER_QUANTITY_OUT_PRODUCT, JUST_ONE, ALLOW_TO_CLOSE, AUTO_CLOSE_ORDER)) {
            if (order.getStringField(fieldName) == null) {
                order.setField(fieldName, parameterService.getParameter().getField(fieldName));
            }
        }
    }

}
