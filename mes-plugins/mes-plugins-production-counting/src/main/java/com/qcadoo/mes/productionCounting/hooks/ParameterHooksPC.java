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
package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksPC {

    public void onCreate(final DataDefinition parameterDD, final Entity parameter) {
        setParameterWithDefaultProductionCountingValues(parameterDD, parameter);
    }

    private void setParameterWithDefaultProductionCountingValues(final DataDefinition parameterDD, final Entity parameter) {
        if (parameter.getStringField(ParameterFieldsPC.TYPE_OF_PRODUCTION_RECORDING) == null) {
            parameter.setField(ParameterFieldsPC.TYPE_OF_PRODUCTION_RECORDING,
                    TypeOfProductionRecording.CUMULATED.getStringValue());
        }
        if (parameter.getField(ParameterFieldsPC.REGISTER_PRODUCTION_TIME) == null) {
            parameter.setField(ParameterFieldsPC.REGISTER_PRODUCTION_TIME, true);
        }
        if (parameter.getField(ParameterFieldsPC.REGISTER_PIECEWORK) == null) {
            parameter.setField(ParameterFieldsPC.REGISTER_PIECEWORK, false);
        }
        if (parameter.getField(ParameterFieldsPC.REGISTER_QUANTITY_IN_PRODUCT) == null) {
            parameter.setField(ParameterFieldsPC.REGISTER_QUANTITY_IN_PRODUCT, true);
        }
        if (parameter.getField(ParameterFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT) == null) {
            parameter.setField(ParameterFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, true);
        }
        if (parameter.getField(ParameterFieldsPC.JUST_ONE) == null) {
            parameter.setField(ParameterFieldsPC.JUST_ONE, false);
        }
        if (parameter.getField(ParameterFieldsPC.ALLOW_TO_CLOSE) == null) {
            parameter.setField(ParameterFieldsPC.ALLOW_TO_CLOSE, false);
        }
        if (parameter.getField(ParameterFieldsPC.AUTO_CLOSE_ORDER) == null) {
            parameter.setField(ParameterFieldsPC.AUTO_CLOSE_ORDER, false);
        }
    }

}
