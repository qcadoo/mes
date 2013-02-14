/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.lineChangeoverNorms.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.lineChangeoverNorms.listeners.MatchingChangeoverNormsDetailsListeners;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MatchingChangeoverNormsDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MatchingChangeoverNormsDetailsListeners listeners;

    public void setFieldsVisible(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        ComponentState matchingNorm = view.getComponentByReference("matchingNorm");
        ComponentState matchingNormNotFound = view.getComponentByReference("matchingNormNotFound");

        if (form.getEntityId() == null) {
            matchingNorm.setVisible(false);
            matchingNormNotFound.setVisible(true);
        } else {
            matchingNorm.setVisible(true);
            matchingNormNotFound.setVisible(false);
        }
    }

    public void fillOrCleanFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntityId() == null) {
            listeners.clearField(view);
            listeners.changeStateEditButton(view, false);
        } else {
            Entity changeover = dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                    LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS).get(form.getEntityId());
            listeners.fillField(view, changeover);
            listeners.changeStateEditButton(view, true);
        }
    }

}
