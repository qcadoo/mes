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
package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class GeneralParametersHooksMFR {

    public void onBeforeRender(final ViewDefinitionState view) {
        setCostsSourceAndWarehousesEnabled(view);
    }

    public void setCostsSourceAndWarehousesEnabled(final ViewDefinitionState view) {
        FormComponent parameterForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent costsSourceField = (FieldComponent) view.getComponentByReference(ParameterFieldsMFR.COSTS_SOURCE);
        AwesomeDynamicListComponent warehousesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(ParameterFieldsMFR.WAREHOUSES);

        Entity parameter = parameterForm.getPersistedEntityWithIncludedFormValues();

        String costsSource = parameter.getStringField(ParameterFieldsMFR.COSTS_SOURCE);

        boolean automaticUpdateCostNorms = parameter.getBooleanField(ParameterFieldsMFR.AUTOMATIC_UPDATE_COST_NORMS);
        boolean costsSourceIsMes = "01mes".equals(costsSource);

        if (automaticUpdateCostNorms) {
            costsSourceField.setEnabled(true);
            warehousesADL.setEnabled(costsSourceIsMes);
            warehousesADL.getFormComponents().forEach(formComponent -> formComponent.setFormEnabled(costsSourceIsMes));
        } else {
            costsSourceField.setFieldValue(null);
            costsSourceField.setEnabled(false);
            warehousesADL.setFieldValue(null);
            warehousesADL.setEnabled(false);
        }
        costsSourceField.requestComponentUpdateState();
        warehousesADL.requestComponentUpdateState();
    }

}
