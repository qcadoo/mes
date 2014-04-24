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

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.*;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.productionPerShift.util.OrderRealizationDaysResolver;
import com.qcadoo.mes.productionPerShift.util.OrderRealizationDaysResolver.OrderRealizationDayWithShifts;
import com.qcadoo.mes.productionPerShift.util.ProductionPerShiftDataProvider;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;
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

    private static final String L_DATE = "date";

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderRealizationDaysResolver orderRealizationDaysResolver;

    @Autowired
    private PPSHelper ppsHelper;

    @Autowired
    private ProductionPerShiftDetailsHooks detailsHooks;

    @Autowired
    private ProductionPerShiftDataProvider productionPerShiftDataProvider;

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

        FieldComponent plannedProgressTypeField = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);
        String plannedProgressType = plannedProgressTypeField.getFieldValue().toString();

        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();

        FieldComponent plannedProgressCorrectionCommentField = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT);
        String plannedProgressCorrectionComment = plannedProgressCorrectionCommentField.getFieldValue().toString();

        AwesomeDynamicListComponent plannedProgressCorrectionTypesADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES);

        List<Entity> plannedProgressCorrectionTypes = (List<Entity>) plannedProgressCorrectionTypesADL.getFieldValue();

        for (Entity progressForDay : progressForDays) {
            progressForDay.setField(ProgressForDayFields.CORRECTED,
                    plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));
        }

        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);
        Entity productionPerShiftOperation = productionPerShiftOperationLookup.getEntity();

        Entity technologyOperationComponent = productionPerShiftOperation.getDataDefinition().get(
                productionPerShiftOperation.getId());

        boolean hasCorrections = ppsHelper.shouldHasCorrections(view);

        if (technologyOperationComponent != null) {
            technologyOperationComponent.setField(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS, hasCorrections);
            technologyOperationComponent.setField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS,
                    prepareProgressForDaysForTOC(technologyOperationComponent, hasCorrections, progressForDays));

            technologyOperationComponent = technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);

            if (!technologyOperationComponent.isValid()) {
                List<ErrorMessage> errors = technologyOperationComponent.getGlobalErrors();
                for (ErrorMessage error : errors) {
                    state.addMessage(error.getMessage(), MessageType.FAILURE, error.getVars());
                }
            }

            if (state.isHasError()) {
                state.performEvent(view, "initialize", new String[0]);
            } else {
                state.performEvent(view, "save");

                Entity productionPerShift = productionPerShiftForm.getEntity();

                productionPerShift.setField(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT,
                        plannedProgressCorrectionComment);
                productionPerShift.setField(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES,
                        plannedProgressCorrectionTypes);

                productionPerShift.getDataDefinition().save(productionPerShift);

                plannedProgressCorrectionCommentField.setFieldValue(plannedProgressCorrectionComment);
                progressForDaysADL.setFieldValue(progressForDays);
                plannedProgressCorrectionTypesADL.setFieldValue(plannedProgressCorrectionTypes);

                plannedProgressCorrectionCommentField.requestComponentUpdateState();
                progressForDaysADL.requestComponentUpdateState();
                plannedProgressCorrectionTypesADL.requestComponentUpdateState();

                LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);
                Entity order = orderLookup.getEntity();

                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

                EntityTree technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

                if (technologyOperationComponents.isEmpty()) {
                    return;
                }

                Entity technologyTreeRoot = technologyOperationComponents.getRoot();

                if (!technologyTreeRoot.getId().equals(technologyOperationComponent.getId())) {
                    return;
                }

                BigDecimal sumOfDailyPlannedQuantities = productionPerShiftDataProvider.getSumOfQuantities(technology.getId(),
                        ProductionPerShiftDataProvider.ONLY_ROOT_OPERATIONS_CRITERIA);

                BigDecimal planedQuantityFromOrder = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                BigDecimal difference = planedQuantityFromOrder.subtract(sumOfDailyPlannedQuantities,
                        numberService.getMathContext());

                if (difference.compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }

                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    productionPerShiftForm.addMessage("productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSSmaller",
                            MessageType.INFO, false,
                            numberService.formatWithMinimumFractionDigits(difference.abs(numberService.getMathContext()), 0));
                } else {
                    productionPerShiftForm.addMessage("productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSGreater",
                            MessageType.INFO, false,
                            numberService.formatWithMinimumFractionDigits(difference.abs(numberService.getMathContext()), 0));
                }
            }
        }
    }

    private List<Entity> prepareProgressForDaysForTOC(final Entity technologyOperationComponent, final boolean hasCorrections,
            final List<Entity> progressForDays) {
        Entity technologyOperationComponentFromDB = technologyOperationComponent.getDataDefinition().get(
                technologyOperationComponent.getId());

        List<Entity> plannedPrograssForDay = technologyOperationComponentFromDB
                .getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS).find()
                .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, !hasCorrections)).list().getEntities();

        plannedPrograssForDay.addAll(progressForDays);

        return plannedPrograssForDay;
    }

    public void changeView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooks.disablePlannedProgressTypeForPendingOrder(view);
        detailsHooks.disableReasonOfCorrection(view);
        detailsHooks.fillProgressForDays(view);
    }

    public void copyFromPlanned(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);
        Entity technologyOperationComponent = productionPerShiftOperationLookup.getEntity();

        if (technologyOperationComponent == null) {
            return;
        } else {
            FieldComponent plannedProgressTypeField = (FieldComponent) view
                    .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);
            String plannedProgressType = plannedProgressTypeField.getFieldValue().toString();

            List<Entity> progressForDays = getProgressForDayFromTOC(technologyOperationComponent,
                    plannedProgressType.equals(PlannedProgressType.PLANNED.getStringValue()));

            deleteCorrectedProgressForDays(view, technologyOperationComponent);

            for (Entity progressForDay : progressForDays) {
                Entity copyProgressForDay = progressForDay.getDataDefinition().copy(progressForDay.getId()).get(0);
                copyProgressForDay.setField(ProgressForDayFields.CORRECTED, true);

                copyProgressForDay.getDataDefinition().save(copyProgressForDay);
            }

            technologyOperationComponent.setField(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS, true);

            technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
        }

        detailsHooks.fillProgressForDays(view);
    }

    public void deleteProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);
        Entity technologyOperationComponent = productionPerShiftOperationLookup.getEntity();

        if (technologyOperationComponent == null) {
            return;
        } else {
            deleteCorrectedProgressForDays(view, technologyOperationComponent);
        }

        detailsHooks.fillProgressForDays(view);
    }

    private void deleteCorrectedProgressForDays(final ViewDefinitionState view, final Entity technologyOperationComponent) {
        FieldComponent plannedProgressTypeField = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);
        String plannedProgressType = plannedProgressTypeField.getFieldValue().toString();

        List<Entity> progressForDays = getProgressForDayFromTOC(technologyOperationComponent,
                plannedProgressType.equals(PlannedProgressType.CORRECTED.getStringValue()));

        for (Entity progressForDay : progressForDays) {
            progressForDay.getDataDefinition().delete(progressForDay.getId());
        }

        technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
    }

    private List<Entity> getProgressForDayFromTOC(final Entity technologyOperationComponent, final boolean corrected) {
        return technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS).find()
                .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, corrected)).list().getEntities();
    }

    public void updateProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);

        Entity order = orderLookup.getEntity();

        int lastDay = 0;
        boolean isFirstRow = true;
        DateTime orderStartDate = new DateTime(order.getDateField(OrderFields.START_DATE));

        for (FormComponent progressForDayForm : progressForDaysADL.getFormComponents()) {
            FieldComponent dayField = progressForDayForm.findFieldComponentByName(ProgressForDayFields.DAY);
            FieldComponent dateField = progressForDayForm.findFieldComponentByName(L_DATE);

            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDayForm
                    .findFieldComponentByName(ProgressForDayFields.DAILY_PROGRESS);

            Integer dayNum = IntegerUtils.parse((String) dayField.getFieldValue());

            if (dayNum == null) {
                OrderRealizationDayWithShifts dayWithShifts = orderRealizationDaysResolver.find(orderStartDate, lastDay,
                        isFirstRow);
                lastDay = dayWithShifts.getDaysAfterStartDate() + 1;

                dayField.setFieldValue(lastDay);
                dateField.setFieldValue(DateUtils.toDateString(dayWithShifts.getDateTime().toDate()));

                dailyProgressADL.setFieldValue(fillDailyProgressWithShifts(dayWithShifts.getWorkingShifts()));

                dayField.requestComponentUpdateState();
                dateField.requestComponentUpdateState();

                dailyProgressADL.requestComponentUpdateState();
            } else {
                lastDay = dayNum;
            }

            isFirstRow = false;
        }
    }

    private List<Entity> fillDailyProgressWithShifts(final List<Shift> shifts) {
        List<Entity> dailyProgress = Lists.newArrayList();

        for (Shift shift : shifts) {
            Entity dailyProgressWithShift = ppsHelper.getDailyProgressDD().create();

            dailyProgressWithShift.setField(DailyProgressFields.SHIFT, shift.getEntity());
            dailyProgress.add(dailyProgressWithShift);
        }

        return dailyProgress;
    }

}
