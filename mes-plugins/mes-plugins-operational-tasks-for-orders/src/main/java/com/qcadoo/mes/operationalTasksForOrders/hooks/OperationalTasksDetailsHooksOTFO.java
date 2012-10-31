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

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationalTasksDetailsHooksOTFO {

    public void disabledFieldWhenOrderTypeIsSelected(final ViewDefinitionState view) {

        FieldComponent type = (FieldComponent) view.getComponentByReference(OperationalTasksFields.TYPE_TASK);

        List<String> referenceBasicFields = Lists.newArrayList(OperationalTasksFields.NAME,
                OperationalTasksFields.PRODUCTION_LINE, OperationalTasksFields.DESCRIPTION);
        List<String> extendFields = Lists.newArrayList(OperationalTasksOTFRFields.ORDER,
                OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
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
}
