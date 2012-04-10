/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.timeNormsForOperations;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationService {

    public void changeCountRealizedOperation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countMachineOperation");
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineUNIT");

        if (countRealizedOperation.getFieldValue().equals("02specified")) {
            countMachineOperation.setVisible(true);
            countMachineUNIT.setVisible(true);

        } else {
            countMachineOperation.setVisible(false);
            countMachineUNIT.setVisible(false);
        }
        countMachineOperation.requestComponentUpdateState();
    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {

        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countMachineOperation");
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineUNIT");

        countRealizedOperation.setRequired(true);

        if (countRealizedOperation.getFieldValue().equals("02specified")) {
            countMachineOperation.setVisible(true);
            countMachineOperation.setEnabled(true);
            countMachineUNIT.setVisible(true);
            countMachineUNIT.setEnabled(true);
        } else {
            countMachineOperation.setVisible(false);
            countMachineUNIT.setVisible(false);
        }
        countMachineOperation.requestComponentUpdateState();
    }

    public void setCountRealizedOperationValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        if (!"02specified".equals(countRealizedOperation.getFieldValue())) {
            countRealizedOperation.setFieldValue("01all");

        }
    }
}
