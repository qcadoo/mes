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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ParametersHooksPC {

    @Autowired
    private ProductionCountingService productionCountingService;

    public void checkIfTypeIsCumulatedAndRegisterPieceworkIsFalse(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecordingField = (FieldComponent) view
                .getComponentByReference(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent registerPieceworkField = (FieldComponent) view
                .getComponentByReference(OrderFieldsPC.REGISTER_PIECEWORK);

        String typeOfProductionRecording = (String) typeOfProductionRecordingField.getFieldValue();

        if ((typeOfProductionRecording != null)
                && productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
            registerPieceworkField.setFieldValue(false);
            registerPieceworkField.setEnabled(false);
        } else {
            registerPieceworkField.setEnabled(true);
        }
        registerPieceworkField.requestComponentUpdateState();
    }

}
