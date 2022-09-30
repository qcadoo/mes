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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersMaterialRequirementFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MasterOrdersMaterialRequirementDetailsHooks {

    private static final String L_GENERATE = "generate";

    private static final String L_GENERATE_MASTER_ORDERS_MATERIAL_REQUIREMENT = "generateMasterOrdersMaterialRequirement";

	private static final String L_INCLUDE_COMPONENTS = "includeComponents";

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setRibbonEnabled(view);
        setFormEnabled(view);
        setIncludeComponents(view);
    }

    private void setRibbonEnabled(final ViewDefinitionState view) {
        FormComponent masterOrdersMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(MasterOrdersMaterialRequirementFields.GENERATED);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup generateRibbonGroup = ribbon.getGroupByName(L_GENERATE);

        RibbonActionItem generateRibbonActionItem = generateRibbonGroup.getItemByName(L_GENERATE_MASTER_ORDERS_MATERIAL_REQUIREMENT);

        Long masterOrdersMaterialRequirementId = masterOrdersMaterialRequirementForm.getEntityId();

        boolean isSaved = Objects.nonNull(masterOrdersMaterialRequirementId);
        boolean isGenerated = generatedCheckBox.isChecked();

        generateRibbonActionItem.setEnabled(isSaved && !isGenerated);
        generateRibbonActionItem.requestUpdate(true);
    }

    private void setFormEnabled(final ViewDefinitionState view) {
        FormComponent masterOrdersMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent masterOrdersGrid = (GridComponent) view.getComponentByReference(MasterOrdersMaterialRequirementFields.MASTER_ORDERS);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(MasterOrdersMaterialRequirementFields.GENERATED);

        Long masterOrdersMaterialRequirementId = masterOrdersMaterialRequirementForm.getEntityId();

        boolean isSaved = Objects.nonNull(masterOrdersMaterialRequirementId);
        boolean isGenerated = generatedCheckBox.isChecked();

        masterOrdersMaterialRequirementForm.setFormEnabled(!isSaved || !isGenerated);
        masterOrdersGrid.setEnabled(isSaved && !isGenerated);
    }

    private void setIncludeComponents(final ViewDefinitionState view) {
        FormComponent masterOrdersMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent includeComponentsCheckBox = (CheckBoxComponent) view.getComponentByReference(MasterOrdersMaterialRequirementFields.INCLUDE_COMPONENTS);

        Long masterOrdersMaterialRequirementId = masterOrdersMaterialRequirementForm.getEntityId();

        boolean isSaved = Objects.nonNull(masterOrdersMaterialRequirementId);

        if (!isSaved) {
            boolean includeComponents = parameterService.getParameter().getBooleanField(L_INCLUDE_COMPONENTS);

            includeComponentsCheckBox.setChecked(includeComponents);
            includeComponentsCheckBox.requestComponentUpdateState();
        }
    }

}
