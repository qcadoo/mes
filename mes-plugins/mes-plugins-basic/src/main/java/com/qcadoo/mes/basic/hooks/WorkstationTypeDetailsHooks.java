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
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkstationTypeDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void disableSubassemblyCheckbox(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity workstationType = form.getEntity();
        FieldComponent subassemblyCheckbox = (FieldComponent) view.getComponentByReference(WorkstationTypeFields.SUBASSEMBLY);
        if (workstationType.getBooleanField(WorkstationTypeFields.SUBASSEMBLY)) {
            boolean hasSubassemblies = hasSubassemblies(workstationType);
            subassemblyCheckbox.setEnabled(!hasSubassemblies);
        } else {
            boolean hasWorkstations = hasWorkstations(workstationType);
            subassemblyCheckbox.setEnabled(!hasWorkstations);
        }
    }

    private boolean hasSubassemblies(final Entity workstationType) {
        List<Entity> subassemblies = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY).find()
                .add(SearchRestrictions.belongsTo(SubassemblyFields.WORKSTATION_TYPE, workstationType)).list().getEntities();
        return !subassemblies.isEmpty();
    }

    private boolean hasWorkstations(final Entity workstationType) {
        if(workstationType == null || workstationType.getId() == null){
            return false;
        }

        List<Entity> workstations = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION)
                .find().add(SearchRestrictions.belongsTo(WorkstationFields.WORKSTATION_TYPE, workstationType)).list()
                .getEntities();
        return !workstations.isEmpty();
    }
}
