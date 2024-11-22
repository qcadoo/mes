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
package com.qcadoo.mes.basic;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StaffService {

    private static final String DIVISION_ID = "division_id";

    private static final String L_PRODUCTION_LINE_ID = "productionLine_id";

    @Autowired
    private CompanyService companyService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private SecurityService securityService;

    public void onBeforeRender(final ViewDefinitionState view) {
        generateStaffNumber(view);
        setOwnerCompany(view);
        setFilters(view);
        ComponentState skillsTab = view.getComponentByReference("skillsTab");
        skillsTab.setVisible(securityService.hasCurrentUserRole("ROLE_SKILLS"));
    }

    private void setFilters(final ViewDefinitionState view) {
        LookupComponent division = (LookupComponent) view.getComponentByReference(StaffFields.DIVISION);
        LookupComponent workstation = (LookupComponent) view.getComponentByReference(StaffFields.WORKSTATION);
        LookupComponent productionLine = (LookupComponent) view.getComponentByReference(StaffFields.PRODUCTION_LINE);
        Entity divisionEntity = division.getEntity();
        Entity productionLineEntity = productionLine.getEntity();

        FilterValueHolder productionLineFilterValueHolder = productionLine.getFilterValue();
        if (Objects.nonNull(division.getEntity())) {
            productionLineFilterValueHolder.put(DIVISION_ID, divisionEntity.getId());
        } else {
            if (productionLineFilterValueHolder.has(DIVISION_ID)) {
                productionLineFilterValueHolder.remove(DIVISION_ID);
            }
        }
        productionLine.setFilterValue(productionLineFilterValueHolder);

        FilterValueHolder workstationFilterValueHolder = workstation.getFilterValue();
        if (Objects.nonNull(division.getEntity())) {
            workstationFilterValueHolder.put(DIVISION_ID, divisionEntity.getId());
        } else {
            if (workstationFilterValueHolder.has(DIVISION_ID)) {
                workstationFilterValueHolder.remove(DIVISION_ID);
            }
        }

        if (Objects.nonNull(productionLineEntity)) {
            workstationFilterValueHolder.put(L_PRODUCTION_LINE_ID, productionLineEntity.getId());
        } else {
            if (workstationFilterValueHolder.has(L_PRODUCTION_LINE_ID)) {
                workstationFilterValueHolder.remove(L_PRODUCTION_LINE_ID);
            }
        }
        workstation.setFilterValue(workstationFilterValueHolder);

    }

    public void setOwnerCompany(final ViewDefinitionState view) {
        FormComponent staffForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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
                QcadooViewConstants.L_FORM, StaffFields.NUMBER);
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
