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
package com.qcadoo.mes.timeNormsForOperations.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyListenersTN {

    private static final String L_FORM = "form";

    @Autowired
    private NormService normService;

    public void checkOperationOutputQuantities(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (technologyForm.getEntityId() == null) {
            return;
        }

        Entity technology = technologyForm.getEntity();

        if (!TechnologyState.DRAFT.getStringValue().equals(technology.getStringField(TechnologyFields.STATE))) {
            return;
        }

        technology = technology.getDataDefinition().get(technology.getId());

        List<String> messages = normService.checkOperationOutputQuantities(technology);

        if (!messages.isEmpty()) {
            StringBuilder builder = new StringBuilder();

            for (String message : messages) {
                builder.append(message.toString());
                builder.append(", ");
            }

            technologyForm.addMessage("technologies.technology.validate.error.invalidQuantity", MessageType.INFO, false,
                    builder.toString());
        }
    }

}
