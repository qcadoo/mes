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

import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.WORK_ON_LINE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.hooks.MultiAssignmentToShiftDetailsHooks;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class AssignmentToShiftDetailsListeners {

    public static final String L_WINDOW = "window";

    public static final String L_FORM = "form";

    public static final String L_COPY = "copy";

    public static final String L_COPY_STAFF_ASSIGNMENT_TO_SHIFT = "copyStaffAssignmentToShift";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MultiAssignmentToShiftDetailsHooks multiAssignmentToShiftDetailsHooks;

    public void addManyWorkers(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent assignmentToShiftForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity multiAssignmentToShift = prepareMultiAssignmetEntity(assignmentToShiftForm.getEntity());
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", multiAssignmentToShift.getId());
        parameters.put("window.activeMenu", "orders.assignmentToShift");

        view.redirectTo("/page/assignmentToShift/multiAssignmentToShiftDetails.html", false, true, parameters);
    }

    private Entity prepareMultiAssignmetEntity(Entity assignmentToShift) {
        Entity multiAssignmentToShift = dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_MULTI_ASSIGNMENT_TO_SHIFT).create();
        multiAssignmentToShift.setField("assignmentToShift", assignmentToShift);
        Entity dictionaryItem = multiAssignmentToShiftDetailsHooks.findDictionaryItemByTechnicalCode(WORK_ON_LINE
                .getStringValue());

        if (dictionaryItem != null) {
            String occupationTypeName = dictionaryItem.getStringField(NAME);
            multiAssignmentToShift.setField("occupationType", occupationTypeName);
        }
        multiAssignmentToShift = multiAssignmentToShift.getDataDefinition().save(multiAssignmentToShift);
        return multiAssignmentToShift;
    }

    public void copyStaffAssignmentToShift(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent assignmentToShiftForm = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent staffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS);

        Long assignmentToShiftId = assignmentToShiftForm.getEntityId();

        if (assignmentToShiftId != null) {
            Entity assignmentToShift = getAssignmentToShiftFromDB(assignmentToShiftId);

            List<Entity> staffAssignmentToShifts = Lists.newArrayList(assignmentToShift
                    .getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS));

            Set<Long> selectedStaffAssignmentToShiftIds = staffAssignmentToShiftsGrid.getSelectedEntitiesIds();

            for (Long selectedStaffAssignmentToShiftId : selectedStaffAssignmentToShiftIds) {
                Entity staffAssignmentToShift = getStaffAssignmentToShiftFromDB(selectedStaffAssignmentToShiftId);

                staffAssignmentToShifts.add(copyStaffAssignmentToShift(staffAssignmentToShift));
            }

            assignmentToShift.setField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS, staffAssignmentToShifts);

            assignmentToShiftForm.setEntity(assignmentToShift);
        }
    }

    private Entity copyStaffAssignmentToShift(final Entity staffAssignmentToShift) {
        Entity newStaffAssignmentToShift = getStaffAssignmentToShiftDD().create();

        Entity assignmentToShift = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.ASSIGNMENT_TO_SHIFT);
        Entity worker = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.WORKER);
        Entity productionLine = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE);
        String occupationType = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE);
        String occupationTypeName = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME);
        String occupationTypeEnum = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_ENUM);
        String occupationTypeValueForGrid = staffAssignmentToShift
                .getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID);
        Entity masterOrder = staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.MASTER_ORDER);
        String state = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.STATE);
        String description = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.DESCRIPTION);

        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.ASSIGNMENT_TO_SHIFT, assignmentToShift);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.WORKER, worker);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE, occupationType);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME, occupationTypeName);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_ENUM, occupationTypeEnum);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID,
                occupationTypeValueForGrid);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.MASTER_ORDER, masterOrder);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.STATE, state);
        newStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.DESCRIPTION, description);

        newStaffAssignmentToShift = newStaffAssignmentToShift.getDataDefinition().save(newStaffAssignmentToShift);

        return newStaffAssignmentToShift;
    }

    public void changeCopyStaffAssignmentToShiftButtonState(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent assignmentToShiftForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity assignmentToShift = assignmentToShiftForm.getEntity();

        changeCopyStaffAssignmentToShiftButtonState(view, assignmentToShift);
    }

    private void changeCopyStaffAssignmentToShiftButtonState(ViewDefinitionState view, Entity assignmentToShift) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup copyRibbonGroup = ribbon.getGroupByName(L_COPY);

        RibbonActionItem copyStaffAssignmentToShiftRibbonActionItem = copyRibbonGroup
                .getItemByName(L_COPY_STAFF_ASSIGNMENT_TO_SHIFT);

        GridComponent staffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS);

        String state = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);

        AssignmentToShiftState assignmentToShiftState = AssignmentToShiftState.parseString(state);

        Long assignmentToShiftId = assignmentToShift.getId();

        boolean isSaved = (assignmentToShiftId != null);

        if (isSaved) {
            assignmentToShift = getAssignmentToShiftFromDB(assignmentToShiftId);
        }

        boolean isExternalSynchronized = assignmentToShift.getBooleanField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED);

        boolean areSelected = !staffAssignmentToShiftsGrid.getSelectedEntities().isEmpty();

        boolean isEnabled = isSaved ? isExternalSynchronized && assignmentToShiftState.isEditingAllowed() && areSelected : false;

        copyStaffAssignmentToShiftRibbonActionItem.setEnabled(isEnabled);
        copyStaffAssignmentToShiftRibbonActionItem.requestUpdate(true);
    }

    private Entity getAssignmentToShiftFromDB(final Long assignmentToShiftId) {
        return getAssignmentToShiftDD().get(assignmentToShiftId);
    }

    private DataDefinition getAssignmentToShiftDD() {
        return dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT);
    }

    private Entity getStaffAssignmentToShiftFromDB(final Long staffAssignmentToShiftId) {
        return getStaffAssignmentToShiftDD().get(staffAssignmentToShiftId);
    }

    private DataDefinition getStaffAssignmentToShiftDD() {
        return dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_STAFF_ASSIGNMENT_TO_SHIFT);
    }
}
