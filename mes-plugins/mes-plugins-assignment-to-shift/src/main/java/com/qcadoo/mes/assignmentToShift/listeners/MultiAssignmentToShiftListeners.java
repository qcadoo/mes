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
package com.qcadoo.mes.assignmentToShift.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.MultiAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.hooks.MultiAssignmentToShiftDetailsHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MultiAssignmentToShiftListeners {

    @Autowired
    private MultiAssignmentToShiftDetailsHooks multiAssignmentToShiftDetailsHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public static final String L_FORM = "form";

    public void enabledFieldWhenTypeIsSpecific(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        multiAssignmentToShiftDetailsHooks.setFieldsEnabledWhenTypeIsSpecific(view);
    }

    public void addManyWorkers(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        DataDefinition dataDefinition = dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_STAFF_ASSIGNMENT_TO_SHIFT);
        FormComponent assignmentToShiftForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity multiAssignmentToShift = assignmentToShiftForm.getPersistedEntityWithIncludedFormValues();

        Entity productionLine = multiAssignmentToShift.getBelongsToField(MultiAssignmentToShiftFields.PRODUCTION_LINE);
        String occupationType = multiAssignmentToShift.getStringField(MultiAssignmentToShiftFields.OCCUPATION_TYPE);
        String occupationTypeName = multiAssignmentToShift.getStringField(MultiAssignmentToShiftFields.OCCUPATION_TYPE_NAME);
        Entity masterOrder = multiAssignmentToShift.getBelongsToField(MultiAssignmentToShiftFields.MASTER_ORDER);
        Entity assignmentToShift = multiAssignmentToShift.getBelongsToField(MultiAssignmentToShiftFields.ASSIGNMENT_TO_SHIFT);
        List<Entity> workers = multiAssignmentToShift.getManyToManyField(MultiAssignmentToShiftFields.WORKERS);
        for (Entity worker : workers) {
            Entity staffAssignment = dataDefinition.create();
            staffAssignment.setField(StaffAssignmentToShiftFields.WORKER, worker);
            staffAssignment.setField(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine);
            staffAssignment.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE, occupationType);
            staffAssignment.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME, occupationTypeName);
            staffAssignment.setField(StaffAssignmentToShiftFields.MASTER_ORDER, masterOrder);
            staffAssignment.setField(StaffAssignmentToShiftFields.ASSIGNMENT_TO_SHIFT, assignmentToShift);
            staffAssignment.setField(StaffAssignmentToShiftFields.STATE, StaffAssignmentToShiftState.SIMPLE.getStringValue());
            dataDefinition.save(staffAssignment);
        }
        deleteUsedAndOldAssignments(multiAssignmentToShift);
    }

    private void deleteUsedAndOldAssignments(Entity multiAssignmentToShift) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", multiAssignmentToShift.getId());

        String query = "DELETE FROM jointable_multiassignmenttoshift_staff "
                + "WHERE multiassignmenttoshift_id IN (SELECT id FROM assignmenttoshift_multiassignmenttoshift WHERE id = :id OR createdate < current_timestamp - interval '1 day'); "
                + "DELETE FROM assignmenttoshift_multiassignmenttoshift WHERE id = :id OR createdate < current_timestamp - interval '1 day';";
        jdbcTemplate.update(query, params);

    }
}
