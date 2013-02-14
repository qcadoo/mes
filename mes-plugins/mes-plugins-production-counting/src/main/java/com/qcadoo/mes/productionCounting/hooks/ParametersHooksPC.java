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

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ParametersHooksPC {

    public void checkIfTypeIsCumulatedAndRegisterPieceworkIsFalse(final ViewDefinitionState viewDefinitionState) {
        String typeOfProductionRecording = ((FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING)).getFieldValue().toString();
        FieldComponent registerPiecework = (FieldComponent) viewDefinitionState.getComponentByReference(REGISTER_PIECEWORK);
        if (typeOfProductionRecording != null
                && typeOfProductionRecording.equals(TypeOfProductionRecording.CUMULATED.getStringValue())) {
            registerPiecework.setFieldValue(false);
            registerPiecework.setEnabled(false);
        } else {
            registerPiecework.setEnabled(true);
        }
        registerPiecework.requestComponentUpdateState();
    }

}
