/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class StaffService {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    private static final String L_FORM = "form";

    public void setOwnerCompany(final ViewDefinitionState view) {
        FormComponent staffForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent workForLookup = (FieldComponent) view.getComponentByReference(StaffFields.WORK_FOR);

        Entity ownerCompany = companyService.getCompany();

        if ((staffForm.getEntityId() != null) || (workForLookup.getFieldValue() != null) || (ownerCompany == null)) {
            return;
        }

        workForLookup.setFieldValue(ownerCompany.getId());
        workForLookup.requestComponentUpdateState();
    }

    public void generateStaffNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF,
                L_FORM, StaffFields.NUMBER);
    }

    /**
     * @deprecated use com.qcadoo.mes.basic.util.StaffNameExtractor.extractNameAndSurname
     */
    @Deprecated
    public String extractFullName(final Entity staff) {
        if (staff == null) {
            return null;
        }
        String name = staff.getStringField(StaffFields.NAME);
        String surname = staff.getStringField(StaffFields.SURNAME);
        return String.format("%s %s", name, surname);
    }

}
