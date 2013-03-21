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
package com.qcadoo.mes.productionPerShift.listeners;

import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.HAS_CORRECTIONS;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.PROGRESS_FOR_DAYS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionPerShiftListeners {

    private static final String L_FORM = "form";

    private static final String L_PROGRESS_FOR_DAYS_ADL = "progressForDays";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PPSHelper helper;

    @Autowired
    private ProductionPerShiftDetailsHooks detailsHooks;

    public void redirectToProductionPerShift(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId == null) {
            return;
        }

        Long ppsId = helper.getPpsIdForOrder(orderId);
        if (ppsId == null) {
            ppsId = helper.createPpsForOrderAndReturnId(orderId);
            Preconditions.checkNotNull(ppsId);
        }
        redirect(view, ppsId);
    }

    private void redirect(final ViewDefinitionState viewState, final Long ppsId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", ppsId);

        String url = "../page/productionPerShift/productionPerShiftDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public void fillProducedField(final ViewDefinitionState viewState, final ComponentState componentState, final String[] args) {
        detailsHooks.fillProducedField(viewState);
    }

    /**
     * Fill outer AwesomeDynamicList with entities fetched from db. Disable ADL if operation lookup is empty.
     * 
     * @param view
     * @param state
     * @param args
     */
    public void fillProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooks.fillProgressForDays(view);
    }

    /**
     * Save outer AwesomeDynamicList entities in db and reset operation lookup & related components
     * 
     * @param view
     * @param state
     * @param args
     */
    @SuppressWarnings("unchecked")
    public void saveProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productionPerShiftForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent plannedProgressTypeField = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        String plannedProgressType = plannedProgressTypeField.getFieldValue().toString();

        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);

        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();

        FieldComponent plannedProgressCorrectionCommentField = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_COMMENT);
        String plannedProgressCorrectionComment = plannedProgressCorrectionCommentField.getFieldValue().toString();

        AwesomeDynamicListComponent plannedProgressCorrectionTypesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_TYPES);

        List<Entity> plannedProgressCorrectionTypes = (List<Entity>) plannedProgressCorrectionTypesADL.getFieldValue();

        for (Entity progressForDay : progressForDays) {
            progressForDay.setField(CORRECTED, plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        }

        Entity tioc = ((LookupComponent) view.getComponentByReference("productionPerShiftOperation")).getEntity();

        boolean hasCorrections = helper.shouldHasCorrections(view);

        if (tioc != null) {
            tioc.setField(HAS_CORRECTIONS, hasCorrections);
            tioc.setField(PROGRESS_FOR_DAYS, prepareProgressForDaysForTIOC(tioc, hasCorrections, progressForDays));
            tioc = tioc.getDataDefinition().save(tioc);

            if (!tioc.isValid()) {
                List<ErrorMessage> errors = tioc.getGlobalErrors();
                for (ErrorMessage error : errors) {
                    state.addMessage(error.getMessage(), MessageType.FAILURE, error.getVars());
                }
            }

            if (state.isHasError()) {
                state.performEvent(view, "initialize", new String[0]);
            } else {
                state.performEvent(view, "save");

                Entity productionPerShift = productionPerShiftForm.getEntity();

                productionPerShift.setField(PLANNED_PROGRESS_CORRECTION_COMMENT, plannedProgressCorrectionComment);
                productionPerShift.setField(PLANNED_PROGRESS_CORRECTION_TYPES, plannedProgressCorrectionTypes);

                productionPerShift.getDataDefinition().save(productionPerShift);

                plannedProgressCorrectionCommentField.setFieldValue(plannedProgressCorrectionComment);
                progressForDaysADL.setFieldValue(progressForDays);
                plannedProgressCorrectionTypesADL.setFieldValue(plannedProgressCorrectionTypes);
            }
        }
    }

    private List<Entity> prepareProgressForDaysForTIOC(final Entity tioc, final boolean hasCorrections,
            final List<Entity> progressForDays) {
        Entity techInstOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(tioc.getId());
        List<Entity> plannedPrograssForDay = techInstOperComp.getHasManyField(PROGRESS_FOR_DAYS).find()
                .add(SearchRestrictions.eq(CORRECTED, !hasCorrections)).list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    private List<Entity> addCorrectedToPlannedProgressForDay(final Entity tioc, final List<Entity> progressForDays) {
        Entity techInstOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).get(tioc.getId());
        List<Entity> plannedPrograssForDay = techInstOperComp.getHasManyField(PROGRESS_FOR_DAYS).find().list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    public void changeView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooks.disablePlannedProgressTypeForPendingOrder(view);
        detailsHooks.disableReasonOfCorrection(view);
        detailsHooks.fillProgressForDays(view);
    }

    public void copyFromPlanned(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        DataDefinition progressForDayDD = dataDefinitionService.get(PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
        Entity tioc = ((LookupComponent) view.getComponentByReference("productionPerShiftOperation")).getEntity();
        if (tioc == null) {
            return;
        } else {
            String plannedProgressType = ((FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue()
                    .toString();
            List<Entity> progressForDays = getProgressForDayFromTIOC(tioc,
                    plannedProgressType.equals(PlannedProgressType.PLANNED.getStringValue()));
            List<Entity> copiedProgressForDays = new ArrayList<Entity>();
            for (Entity progressForDay : progressForDays) {
                Entity copyProgressForDay = progressForDayDD.copy(progressForDay.getId()).get(0);
                copyProgressForDay.setField(CORRECTED, true);
                copiedProgressForDays.add(copyProgressForDay);
            }
            tioc.setField(HAS_CORRECTIONS, true);
            deleteProgressForDays(view, tioc);
            tioc.setField(PROGRESS_FOR_DAYS, addCorrectedToPlannedProgressForDay(tioc, copiedProgressForDays));
            tioc.getDataDefinition().save(tioc);
        }
        detailsHooks.fillProgressForDays(view);
    }

    public void deleteProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity tioc = ((LookupComponent) view.getComponentByReference("productionPerShiftOperation")).getEntity();
        if (tioc == null) {
            return;
        } else {
            deleteProgressForDays(view, tioc);
        }
        detailsHooks.fillProgressForDays(view);
    }

    private void deleteProgressForDays(final ViewDefinitionState view, final Entity tioc) {
        String plannedProgressType = ((FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue()
                .toString();
        List<Entity> progressForDays = getProgressForDayFromTIOC(tioc,
                plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        for (Entity progressForDay : progressForDays) {
            progressForDay.getDataDefinition().delete(progressForDay.getId());
        }
        tioc.getDataDefinition().save(tioc);
    }

    private List<Entity> getProgressForDayFromTIOC(final Entity tioc, final boolean corrected) {
        return tioc.getHasManyField(PROGRESS_FOR_DAYS).find().add(SearchRestrictions.eq(CORRECTED, corrected)).list()
                .getEntities();
    }

}
