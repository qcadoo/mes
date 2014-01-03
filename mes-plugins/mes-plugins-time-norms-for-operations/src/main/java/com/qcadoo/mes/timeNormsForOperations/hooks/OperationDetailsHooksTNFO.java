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
package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationDetailsHooksTNFO {

    @Autowired
    private UnitService unitService;

    public void setNextOperationAfterProducedTypeOperationValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        if (!"02specified".equals(nextOperationAfterProducedType.getFieldValue())) {
            nextOperationAfterProducedType.setFieldValue("01all");

        }
    }

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {

        FieldComponent nextOperationAfterProducedType = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_TYPE);
        FieldComponent nextOperationAfterProducedQuantity = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY);
        FieldComponent nextOperationAfterProducedQuantityUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT);
        FieldComponent areProductQuantitiesDivisible = (FieldComponent) viewDefinitionState
                .getComponentByReference("areProductQuantitiesDivisible");
        FieldComponent isTjDivisible = (FieldComponent) viewDefinitionState.getComponentByReference("isTjDivisible");

        nextOperationAfterProducedType.setRequired(true);

        if (nextOperationAfterProducedType.getFieldValue().equals("02specified")) {
            nextOperationAfterProducedQuantity.setVisible(true);
            nextOperationAfterProducedQuantity.setEnabled(true);
            nextOperationAfterProducedQuantityUNIT.setVisible(true);
            nextOperationAfterProducedQuantityUNIT.setEnabled(true);
        } else {
            nextOperationAfterProducedQuantity.setVisible(false);
            nextOperationAfterProducedQuantityUNIT.setVisible(false);
        }
        nextOperationAfterProducedQuantity.requestComponentUpdateState();
        if ("1".equals(areProductQuantitiesDivisible.getFieldValue())) {
            isTjDivisible.setEnabled(true);
        }
    }

    public void setDefaultUnit(final ViewDefinitionState viewDefinitionState) {
        String defaultUnit = unitService.getDefaultUnitFromSystemParameters();
        FieldComponent productionInOneCycleUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE_UNIT);

        String unit = (String) productionInOneCycleUNIT.getFieldValue();

        if (defaultUnit != null && (unit == null || "".equals(unit))) {
            productionInOneCycleUNIT.setFieldValue(defaultUnit);
        }

    }
}
