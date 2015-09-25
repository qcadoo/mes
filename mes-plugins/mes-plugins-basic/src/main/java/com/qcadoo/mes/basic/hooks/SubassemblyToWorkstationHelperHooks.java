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
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.SubassemblyToWorkstationHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubassemblyToWorkstationHelperHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(DataDefinition subassemblyToWorkstationHelperDD, Entity subassemblyToWorkstationHelper) {
        DataDefinition subassemblyDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY);

        Entity subassembly = subassemblyToWorkstationHelper.getBelongsToField(SubassemblyToWorkstationHelperFields.SUBASSEMBLY);
        if (subassembly != null) {
            subassembly.setField(SubassemblyFields.WORKSTATION, subassemblyToWorkstationHelper.getField(SubassemblyToWorkstationHelperFields.WORKSTATION));
            subassembly.setField(SubassemblyFields.TYPE, subassemblyToWorkstationHelper.getField(SubassemblyToWorkstationHelperFields.TYPE));
            subassembly = subassemblyDD.save(subassembly);

            if (!subassembly.isValid()) {
                for (Map.Entry<String, ErrorMessage> entry : subassembly.getErrors().entrySet()) {
                    subassemblyToWorkstationHelper.addGlobalError(entry.getValue().getMessage(), entry.getValue().getAutoClose(), entry.getValue().getVars());
                    subassemblyToWorkstationHelper.addError(subassemblyToWorkstationHelperDD.getField(SubassemblyToWorkstationHelperFields.TYPE), entry.getValue().getMessage());
                }

                for (ErrorMessage msg : subassembly.getGlobalErrors()) {
                    subassemblyToWorkstationHelper.addGlobalError(msg.getMessage(), msg.getAutoClose(), msg.getVars());
                    subassemblyToWorkstationHelper.addError(subassemblyToWorkstationHelperDD.getField(SubassemblyToWorkstationHelperFields.TYPE), msg.getMessage());
                }
                subassemblyToWorkstationHelper.setNotValid();
            }
        }
    }

    public void onDelete(DataDefinition subassemblyToWorkstationHelperDD, Entity subassemblyToWorkstationHelper) {
        Entity subassembly = subassemblyToWorkstationHelper.getBelongsToField(SubassemblyToWorkstationHelperFields.SUBASSEMBLY);
        subassembly.setField(SubassemblyFields.WORKSTATION, null);
        subassembly.getDataDefinition().save(subassembly);
    }

    public void beforeRenderView(final ViewDefinitionState view) {
        refreshTypeComponentValue(view);
    }

    public void subassemblyEntityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        refreshTypeComponentValue(view);
    }

    private void refreshTypeComponentValue(ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity subassemblyToWorkstationHelper = formComponent.getEntity();
        Entity subassemly = subassemblyToWorkstationHelper.getBelongsToField(SubassemblyToWorkstationHelperFields.SUBASSEMBLY);
        if (subassemly != null) {
            subassemblyToWorkstationHelper.setField(SubassemblyToWorkstationHelperFields.TYPE, subassemly.getField(SubassemblyFields.TYPE));

            final FieldComponent typeComponent = (FieldComponent) view.getComponentByReference("type");
            typeComponent.setFieldValue(subassemly.getField(SubassemblyFields.TYPE));
            typeComponent.setEnabled(true);
            typeComponent.requestComponentUpdateState();
        }
    }
}
