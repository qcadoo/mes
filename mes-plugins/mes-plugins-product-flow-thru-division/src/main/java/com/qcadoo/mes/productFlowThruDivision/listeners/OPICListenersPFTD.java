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
package com.qcadoo.mes.productFlowThruDivision.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DivisionFieldsMFR;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.hooks.TechnologyDetailsHooksPFTD;
import com.qcadoo.mes.productFlowThruDivision.hooks.TechnologyHooksPFTD;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OPICListenersPFTD {

    private static final String L_FORM = "form";

    @Autowired
    private TechnologyDetailsHooksPFTD technologyDetailsHooksPFTD;

    @Autowired
    private TechnologyHooksPFTD technologyHooksPFTD;

    public void onComponentsLocationChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent isDivisionLocationModifiedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFieldsPFTD.IS_DIVISION_LOCATION_MODIFIED);
        if (isDivisionLocationModifiedCheckBox.isChecked()) {
            return;
        }
        isDivisionLocationModifiedCheckBox.setChecked(true);
        isDivisionLocationModifiedCheckBox.requestComponentUpdateState();

        CheckBoxComponent isDivisionLocationChangeOPIC = (CheckBoxComponent) view
                .getComponentByReference("isDivisionLocationChangeOPIC");
        isDivisionLocationChangeOPIC.setChecked(true);
        isDivisionLocationChangeOPIC.requestComponentUpdateState();
    }

    public void onComponentsOutputLocationChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent isDivisionOutputLocationModifiedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFieldsPFTD.IS_DIVISION_OUTPUT_LOCATION_MODIFIED);
        if (isDivisionOutputLocationModifiedCheckBox.isChecked()) {
            return;
        }
        isDivisionOutputLocationModifiedCheckBox.setChecked(true);
        isDivisionOutputLocationModifiedCheckBox.requestComponentUpdateState();

        CheckBoxComponent isDivisionLocationChangeOPIC = (CheckBoxComponent) view
                .getComponentByReference("isDivisionLocationChangeOPIC");
        isDivisionLocationChangeOPIC.setChecked(true);
        isDivisionLocationChangeOPIC.requestComponentUpdateState();
    }

    public void onIsDivisionLocationChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent opicForm = (FormComponent) view.getComponentByReference(L_FORM);

        LookupComponent componentsLocationLookup = (LookupComponent) view
                .getComponentByReference(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION);
        CheckBoxComponent isDivisionLocationCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductInComponentFieldsPFTD.IS_DIVISION_LOCATION);

        Entity opic = opicForm.getPersistedEntityWithIncludedFormValues();
        Entity toc = opic.getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);

        Entity division = technologyHooksPFTD.getDivisionForOperation(toc);
        if (division == null) {
            return;
        }

        Entity componentsLocation = division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_LOCATION);

        if ((componentsLocation != null) && isDivisionLocationCheckBox.isChecked()) {
            componentsLocationLookup.setFieldValue(componentsLocation.getId());
            componentsLocationLookup.setEnabled(false);
            componentsLocationLookup.requestComponentUpdateState();
        } else {
            // componentsLocationLookup.setFieldValue(componentsLocation.getId());
            componentsLocationLookup.setEnabled(true);
            componentsLocationLookup.requestComponentUpdateState();
        }

        CheckBoxComponent isDivisionLocationChangeOPIC = (CheckBoxComponent) view
                .getComponentByReference("isDivisionLocationChangeOPIC");
        isDivisionLocationChangeOPIC.setChecked(true);
        isDivisionLocationChangeOPIC.requestComponentUpdateState();
    }

    public void onIsDivisionOutputLocationChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent opicForm = (FormComponent) view.getComponentByReference(L_FORM);

        LookupComponent componentsOutputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        CheckBoxComponent isDivisionOutputLocationCheckBox = (CheckBoxComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.IS_DIVISION_OUTPUT_LOCATION);

        Entity opic = opicForm.getPersistedEntityWithIncludedFormValues();
        Entity toc = opic.getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);

        Entity division = technologyHooksPFTD.getDivisionForOperation(toc);
        if (division == null) {
            return;
        }

        Entity divisionComponentsOutputLocation = division.getBelongsToField(DivisionFieldsMFR.COMPONENTS_OUTPUT_LOCATION);

        if ((divisionComponentsOutputLocation != null) && isDivisionOutputLocationCheckBox.isChecked()) {
            componentsOutputLocationLookup.setFieldValue(divisionComponentsOutputLocation.getId());
            componentsOutputLocationLookup.setEnabled(false);
            componentsOutputLocationLookup.requestComponentUpdateState();
        } else {
            componentsOutputLocationLookup.setEnabled(true);
            componentsOutputLocationLookup.requestComponentUpdateState();
        }

        CheckBoxComponent isDivisionLocationChangeOPIC = (CheckBoxComponent) view
                .getComponentByReference("isDivisionLocationChangeOPIC");
        isDivisionLocationChangeOPIC.setChecked(true);
        isDivisionLocationChangeOPIC.requestComponentUpdateState();
    }
}
