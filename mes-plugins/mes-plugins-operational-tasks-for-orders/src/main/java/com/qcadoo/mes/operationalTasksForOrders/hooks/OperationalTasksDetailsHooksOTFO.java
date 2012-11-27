/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.DESCRIPTION;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.NAME;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCTION_LINE;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.TYPE_TASK;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields.ORDER;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class OperationalTasksDetailsHooksOTFO {

    public void disabledFieldWhenOrderTypeIsSelected(final ViewDefinitionState view) {

        FieldComponent type = (FieldComponent) view.getComponentByReference(TYPE_TASK);

        List<String> referenceBasicFields = Lists.newArrayList(NAME, PRODUCTION_LINE, DESCRIPTION);
        List<String> extendFields = Lists.newArrayList(ORDER, TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        if (type.getFieldValue().equals("01otherCase")) {
            changedStateField(view, referenceBasicFields, true);
            changedStateField(view, extendFields, false);
            clearFieldValue(view, extendFields);
        } else {
            changedStateField(view, referenceBasicFields, false);
            changedStateField(view, extendFields, true);
        }
    }

    private void clearFieldValue(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(null);
            field.requestComponentUpdateState();
        }
    }

    private void changedStateField(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setEnabled(enabled);
            field.requestComponentUpdateState();
        }
    }

    public void disabledButtons(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        FieldComponent typeTask = (FieldComponent) view.getComponentByReference(OperationalTasksFields.TYPE_TASK);
        boolean isSelectedExecutionOperationInOrder = typeTask.getFieldValue().equals("02executionOperationInOrder");

        RibbonActionItem showOrder = window.getRibbon().getGroupByName("order").getItemByName("showOrder");
        showOrder.setEnabled(isSelectedExecutionOperationInOrder);
        showOrder.requestUpdate(true);

        RibbonActionItem showOperationParameter = window.getRibbon().getGroupByName("techInstOperComp")
                .getItemByName("showOperationParameter");
        showOperationParameter.setEnabled(isSelectedExecutionOperationInOrder);
        showOperationParameter.requestUpdate(true);

        RibbonActionItem showOperationalTasksWithOrder = window.getRibbon().getGroupByName("operationalTasksList")
                .getItemByName("showOperationalTasksWithOrder");
        showOperationalTasksWithOrder.setEnabled(isSelectedExecutionOperationInOrder);
        showOperationalTasksWithOrder.requestUpdate(true);
    }
}
