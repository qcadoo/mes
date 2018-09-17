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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductOutComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductsFinalDetailsHooks {

    private static final String L_FORM = "form";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity opoc = form.getPersistedEntityWithIncludedFormValues();

        LookupComponent productsShiftingLocationLookup = (LookupComponent) view
                .getComponentByReference("productsShiftingLocation");
        List<String> references = Arrays.asList(OperationProductOutComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        if (opoc.getBooleanField("automaticMove")) {
            productsShiftingLocationLookup.setEnabled(true);
            references = Arrays
                    .asList(OperationProductOutComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION, "productsShiftingLocation");
        } else {
            productsShiftingLocationLookup.setEnabled(false);
            references = Arrays.asList(OperationProductOutComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION);
        }
        setFieldsRequired(view, references);

    }

    public void onProductionFlowComponentChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity opic = form.getPersistedEntityWithIncludedFormValues();

        LookupComponent productsFlowLocationLookup = (LookupComponent) view
                .getComponentByReference(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        if (ProductionFlowComponent.WAREHOUSE.getStringValue().equals(
                opic.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW))) {
            productsFlowLocationLookup.setEnabled(true);
        } else {
            productsFlowLocationLookup.setEnabled(false);
        }

    }

    private void setFieldsRequired(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setRequired(true);
            field.requestComponentUpdateState();
        }
    }

    public void onAutomaticMoveChange(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

    }

}
