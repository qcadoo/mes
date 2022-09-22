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
package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.cmmsMachineParts.constants.OrdersToolRequirementFields;
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
public class OrdersToolRequirementDetailsHooks {

    private static final String L_GENERATE = "generate";

    private static final String L_GENERATE_ORDERS_TOOL_REQUIREMENT = "generateOrdersToolRequirement";

    private static final String L_EXPORT = "export";

    private static final String L_PRINT_ORDERS_TOOL_REQUIREMENT = "printOrdersToolRequirement";

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setRibbonEnabled(view);
        setFormEnabled(view);
    }

    private void setRibbonEnabled(final ViewDefinitionState view) {
        FormComponent ordersToolRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OrdersToolRequirementFields.GENERATED);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();

        RibbonGroup generateRibbonGroup = ribbon.getGroupByName(L_GENERATE);

        RibbonActionItem generateRibbonActionItem = generateRibbonGroup.getItemByName(L_GENERATE_ORDERS_TOOL_REQUIREMENT);

        RibbonGroup exportRibbonGroup = ribbon.getGroupByName(L_EXPORT);

        RibbonActionItem printRibbonActionItem = exportRibbonGroup.getItemByName(L_PRINT_ORDERS_TOOL_REQUIREMENT);

        Long ordersToolRequirementId = ordersToolRequirementForm.getEntityId();

        boolean isSaved = Objects.nonNull(ordersToolRequirementId);
        boolean isGenerated = generatedCheckBox.isChecked();

        generateRibbonActionItem.setEnabled(isSaved && !isGenerated);
        generateRibbonActionItem.requestUpdate(true);

        if (isSaved && !isGenerated) {
            printRibbonActionItem.setMessage("cmmsMachineParts.ordersToolRequirement.ribbon.message.recordNotGenerated");
        }

        printRibbonActionItem.setEnabled(isSaved && isGenerated);
        printRibbonActionItem.requestUpdate(true);
    }

    private void setFormEnabled(final ViewDefinitionState view) {
        FormComponent ordersToolRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(OrdersToolRequirementFields.ORDERS);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OrdersToolRequirementFields.GENERATED);

        Long ordersToolRequirementId = ordersToolRequirementForm.getEntityId();

        boolean isSaved = Objects.nonNull(ordersToolRequirementId);
        boolean isGenerated = generatedCheckBox.isChecked();

        ordersToolRequirementForm.setFormEnabled(!isSaved || !isGenerated);
        ordersGrid.setEnabled(isSaved && !isGenerated);
    }

}
