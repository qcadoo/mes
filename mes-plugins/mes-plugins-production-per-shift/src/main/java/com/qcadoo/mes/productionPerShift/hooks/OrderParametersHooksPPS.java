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
package com.qcadoo.mes.productionPerShift.hooks;

import com.qcadoo.mes.productionPerShift.constants.ParameterFieldsPPS;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderParametersHooksPPS {
    
    public void onBeforeRender(final ViewDefinitionState view) {
        getParametersAndTogglePpsAlgorithm(view);
    }
    
    public void onChangePpsIsAutomatic(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        getParametersAndTogglePpsAlgorithm(view);
    }
    
    private void getParametersAndTogglePpsAlgorithm(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity parameters = form.getPersistedEntityWithIncludedFormValues();
        
        togglePpsAlgorithm(view, parameters);
    }
    
    private void togglePpsAlgorithm(final ViewDefinitionState view, Entity parameters) {
        boolean isPpsAutomatic = parameters.getBooleanField(ParameterFieldsPPS.PPS_IS_AUTOMATIC);
        FieldComponent ppsAlgorithmComponent = (FieldComponent) view.getComponentByReference(ParameterFieldsPPS.PPS_ALGORITHM);
        
        if (!isPpsAutomatic) {
            ppsAlgorithmComponent.setFieldValue(null);
        }
        ppsAlgorithmComponent.setEnabled(isPpsAutomatic);
        ppsAlgorithmComponent.requestComponentUpdateState();
    }
    
}
