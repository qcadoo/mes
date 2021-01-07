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
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaterialRequirementDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateMaterialRequirementNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT, QcadooViewConstants.L_FORM,
                MaterialRequirementFields.NUMBER);
    }

    public void disableFormForExistingMaterialRequirement(final ViewDefinitionState view) {
        FormComponent materialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (materialRequirementForm.getEntityId() == null) {
            return;
        }

        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.GENERATED);
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.NUMBER);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.NAME);
        FieldComponent mrpAlgorithmField = (FieldComponent) view.getComponentByReference(MaterialRequirementFields.MRP_ALGORITHM);
        CheckBoxComponent includeWarehouseField = (CheckBoxComponent) view
                .getComponentByReference(MaterialRequirementFields.INCLUDE_WAREHOUSE);
        CheckBoxComponent showCurrentStockLevelField = (CheckBoxComponent) view
                .getComponentByReference(MaterialRequirementFields.SHOW_CURRENT_STOCK_LEVEL);
        CheckBoxComponent includeStartDateOrderField = (CheckBoxComponent) view
                .getComponentByReference(MaterialRequirementFields.INCLUDE_START_DATE_ORDER);

        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(MaterialRequirementFields.ORDERS);

        boolean isGenerated = "1".equals(generatedField.getFieldValue());

        numberField.setEnabled(!isGenerated);
        nameField.setEnabled(!isGenerated);
        mrpAlgorithmField.setEnabled(!isGenerated);
        includeWarehouseField.setEnabled(!isGenerated);
        showCurrentStockLevelField.setEnabled(!isGenerated);
        includeStartDateOrderField.setEnabled(!isGenerated);
        ordersGrid.setEnabled(!isGenerated);
    }

}
