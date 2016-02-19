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
package com.qcadoo.mes.assignmentToShift.hooks;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class AssignmentToShiftDetailsHooks {

    public static final String L_WINDOW = "window";

    public static final String L_FORM = "form";

    public static final String L_ACTIONS = "actions";

    public static final String L_STATUS = "status";

    public static final String L_COPY = "copy";

    public static final String L_SAVE = "save";

    public static final String L_SAVE_BACK = "saveBack";

    public static final String L_SAVE_NEW = "saveNew";

    public static final String L_DELETE = "delete";

    public static final String L_ACCEPT_ASSIGNMENT_TO_SHIFT = "acceptAssignmentToShift";

    public static final String L_CORRECT_ASSIGNMENT_TO_SHIFT = "correctAssignmentToShift";

    public static final String L_ACCEPT_CORRECTED_ASSIGNMENT_TO_SHIFT = "acceptCorrectedAssignmentToShift";

    public static final String L_COPY_STAFF_ASSIGNMENT_TO_SHIFT = "copyStaffAssignmentToShift";

    public static final String L_ASSIGNMENT_TO_SHIFT_INFO_IS_WAITING_FOR_SYNC = "assignmentToShift.assignmentToShift.info.isWaitingForSync";

    public static final String L_ASSIGNMENT_TO_SHIFT_STATE_DOES_NOT_ALLOW_EDITING = "assignmentToShift.assignmentToShiftDetails.window.ribbon.stateDoesNotAllowEditing";

    private static final Logger LOG = LoggerFactory.getLogger(AssignmentToShiftDetailsHooks.class);

    private static final String L_ADD_MANY_WORKERS = "addManyWorkers";

    private static final String L_ADD = "add";

    private static CustomRestriction customRestrictionAccepted = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.eq(AssignmentToShiftFields.STATE,
                    StaffAssignmentToShiftState.ACCEPTED.getStringValue()));
        }

    };

    private static CustomRestriction customRestrictionSimple = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.eq(AssignmentToShiftFields.STATE,
                    StaffAssignmentToShiftState.SIMPLE.getStringValue()));
        }
    };

    private static CustomRestriction customRestrictionCorrected = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.eq(AssignmentToShiftFields.STATE,
                    StaffAssignmentToShiftState.CORRECTED.getStringValue()));
        }

    };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        addDiscriminatorRestrictionToStaffAssignmentGrid(view);
        addDiscriminatorRestrictionToCorrectedStaffAssignmentGrid(view);
        addDiscriminatorRestrictionToAcceptedStaffAssignmentGrid(view);

        FormComponent assignmentToShiftForm = (FormComponent) view.getComponentByReference(L_FORM);

        disableButtonsWhenNotExternalSynchronized(view, assignmentToShiftForm.getEntity());

        Long assignmentToShiftFormId = assignmentToShiftForm.getEntityId();

        if (assignmentToShiftFormId == null) {
            return;
        }

        Entity assignmentToShift = getAssignmentToShift(assignmentToShiftFormId);

        disableFormWhenStateIsAcceptedOrCorrected(view, assignmentToShift);

        showExternalSyncNotification(assignmentToShiftForm, assignmentToShift);
        showLastStateChangeFailNotification(assignmentToShiftForm, assignmentToShift);
    }

    private Entity getAssignmentToShift(final Long assignmentToShiftFormId) {
        return dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT).get(assignmentToShiftFormId);
    }

    private final void addDiscriminatorRestrictionToStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent staffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS);

        staffAssignmentToShiftsGrid.setCustomRestriction(customRestrictionSimple);
    }

    private final void addDiscriminatorRestrictionToCorrectedStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent correctedStaffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.CORRECTED_STAFF_ASSIGNMENT_TO_SHIFTS);

        correctedStaffAssignmentToShiftsGrid.setCustomRestriction(customRestrictionCorrected);
    }

    private final void addDiscriminatorRestrictionToAcceptedStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent plannedStaffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.PLANNED_STAFF_ASSIGNMENT_TO_SHIFTS);

        plannedStaffAssignmentToShiftsGrid.setCustomRestriction(customRestrictionAccepted);
    }

    private void disableButtonsWhenNotExternalSynchronized(final ViewDefinitionState view, Entity assignmentToShift) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonGroup statusRibbonGroup = ribbon.getGroupByName(L_STATUS);
        RibbonGroup copyRibbonGroup = ribbon.getGroupByName(L_COPY);
        RibbonGroup addRibbonGroup = ribbon.getGroupByName(L_ADD);

        RibbonActionItem saveRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE);
        RibbonActionItem saveBackRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE_BACK);
        RibbonActionItem saveNewRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE_NEW);
        RibbonActionItem deleteRibbonActionItem = actionsRibbonGroup.getItemByName(L_DELETE);

        RibbonActionItem acceptAssignmentToShiftRibbonActionItem = statusRibbonGroup.getItemByName(L_ACCEPT_ASSIGNMENT_TO_SHIFT);
        RibbonActionItem correctAssignmentToShiftRibbonActionItem = statusRibbonGroup
                .getItemByName(L_CORRECT_ASSIGNMENT_TO_SHIFT);
        RibbonActionItem acceptCorrectedAssignmentToShiftRibbonActionItem = statusRibbonGroup
                .getItemByName(L_ACCEPT_CORRECTED_ASSIGNMENT_TO_SHIFT);

        RibbonActionItem copyStaffAssignmentToShiftRibbonActionItem = copyRibbonGroup
                .getItemByName(L_COPY_STAFF_ASSIGNMENT_TO_SHIFT);

        RibbonActionItem addManyWorkersRibbonActionItem = addRibbonGroup.getItemByName(L_ADD_MANY_WORKERS);

        GridComponent staffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS);

        String state = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);

        AssignmentToShiftState assignmentToShiftState = AssignmentToShiftState.parseString(state);

        Long assignmentToShiftId = assignmentToShift.getId();

        boolean isSaved = (assignmentToShiftId != null);

        if (isSaved) {
            assignmentToShift = getAssignmentToShift(assignmentToShiftId);
        }

        boolean isExternalSynchronized = assignmentToShift.getBooleanField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED);

        boolean areSelected = !staffAssignmentToShiftsGrid.getSelectedEntities().isEmpty();

        boolean isEnabled = isSaved ? isExternalSynchronized && assignmentToShiftState.isEditingAllowed() : true;

        String message = null;

        if (!isEnabled) {
            if (isExternalSynchronized) {
                message = L_ASSIGNMENT_TO_SHIFT_STATE_DOES_NOT_ALLOW_EDITING;
            } else {
                message = L_ASSIGNMENT_TO_SHIFT_INFO_IS_WAITING_FOR_SYNC;
            }
        }

        List<RibbonActionItem> ribbonActionItems = Lists.newArrayList(saveRibbonActionItem, saveBackRibbonActionItem,
                saveNewRibbonActionItem, deleteRibbonActionItem, acceptAssignmentToShiftRibbonActionItem,
                correctAssignmentToShiftRibbonActionItem, acceptCorrectedAssignmentToShiftRibbonActionItem,
                copyStaffAssignmentToShiftRibbonActionItem, addManyWorkersRibbonActionItem);

        for (RibbonActionItem ribbonActionItem : ribbonActionItems) {
            if (ribbonActionItem != null) {
                String ribbonActionItemName = ribbonActionItem.getName();

                if (AssignmentToShiftStateStringValues.DRAFT.equals(state)
                        && (L_CORRECT_ASSIGNMENT_TO_SHIFT.equals(ribbonActionItemName) || L_ACCEPT_CORRECTED_ASSIGNMENT_TO_SHIFT
                                .equals(ribbonActionItemName))) {
                    ribbonActionItem.setEnabled(false);
                    ribbonActionItem.setMessage(message);
                } else if ((AssignmentToShiftStateStringValues.ACCEPTED.equals(state) || AssignmentToShiftStateStringValues.CORRECTED
                        .equals(state)) && L_CORRECT_ASSIGNMENT_TO_SHIFT.equals(ribbonActionItemName)) {
                    ribbonActionItem.setEnabled(true);
                } else if (AssignmentToShiftStateStringValues.DURING_CORRECTION.equals(state)
                        && (L_DELETE.equals(ribbonActionItemName) || L_ACCEPT_ASSIGNMENT_TO_SHIFT.equals(ribbonActionItemName) || L_CORRECT_ASSIGNMENT_TO_SHIFT
                                .equals(ribbonActionItemName))) {
                    ribbonActionItem.setEnabled(false);
                    ribbonActionItem.setMessage(message);
                } else if (L_COPY_STAFF_ASSIGNMENT_TO_SHIFT.equals(ribbonActionItemName) && !areSelected) {
                    ribbonActionItem.setEnabled(false);
                } else if (L_ADD_MANY_WORKERS.equals(ribbonActionItemName) && !isSaved) {
                    ribbonActionItem.setEnabled(false);
                } else {
                    ribbonActionItem.setEnabled(isEnabled);
                    ribbonActionItem.setMessage(message);
                }

                ribbonActionItem.requestUpdate(true);
            }
        }
    }

    private void disableFormWhenStateIsAcceptedOrCorrected(final ViewDefinitionState view, final Entity assignmentToShift) {
        FormComponent assignmentToShiftForm = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent staffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS);

        boolean isExternalSynchronized = assignmentToShift.getBooleanField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED);

        String state = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);

        if (!isExternalSynchronized || AssignmentToShiftState.ACCEPTED.getStringValue().equals(state)
                || AssignmentToShiftState.CORRECTED.getStringValue().equals(state)) {
            assignmentToShiftForm.setFormEnabled(false);
            staffAssignmentToShiftsGrid.setEditable(false);
        } else {
            assignmentToShiftForm.setFormEnabled(true);
            staffAssignmentToShiftsGrid.setEditable(true);
        }
    }

    private void showExternalSyncNotification(final FormComponent assignmentToShiftForm, final Entity assignmentToShift) {
        boolean isExternalSynchronized = assignmentToShift.getBooleanField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED);

        if (!isExternalSynchronized) {
            assignmentToShiftForm.addMessage("assignmentToShift.assignmentToShift.info.isWaitingForSync",
                    ComponentState.MessageType.INFO, true);
        }
    }

    private void showLastStateChangeFailNotification(final FormComponent assignmentToShiftForm, final Entity assignmentToShift) {
        boolean showLastStateChangeResults = assignmentToShift
                .getBooleanField(AssignmentToShiftFields.SHOW_LAST_STATE_CHANGE_RESULT);

        if (!showLastStateChangeResults) {
            return;
        }

        boolean lastStateChangeFails = assignmentToShift.getBooleanField(AssignmentToShiftFields.LAST_STATE_CHANGE_FAILS);

        if (lastStateChangeFails) {
            showStateChangeFailureNotification(assignmentToShiftForm, assignmentToShift);
        } else {
            showStateChangeSuccessNotification(assignmentToShiftForm);
        }

        removeShowLastResultsFlag(assignmentToShift);
    }

    private void showStateChangeSuccessNotification(final FormComponent assignmentToShiftForm) {
        assignmentToShiftForm.addMessage("assignmentToShift.assignmentToShift.info.lastStateChangeSucceed",
                ComponentState.MessageType.SUCCESS, true);
    }

    private void showStateChangeFailureNotification(final FormComponent assignmentToShiftForm, final Entity assignmentToShift) {
        String lastStateChangeFailCause = assignmentToShift.getStringField(AssignmentToShiftFields.LAST_STATE_CHANGE_FAIL_CAUSE);

        if (StringUtils.isEmpty(lastStateChangeFailCause)) {
            assignmentToShiftForm.addMessage("assignmentToShift.assignmentToShift.info.lastStateChangeFails",
                    ComponentState.MessageType.FAILURE, false);
        } else {
            assignmentToShiftForm.addMessage("assignmentToShift.assignmentToShift.info.lastStateChangeFails.withCause",
                    ComponentState.MessageType.FAILURE, false, lastStateChangeFailCause);
        }
    }

    private void removeShowLastResultsFlag(final Entity assignmentToShift) {
        assignmentToShift.setField(AssignmentToShiftFields.SHOW_LAST_STATE_CHANGE_RESULT, false);

        Entity savedAssignmentToShift = assignmentToShift.getDataDefinition().save(assignmentToShift);

        if (!savedAssignmentToShift.isValid() && LOG.isWarnEnabled()) {
            LOG.warn("Can't remove 'showLastStateChangeResults' flag because of validation errors in entity: "
                    + savedAssignmentToShift);
        }
    }

}