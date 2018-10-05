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
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductOutComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.hooks.TechnologyDetailsHooksPFTD;
import com.qcadoo.mes.productFlowThruDivision.hooks.TechnologyHooksPFTD;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OPOCListenersPFTD {

    private static final String L_FORM = "form";

    @Autowired
    private TechnologyDetailsHooksPFTD technologyDetailsHooksPFTD;

    @Autowired
    private TechnologyHooksPFTD technologyHooksPFTD;

    public void onProductsInputLocationChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent isDivisionInputLocationModifiedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OperationProductOutComponentFieldsPFTD.IS_DIVISION_INPUT_LOCATION_MODIFIED);
        if (isDivisionInputLocationModifiedCheckBox.isChecked()) {
            return;
        }
        isDivisionInputLocationModifiedCheckBox.setChecked(true);
        isDivisionInputLocationModifiedCheckBox.requestComponentUpdateState();

        CheckBoxComponent isDivisionLocationChangeOPOC = (CheckBoxComponent) view
                .getComponentByReference("isDivisionLocationChangeOPOC");
        isDivisionLocationChangeOPOC.setChecked(true);
        isDivisionLocationChangeOPOC.requestComponentUpdateState();
    }

    public void onIsDivisionInputLocationChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent opocForm = (FormComponent) view.getComponentByReference(L_FORM);

        LookupComponent productsInputLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        CheckBoxComponent isDivisionInputLocationCheckBox = (CheckBoxComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.IS_DIVISION_INPUT_LOCATION);

        Entity opoc = opocForm.getPersistedEntityWithIncludedFormValues();

        Entity toc = opoc.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);

        Entity division = technologyHooksPFTD.getDivisionForOperation(toc);
        if (division == null) {
            return;
        }
        Entity productsInputLocation = division.getBelongsToField(DivisionFieldsMFR.PRODUCTS_INPUT_LOCATION);

        if ((productsInputLocation != null) && isDivisionInputLocationCheckBox.isChecked()) {
            productsInputLocationLookup.setFieldValue(productsInputLocation.getId());
            productsInputLocationLookup.setEnabled(false);
            productsInputLocationLookup.requestComponentUpdateState();
        } else {
            productsInputLocationLookup.setEnabled(true);
            productsInputLocationLookup.requestComponentUpdateState();
        }

        CheckBoxComponent isDivisionLocationChangeOPOC = (CheckBoxComponent) view
                .getComponentByReference("isDivisionLocationChangeOPOC");
        isDivisionLocationChangeOPOC.setChecked(true);
        isDivisionLocationChangeOPOC.requestComponentUpdateState();
    }

}
