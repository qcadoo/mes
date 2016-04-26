/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited Project: Qcadoo Framework Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ParameterListenersMFR {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void redirectToMaterialFlowResourcesParameters(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long parameterId = (Long) componentState.getFieldValue();

        if (parameterId != null) {
            DataDefinition parameterDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER);
            DataDefinition documentPositionParametersDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS);

            Entity parameter = parameterDD.get(parameterId);
            Entity documentPositionParameters = parameter.getBelongsToField("documentPositionParameters");

            if (documentPositionParameters == null) {
                List<Entity> entities = documentPositionParametersDD.find().setMaxResults(1).list().getEntities();
                if (!entities.isEmpty()) {
                    documentPositionParameters = entities.get(0);
                }
                if (documentPositionParameters == null) {
                    documentPositionParameters = documentPositionParametersDD.create();
                    documentPositionParameters = documentPositionParametersDD.save(documentPositionParameters);
                }

                parameter.setField("documentPositionParameters", documentPositionParameters);
                parameter = parameterDD.save(parameter);
            }

            String url = "../page/materialFlowResources/materialFlowResourcesParameters.html?context={\"form.id\":\"" + documentPositionParameters.getId() + "\"}";
            view.redirectTo(url, false, true);
        }
    }

    public void setChecked(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        changeVisibility(view, true);
    }

    public void setUnchecked(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        changeVisibility(view, false);
    }

    private void changeVisibility(ViewDefinitionState view, boolean visibility) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");

        for (Entity entity : grid.getEntities()) {
            entity.setField("checked", visibility);
            entity.getDataDefinition().save(entity);
        }
    }

}
