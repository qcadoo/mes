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

import static com.qcadoo.mes.productionPerShift.constants.DailyProgressFields.SHIFT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.HAS_CORRECTIONS;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.PROGRESS_FOR_DAYS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
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

    private static final String L_PRODUCTION_PER_SHIFT_OPERATION = "productionPerShiftOperation";

    private static final String L_PROGRESS_FOR_DAYS = "progressForDays";

    private static final String L_DAILY_PROGRESS = "dailyProgress";

    private static final String L_DAY = "day";

    private static final String L_DATE = "date";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private PPSHelper ppsHelper;

    @Autowired
    private ProductionPerShiftDetailsHooks detailsHooks;

    public void redirectToProductionPerShift(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId == null) {
            return;
        }

        Long ppsId = ppsHelper.getPpsIdForOrder(orderId);
        if (ppsId == null) {
            ppsId = ppsHelper.createPpsForOrderAndReturnId(orderId);
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
                .getComponentByReference(L_PROGRESS_FOR_DAYS);

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

        Entity tocComponent = ((LookupComponent) view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        DataDefinition tocDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        Entity toc = tocDD.get(tocComponent.getId());
        boolean hasCorrections = ppsHelper.shouldHasCorrections(view);

        if (toc != null) {
            toc.setField(HAS_CORRECTIONS, hasCorrections);
            toc.setField(PROGRESS_FOR_DAYS, prepareProgressForDaysForTOC(toc, hasCorrections, progressForDays));
            toc = toc.getDataDefinition().save(toc);

            if (!toc.isValid()) {
                List<ErrorMessage> errors = toc.getGlobalErrors();
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

                plannedProgressCorrectionCommentField.requestComponentUpdateState();
                progressForDaysADL.requestComponentUpdateState();
                plannedProgressCorrectionTypesADL.requestComponentUpdateState();
            }
        }
    }

    private List<Entity> prepareProgressForDaysForTOC(final Entity toc, final boolean hasCorrections,
            final List<Entity> progressForDays) {
        Entity techOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(toc.getId());
        List<Entity> plannedPrograssForDay = techOperComp.getHasManyField(PROGRESS_FOR_DAYS).find()
                .add(SearchRestrictions.eq(CORRECTED, !hasCorrections)).list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

    private List<Entity> addCorrectedToPlannedProgressForDay(final Entity toc, final List<Entity> progressForDays) {
        Entity techOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(toc.getId());
        List<Entity> plannedPrograssForDay = techOperComp.getHasManyField(PROGRESS_FOR_DAYS).find().list().getEntities();
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
        Entity toc = ((LookupComponent) view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        if (toc == null) {
            return;
        } else {
            String plannedProgressType = ((FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue()
                    .toString();
            List<Entity> progressForDays = getProgressForDayFromTOC(toc,
                    plannedProgressType.equals(PlannedProgressType.PLANNED.getStringValue()));
            List<Entity> copiedProgressForDays = new ArrayList<Entity>();
            for (Entity progressForDay : progressForDays) {
                Entity copyProgressForDay = progressForDayDD.copy(progressForDay.getId()).get(0);
                copyProgressForDay.setField(CORRECTED, true);
                copiedProgressForDays.add(copyProgressForDay);
            }
            toc.setField(HAS_CORRECTIONS, true);
            deleteProgressForDays(view, toc);
            toc.setField(PROGRESS_FOR_DAYS, addCorrectedToPlannedProgressForDay(toc, copiedProgressForDays));
            toc.getDataDefinition().save(toc);
        }
        detailsHooks.fillProgressForDays(view);
    }

    public void deleteProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity toc = ((LookupComponent) view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        if (toc == null) {
            return;
        } else {
            deleteProgressForDays(view, toc);
        }
        detailsHooks.fillProgressForDays(view);
    }

    private void deleteProgressForDays(final ViewDefinitionState view, final Entity toc) {
        String plannedProgressType = ((FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE)).getFieldValue()
                .toString();
        List<Entity> progressForDays = getProgressForDayFromTOC(toc,
                plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        for (Entity progressForDay : progressForDays) {
            progressForDay.getDataDefinition().delete(progressForDay.getId());
        }
        toc.getDataDefinition().save(toc);
    }

    private List<Entity> getProgressForDayFromTOC(final Entity toc, final boolean corrected) {
        return toc.getHasManyField(PROGRESS_FOR_DAYS).find().add(SearchRestrictions.eq(CORRECTED, corrected)).list()
                .getEntities();
    }

    public void updateProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(L_PROGRESS_FOR_DAYS);

        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        Entity toc = productionPerShiftOperationLookup.getEntity();
        Entity technology = toc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).uniqueResult();

        Integer lastDay = null;

        for (FormComponent progressForDay : progressForDaysADL.getFormComponents()) {
            FieldComponent dayField = progressForDay.findFieldComponentByName(L_DAY);
            FieldComponent dateField = progressForDay.findFieldComponentByName(L_DATE);

            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDay
                    .findFieldComponentByName(L_DAILY_PROGRESS);

            String day = (String) dayField.getFieldValue();
            Date date = null;
            List<Entity> shifts = Lists.newArrayList();

            if (StringUtils.isEmpty(day)) {
                if (lastDay == null) {
                    lastDay = 0;
                }

                do {
                    lastDay++;

                    date = ppsHelper.getDateAfterStartOrderForProgress(order, lastDay);

                    shifts = shiftsService.getShiftsWorkingAtDate(date);
                } while (shifts.isEmpty());

                dayField.setFieldValue(lastDay);
                dateField.setFieldValue(DateUtils.toDateString(date));

                dailyProgressADL.setFieldValue(fillDailyProgressWithShifts(shifts));

                dayField.requestComponentUpdateState();
                dateField.requestComponentUpdateState();

                dailyProgressADL.requestComponentUpdateState();
            } else {
                lastDay = Integer.parseInt(day);
            }
        }
    }

    private List<Entity> fillDailyProgressWithShifts(final List<Entity> shifts) {
        List<Entity> dailyProgress = Lists.newArrayList();

        for (Entity shift : shifts) {
            Entity dailyProgressWithShift = ppsHelper.getDailyProgressDataDef().create();

            dailyProgressWithShift.setField(SHIFT, shift);

            dailyProgress.add(dailyProgressWithShift);
        }

        return dailyProgress;
    }

}
