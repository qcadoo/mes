/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

import org.springframework.stereotype.Service;

@Service
public class FormDetailsHooks {

    public final void onBeforeRender(final ViewDefinitionState view) {
        FieldComponent size = (FieldComponent) view.getComponentByReference("size");
        FieldComponent unit = (FieldComponent) view.getComponentByReference("unit");

        if(size.getFieldValue() != null){
            unit.setRequired(true);
            unit.requestComponentUpdateState();
        }else{
            unit.setRequired(false);
            unit.requestComponentUpdateState();
        }
    }
}
