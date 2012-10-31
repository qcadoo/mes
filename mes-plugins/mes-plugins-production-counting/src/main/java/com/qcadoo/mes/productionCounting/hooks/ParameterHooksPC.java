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
package com.qcadoo.mes.productionCounting.hooks;

import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.ALLOW_TO_CLOSE;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.AUTO_CLOSE_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.JUST_ONE;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.ParameterFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksPC {

    public void setParameterWithDefaultProductionCountingValues(final DataDefinition parameterDD, final Entity parameter) {
        if (parameter.getStringField(TYPE_OF_PRODUCTION_RECORDING) == null) {
            parameter.setField(TYPE_OF_PRODUCTION_RECORDING, CUMULATED.getStringValue());
        }
        if (parameter.getField(REGISTER_PRODUCTION_TIME) == null) {
            parameter.setField(REGISTER_PRODUCTION_TIME, true);
        }
        if (parameter.getField(REGISTER_PIECEWORK) == null) {
            parameter.setField(REGISTER_PIECEWORK, false);
        }
        if (parameter.getField(REGISTER_QUANTITY_IN_PRODUCT) == null) {
            parameter.setField(REGISTER_QUANTITY_IN_PRODUCT, true);
        }
        if (parameter.getField(REGISTER_QUANTITY_OUT_PRODUCT) == null) {
            parameter.setField(REGISTER_QUANTITY_OUT_PRODUCT, true);
        }
        if (parameter.getField(JUST_ONE) == null) {
            parameter.setField(JUST_ONE, false);
        }
        if (parameter.getField(ALLOW_TO_CLOSE) == null) {
            parameter.setField(ALLOW_TO_CLOSE, false);
        }
        if (parameter.getField(AUTO_CLOSE_ORDER) == null) {
            parameter.setField(AUTO_CLOSE_ORDER, false);
        }
    }

}
