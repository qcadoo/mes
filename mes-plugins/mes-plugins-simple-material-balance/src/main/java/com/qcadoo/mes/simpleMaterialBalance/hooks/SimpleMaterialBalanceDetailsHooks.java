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
package com.qcadoo.mes.simpleMaterialBalance.hooks;

import static com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceFields.MRPALGORITHM;
import static com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceFields.NAME;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class SimpleMaterialBalanceDetailsHooks {

    public void disableFieldsWhenGenerated(final ViewDefinitionState view) {
        Boolean enabled = false;
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        ComponentState generated = (ComponentState) view.getComponentByReference("generated");
        if (generated == null || generated.getFieldValue() == null || "0".equals(generated.getFieldValue())) {
            enabled = true;
        }
        for (String reference : Arrays.asList(NAME, MRPALGORITHM)) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(reference);
            component.setEnabled(enabled);
        }
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS))
                .setEnabled(enabled);
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS))
                .setEditable(enabled);
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS))
                .setEnabled(enabled);
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS))
                .setEditable(enabled);
    }
}
