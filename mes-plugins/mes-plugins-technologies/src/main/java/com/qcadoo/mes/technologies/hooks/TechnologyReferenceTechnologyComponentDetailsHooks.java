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
package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyReferenceTechnologyComponentDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_REFERENCE_MODE = "referenceMode";

    public void hideReferenceMode(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            ComponentState referenceModeComponent = viewDefinitionState.getComponentByReference(L_REFERENCE_MODE);
            referenceModeComponent.setFieldValue("01reference");
            referenceModeComponent.setVisible(false);
        }
    }

    public void disabledSaveBackButton(final ViewDefinitionState viewDefinitionState) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonGroup actionsGroup = (RibbonGroup) window.getRibbon().getGroupByName("actions");
        RibbonActionItem saveBack = (RibbonActionItem) actionsGroup.getItemByName("saveBack");

        LookupComponent technology = (LookupComponent) viewDefinitionState.getComponentByReference("technology");
        if (technology.getEntity() == null) {
            saveBack.setEnabled(false);
        } else {
            saveBack.setEnabled(true);
        }
        saveBack.requestUpdate(true);
    }
}
