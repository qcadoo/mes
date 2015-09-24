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

import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class MachinePartDetailsHooks {

    private static final String L_FORM = "form";

    public void setMachinePartCheckbox(final ViewDefinitionState view) {
        CheckBoxComponent machinePartCheckbox = (CheckBoxComponent) view.getComponentByReference("machinePart");
        machinePartCheckbox.setChecked(true);
        machinePartCheckbox.requestComponentUpdateState();
    }

    public void toggleSuppliersGrids(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity machinePart = form.getPersistedEntityWithIncludedFormValues();
        GridComponent productCompanies = (GridComponent) view.getComponentByReference("productCompanies");
        GridComponent productsFamilyCompanies = (GridComponent) view.getComponentByReference("productsFamilyCompanies");
        if (ProductFamilyElementType.from(machinePart).compareTo(ProductFamilyElementType.PARTICULAR_PRODUCT) == 0) {
            productCompanies.setVisible(true);
            productsFamilyCompanies.setVisible(false);
        } else {
            productCompanies.setVisible(false);
            productsFamilyCompanies.setVisible(true);
        }
    }

    public void setMachinePartIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent technology = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent technologyIdForMultiUpload = (FieldComponent) view.getComponentByReference("machinePartIdForMultiUpload");
        FieldComponent technologyMultiUploadLocale = (FieldComponent) view.getComponentByReference("machinePartMultiUploadLocale");

        if (technology.getEntityId() != null) {
            technologyIdForMultiUpload.setFieldValue(technology.getEntityId());
            technologyIdForMultiUpload.requestComponentUpdateState();
        } else {
            technologyIdForMultiUpload.setFieldValue("");
            technologyIdForMultiUpload.requestComponentUpdateState();
        }
        technologyMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        technologyMultiUploadLocale.requestComponentUpdateState();

    }
}
