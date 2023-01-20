/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.SubassemblyToWorkstationHelperFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.states.constants.WorkstationStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TranslationService translationService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setWorkstationIdForMultiUploadField(view);
        setSubassembliesHelpers(view);
        setStateButtons(view);
    }

    private void setStateButtons(ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup workstationState = window.getRibbon().getGroupByName("workstationState");
        RibbonActionItem launch = workstationState.getItemByName("launch");
        RibbonActionItem stop = workstationState.getItemByName("stop");
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(WorkstationFields.STATE);
        String state = (String) stateField.getFieldValue();
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long entityId = form.getEntityId();
        if (entityId != null) {
            state = form.getEntity().getDataDefinition().get(entityId).getStringField(WorkstationFields.STATE);
        }
        if (WorkstationStateStringValues.STOPPED.equals(state)) {
            launch.setMessage(null);
            stop.setMessage(translationService.translate("basic.workstationDetails.window.ribbon.workstationState.stop.message", LocaleContextHolder.getLocale()));
        } else {
            launch.setMessage(translationService.translate("basic.workstationDetails.window.ribbon.workstationState.launch.message", LocaleContextHolder.getLocale()));
            stop.setMessage(null);
        }
        launch.setEnabled(WorkstationStateStringValues.STOPPED.equals(state));
        launch.requestUpdate(true);
        stop.setEnabled(WorkstationStateStringValues.LAUNCHED.equals(state));
        stop.requestUpdate(true);
    }

    private void setWorkstationIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent workstationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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

    private void setSubassembliesHelpers(final ViewDefinitionState view) {
        FormComponent workstationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

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
