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
package com.qcadoo.mes.basic.hooks;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class SubassemblyDetailsHooks {

    private static final String L_FORM = "form";

    public void setSubassemblyIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent subassemblyIdForMultiUpload = (FieldComponent) view.getComponentByReference("subassemblyIdForMultiUpload");
        FieldComponent subassemblyMultiUploadLocale = (FieldComponent) view
                .getComponentByReference("subassemblyMultiUploadLocale");

        if (form.getEntityId() != null) {
            subassemblyIdForMultiUpload.setFieldValue(form.getEntityId());
            subassemblyIdForMultiUpload.requestComponentUpdateState();
        } else {
            subassemblyIdForMultiUpload.setFieldValue("");
            subassemblyIdForMultiUpload.requestComponentUpdateState();
        }
        subassemblyMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        subassemblyMultiUploadLocale.requestComponentUpdateState();

    }
}
