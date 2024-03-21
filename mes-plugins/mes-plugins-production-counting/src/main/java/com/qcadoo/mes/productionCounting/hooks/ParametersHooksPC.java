/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
import com.qcadoo.mes.productionCounting.constants.ReceiptOfProducts;
import com.qcadoo.mes.productionCounting.constants.ReleaseOfMaterials;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ParametersHooksPC {

    public void onBeforeRender(final ViewDefinitionState view) {
        checkIfRegisterProductionTimeIsSet(view);
        setStatePriceBasedOn(view);
        setStateConsumptionOfRawMaterialsBasedOnStandards(view);
    }

    private void setStateConsumptionOfRawMaterialsBasedOnStandards(ViewDefinitionState view) {
        CheckBoxComponent consumptionOfRawMaterialsBasedOnStandards = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsPC.CONSUMPTION_OF_RAW_MATERIALS_BASED_ON_STANDARDS);
        FieldComponent releaseOfMaterials = (FieldComponent) view.getComponentByReference(ParameterFieldsPC.RELEASE_OF_MATERIALS);
        consumptionOfRawMaterialsBasedOnStandards.setEnabled(!ReleaseOfMaterials.MANUALLY_TO_ORDER_OR_GROUP.getStringValue().equals(releaseOfMaterials.getFieldValue().toString()));
        consumptionOfRawMaterialsBasedOnStandards.requestComponentUpdateState();
        CheckBoxComponent wastesConsumeRawMaterials = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsPC.WASTES_CONSUME_RAW_MATERIALS);
        if (consumptionOfRawMaterialsBasedOnStandards.isChecked()) {
            wastesConsumeRawMaterials.setEnabled(true);
        } else {
            wastesConsumeRawMaterials.setChecked(true);
            wastesConsumeRawMaterials.setEnabled(false);
        }
    }

    private void setStatePriceBasedOn(ViewDefinitionState view) {
        FieldComponent priceBasedOn = (FieldComponent) view.getComponentByReference(ParameterFieldsPC.PRICE_BASED_ON);
        FieldComponent receiptOfProducts = (FieldComponent) view.getComponentByReference(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
        priceBasedOn.setEnabled(ReceiptOfProducts.END_OF_THE_ORDER.getStringValue()
                .equals(receiptOfProducts.getFieldValue().toString()));
        priceBasedOn.requestComponentUpdateState();
    }

    private void checkIfRegisterProductionTimeIsSet(final ViewDefinitionState viewDefinitionState) {
        CheckBoxComponent registerProductionTime = (CheckBoxComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPC.REGISTER_PRODUCTION_TIME);
        CheckBoxComponent validateProductionRecordTimes = (CheckBoxComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPC.VALIDATE_PRODUCTION_RECORD_TIMES);
        if (registerProductionTime.isChecked()) {
            validateProductionRecordTimes.setEnabled(true);
        } else {
            validateProductionRecordTimes.setEnabled(false);
            validateProductionRecordTimes.setChecked(false);
        }
        validateProductionRecordTimes.requestComponentUpdateState();
    }

}
