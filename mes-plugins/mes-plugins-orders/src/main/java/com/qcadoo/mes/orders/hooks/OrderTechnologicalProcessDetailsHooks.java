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
package com.qcadoo.mes.orders.hooks;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessDetailsHooks {

    private static final String L_PROCESS = "process";

    private static final String L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS = "divideOrderTechnologicalProcess";

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    public final void onBeforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
        fillTechnologicalProcessName(view);
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup processGroup = window.getRibbon().getGroupByName(L_PROCESS);
        RibbonActionItem splitOrderTechnologicalProcessActionItem = processGroup
                .getItemByName(L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS);

        Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
        Entity orderPack = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);
        BigDecimal quantity = orderTechnologicalProcess.getDecimalField(OrderTechnologicalProcessFields.QUANTITY);

        boolean isEnabled = Objects.nonNull(orderTechnologicalProcess.getId()) && Objects.nonNull(orderPack)
                && (BigDecimal.ONE.compareTo(quantity) < 0);

        splitOrderTechnologicalProcessActionItem.setEnabled(isEnabled);
        splitOrderTechnologicalProcessActionItem.requestUpdate(true);
    }

    private void fillTechnologicalProcessName(final ViewDefinitionState view) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent technologicalProcessNameField = (FieldComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_NAME);

        Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
        Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

        if (Objects.nonNull(orderTechnologicalProcessId)) {
            orderTechnologicalProcess = orderTechnologicalProcess.getDataDefinition().get(orderTechnologicalProcessId);

            Entity technologicalProcess = orderTechnologicalProcess
                    .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

            technologicalProcessNameField.setFieldValue(technologicalProcess.getStringField(TechnologicalProcessFields.NAME));
            technologicalProcessNameField.requestComponentUpdateState();
        }
    }

}
