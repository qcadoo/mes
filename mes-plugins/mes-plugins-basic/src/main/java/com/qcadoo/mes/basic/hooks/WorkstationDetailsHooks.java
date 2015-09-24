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
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkstationDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void setWorkstationIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent workstationForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent workstationIdForMultiUpload = (FieldComponent) view.getComponentByReference("workstationIdForMultiUpload");
        FieldComponent workstationMultiUploadLocale = (FieldComponent) view
                .getComponentByReference("workstationMultiUploadLocale");

        if (workstationForm.getEntityId() != null) {
            workstationIdForMultiUpload.setFieldValue(workstationForm.getEntityId());
            workstationIdForMultiUpload.requestComponentUpdateState();
        } else {
            workstationIdForMultiUpload.setFieldValue("");
            workstationIdForMultiUpload.requestComponentUpdateState();
        }
        workstationMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        workstationMultiUploadLocale.requestComponentUpdateState();
    }

    public void setSubassembliesHelpers(final ViewDefinitionState view) {
        FormComponent workstationForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (workstationForm.getEntityId() != null) {
            Entity workstation = workstationForm.getPersistedEntityWithIncludedFormValues();

            DataDefinition subassemblyToWorkstationHelperDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY_TO_WORKSTATION_HELPER);

            List<Long> idList = workstation.getHasManyField(WorkstationFields.SUBASSEMBLIES_HELPERS).stream().map(Entity::getId).collect(Collectors.toList());
            deleteSubassemblyToWorkstationHelpers(idList);

            List<Entity> helpers = new ArrayList<>();
            for (Entity subassembly : workstation.getHasManyField(WorkstationFields.SUBASSEMBLIES)) {
                Entity subassemblyToWorkstationHelper = subassemblyToWorkstationHelperDD.create();
                subassemblyToWorkstationHelper.setField(SubassemblyToWorkstationHelperFields.WORKSTATION, subassembly.getField(SubassemblyFields.WORKSTATION));
                subassemblyToWorkstationHelper.setField(SubassemblyToWorkstationHelperFields.TYPE, subassembly.getField(SubassemblyFields.TYPE));
                subassemblyToWorkstationHelper.setField(SubassemblyToWorkstationHelperFields.SUBASSEMBLY, subassembly);
                subassemblyToWorkstationHelper = subassemblyToWorkstationHelperDD.save(subassemblyToWorkstationHelper);
                helpers.add(subassemblyToWorkstationHelper);
            }

            workstation.setField(WorkstationFields.SUBASSEMBLIES_HELPERS, helpers);
        }
    }

    private void deleteSubassemblyToWorkstationHelpers(List<Long> idList) {
        if (!idList.isEmpty()) {
            String ids = idList.stream().map(Object::toString).collect(Collectors.joining(","));
            jdbcTemplate.update("DELETE FROM basic_subassemblytoworkstationhelper WHERE id IN (" + ids + ")", new HashMap<String, Object>());
        }
    }

}
