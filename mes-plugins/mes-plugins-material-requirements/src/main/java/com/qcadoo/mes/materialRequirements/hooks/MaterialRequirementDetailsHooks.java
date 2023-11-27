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
package com.qcadoo.mes.materialRequirements.hooks;

import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementFields;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementsConstants;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MaterialRequirementDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        generateMaterialRequirementNumber(view);
        disableFormForExistingMaterialRequirement(view);
    }

    private void generateMaterialRequirementNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT, QcadooViewConstants.L_FORM,
                MaterialRequirementFields.NUMBER);
    }

    private void disableFormForExistingMaterialRequirement(final ViewDefinitionState view) {
        FormComponent materialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.GENERATED);
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.NUMBER);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.NAME);
        FieldComponent mrpAlgorithmField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.MRP_ALGORITHM);
        CheckBoxComponent includeWarehouseCheckBox = (CheckBoxComponent) view
                .getComponentByReference(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        CheckBoxComponent showCurrentStockLevelCheckBox = (CheckBoxComponent) view
                .getComponentByReference(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);
        CheckBoxComponent includeStartDateOrderCheckBox = (CheckBoxComponent) view
                .getComponentByReference(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(MaterialRequirementFields.LOCATION);

        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(MaterialRequirementFields.ORDERS);

        boolean isSaved = Objects.nonNull(materialRequirementForm.getEntityId());
        boolean isGenerated = "1".equals(generatedField.getFieldValue());
        boolean includeWarehouse = includeWarehouseCheckBox.isChecked();

        numberField.setEnabled(!isGenerated);
        nameField.setEnabled(!isGenerated);
        mrpAlgorithmField.setEnabled(!isGenerated);
        includeWarehouseCheckBox.setEnabled(!isGenerated);
        showCurrentStockLevelCheckBox.setEnabled(!isGenerated && includeWarehouse);
        includeStartDateOrderCheckBox.setEnabled(!isGenerated);
        locationLookup.setEnabled(!isGenerated && includeWarehouse);
        ordersGrid.setEnabled(isSaved && !isGenerated);

        if (!includeWarehouse) {
            showCurrentStockLevelCheckBox.setChecked(false);
            locationLookup.setFieldValue(null);
        }

        showCurrentStockLevelCheckBox.requestComponentUpdateState();
        locationLookup.requestComponentUpdateState();
    }

}
