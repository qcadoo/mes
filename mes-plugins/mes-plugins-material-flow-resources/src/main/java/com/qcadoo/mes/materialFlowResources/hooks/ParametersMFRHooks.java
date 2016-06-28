/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited Project: Qcadoo MES Version: 1.4
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
package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ParametersMFRHooks {

    @Autowired
    private TranslationService translationService;

    private static final String TRANSLATION_PREFIX = "materialFlowResources.materialFlowResourcesParameters.documentPositionParameters.";

    public void onBeforeRender(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");

        for (Entity entity : grid.getEntities()) {
            String name = entity.getStringField("name");
            String displayName = translationService.translate(TRANSLATION_PREFIX + name, view.getLocale());
            entity.setField("name", displayName);
        }
    }

    public void onBeforeRenderItemDetails(final ViewDefinitionState view) {
        FormComponent documentPositionParametersItemForm = (FormComponent) view.getComponentByReference("form");
        FieldComponent displayNameField = (FieldComponent) view.getComponentByReference("displayName");
        Entity item = documentPositionParametersItemForm.getPersistedEntityWithIncludedFormValues();
        String name = item.getStringField("name");
        String displayName = translationService.translate(TRANSLATION_PREFIX + name, displayNameField.getLocale());
        displayNameField.setFieldValue(displayName);

        boolean editable = item.getBooleanField("editable");
        ((FieldComponent) view.getComponentByReference("checked")).setEnabled(editable);
        ((FieldComponent) view.getComponentByReference("editable")).setEnabled(false);
    }

}
