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

    private static final Logger LOG = LoggerFactory.getLogger(AssignmentToShiftDetailsHooks.class);

    public static final String L_FORM = "form";

    public static final String L_WINDOW = "window";

    public static final String L_ACTIONS = "actions";

    public static final String L_STATUS = "status";

    public static final String L_SAVE = "save";

    public static final String L_SAVE_BACK = "saveBack";

    public static final String L_SAVE_NEW = "saveNew";

    public static final String L_DELETE = "delete";

    public static final String L_STATE = "state";

    public static final String L_ASSIGNMENT_TO_SHIFT_INFO_IS_WAITING_FOR_SYNC = "assignmentToShift.assignmentToShift.info.isWaitingForSync";

    public static final String L_ASSIGNMENT_TO_SHIFT_STATE_DOES_NOT_ALLOW_EDITING = "assignmentToShift.assignmentToShiftDetails.window.ribbon.stateDoesNotAllowEditing";

    @Autowired
    private DataDefinitionService dataDefinitionService;

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

    public final void addDiscriminatorRestrictionToStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent staffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS);

        staffAssignmentToShiftsGrid.setCustomRestriction(customRestrictionSimple);
    }

    public final void addDiscriminatorRestrictionToCorrectedStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent correctedStaffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.CORRECTED_STAFF_ASSIGNMENT_TO_SHIFTS);

        correctedStaffAssignmentToShiftsGrid.setCustomRestriction(customRestrictionCorrected);
    }

    public final void addDiscriminatorRestrictionToAcceptedStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent plannedStaffAssignmentToShiftsGrid = (GridComponent) view
                .getComponentByReference(AssignmentToShiftFields.PLANNED_STAFF_ASSIGNMENT_TO_SHIFTS);

        plannedStaffAssignmentToShiftsGrid.setCustomRestriction(customRestrictionAccepted);
    }

    public void disableButtonsWhenNotExternalSynchronized(final ViewDefinitionState view, Entity assignmentToShift) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonGroup statusRibbonGroup = ribbon.getGroupByName(L_STATUS);

        RibbonActionItem saveRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE);
        RibbonActionItem saveBackRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE_BACK);
        RibbonActionItem saveNewRibbonActionItem = actionsRibbonGroup.getItemByName(L_SAVE_NEW);
        RibbonActionItem deleteRibbonActionItem = actionsRibbonGroup.getItemByName(L_DELETE);
        RibbonActionItem stateRibbonActionItem = statusRibbonGroup.getItemByName(L_STATE);

        String state = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);

        AssignmentToShiftState assignmentToShiftState = AssignmentToShiftState.parseString(state);

        Long assignmentToShiftId = assignmentToShift.getId();

        boolean isSaved = (assignmentToShiftId != null);

        if (isSaved) {
            assignmentToShift = getAssignmentToShift(assignmentToShiftId);
        }

        boolean isExternalSynchronized = assignmentToShift.getBooleanField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED);

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
                saveNewRibbonActionItem, deleteRibbonActionItem, stateRibbonActionItem);

        for (RibbonActionItem ribbonActionItem : ribbonActionItems) {
            if (ribbonActionItem != null) {
                if ((AssignmentToShiftStateStringValues.ACCEPTED.equals(state) || AssignmentToShiftStateStringValues.CORRECTED
                        .equals(state)) && L_STATE.equals(ribbonActionItem.getName())) {
                    ribbonActionItem.setEnabled(true);
                } else {
                    ribbonActionItem.setEnabled(isEnabled);
                    ribbonActionItem.setMessage(message);
                }

                ribbonActionItem.requestUpdate(true);
            }
        }
    }

    public void disableFormWhenStateIsAcceptedOrCorrected(final ViewDefinitionState view, final Entity assignmentToShift) {
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

    public void showExternalSyncNotification(final FormComponent assignmentToShiftForm, final Entity assignmentToShift) {
        boolean isExternalSynchronized = assignmentToShift.getBooleanField(AssignmentToShiftFields.EXTERNAL_SYNCHRONIZED);

        if (!isExternalSynchronized) {
            assignmentToShiftForm.addMessage("assignmentToShift.assignmentToShift.info.isWaitingForSync",
                    ComponentState.MessageType.INFO, true);
        }
    }

    public void showLastStateChangeFailNotification(final FormComponent assignmentToShiftForm, final Entity assignmentToShift) {
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