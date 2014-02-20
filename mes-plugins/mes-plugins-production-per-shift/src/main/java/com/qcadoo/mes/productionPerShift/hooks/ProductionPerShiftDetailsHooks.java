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
package com.qcadoo.mes.productionPerShift.hooks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.TechnologyOperationComponentFieldsPPS;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductionPerShiftDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_PROGRESS = "progress";

    private static final String L_COPY_FROM_PLANNED = "copyFromPlanned";

    private static final String L_CLEAR = "clear";

    private static final String L_PRODUCTION_PER_SHIFT_OPERATION = "productionPerShiftOperation";

    private static final String L_PRODUCES = "produces";

    private static final String L_SET_ROOT = "setRoot";

    private static final String L_DATE = "date";

    private static final String L_UNIT = "unit";

    private static final String L_WAS_IT_CORRECTED = "wasItCorrected";

    private static final String L_ORDER_CORRECTED_START_DATE = "orderCorrectedStartDate";

    private static final String L_ORDER_PLANNED_START_DATE = "orderPlannedStartDate";

    private static final String L_ORDER_EFFECTIVE_START_DATE = "orderEffectiveStartDate";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private PPSHelper ppsHelper;

    public void setTechnologyField(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.TECHNOLOGY);
        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        technologyLookup.setFieldValue(technology.getId());
        technologyLookup.requestComponentUpdateState();

        productionPerShiftOperationLookup.setEnabled(true);
        productionPerShiftOperationLookup.requestComponentUpdateState();
    }

    public void fillSetRoot(final ViewDefinitionState view) {
        CheckBoxComponent setRootCheckBox = (CheckBoxComponent) view.getComponentByReference(L_SET_ROOT);

        setRootCheckBox.setChecked(false);
        setRootCheckBox.requestComponentUpdateState();
    }

    public void addRootForOperation(final ViewDefinitionState view) {
        CheckBoxComponent setRootCheckBox = (CheckBoxComponent) view.getComponentByReference(L_SET_ROOT);

        if (setRootCheckBox.isChecked()) {
            return;
        }

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);
        Entity order = orderLookup.getEntity();

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        EntityTree technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (technologyOperationComponents.isEmpty()) {
            return;
        }

        Entity root = technologyOperationComponents.getRoot();

        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        if (productionPerShiftOperationLookup.getFieldValue() == null) {
            productionPerShiftOperationLookup.setFieldValue(root.getId());
            setRootCheckBox.setChecked(true);
        }

        productionPerShiftOperationLookup.requestComponentUpdateState();
        setRootCheckBox.requestComponentUpdateState();
    }

    public void disablePlannedProgressTypeForPendingOrder(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);
        FieldComponent plannedProgressType = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);

        Entity order = orderLookup.getEntity();

        if (StringUtils.isEmpty((String) plannedProgressType.getFieldValue()) || isPlanned(plannedProgressType.getFieldValue())) {
            plannedProgressType.setFieldValue(PlannedProgressType.PLANNED.getStringValue());
        } else {
            plannedProgressType.setFieldValue(PlannedProgressType.CORRECTED.getStringValue());
        }

        if (OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE))) {
            plannedProgressType.setEnabled(false);
        } else {
            plannedProgressType.setEnabled(true);
        }

        plannedProgressType.requestComponentUpdateState();
    }

    public void fillProducedField(final ViewDefinitionState view) {
        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);
        LookupComponent producesField = (LookupComponent) view.getComponentByReference(L_PRODUCES);

        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        Entity productionPerShiftOperation = productionPerShiftOperationLookup.getEntity();

        if (productionPerShiftOperation == null) {
            progressForDaysADL.setFieldValue(null);

            return;
        } else {
            Entity operationProductOutComponent = technologyService.getMainOutputProductComponent(productionPerShiftOperation);

            Entity product = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);

            producesField.setFieldValue(product.getId());
            producesField.requestComponentUpdateState();

            fillUnitFields(view);
        }
    }

    private void fillUnitFields(final ViewDefinitionState view) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        Entity productionPerShiftOperation = productionPerShiftOperationLookup.getEntity();

        Entity operationProductOutComponent = technologyService.getMainOutputProductComponent(productionPerShiftOperation);
        Entity product = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);

        for (FormComponent progressForDayForm : progressForDaysADL.getFormComponents()) {
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDayForm
                    .findFieldComponentByName(ProgressForDayFields.DAILY_PROGRESS);

            for (FormComponent dailyProgressForm : dailyProgressADL.getFormComponents()) {
                FieldComponent unitField = dailyProgressForm.findFieldComponentByName(L_UNIT);

                unitField.setFieldValue(product.getStringField(ProductFields.UNIT));
                unitField.requestComponentUpdateState();
            }

            dailyProgressADL.requestComponentUpdateState();
        }

        progressForDaysADL.requestComponentUpdateState();
    }

    public void setOrderStartDate(final ViewDefinitionState view) {
        FieldComponent orderPlannedStartDate = (FieldComponent) view.getComponentByReference(L_ORDER_PLANNED_START_DATE);
        FieldComponent orderCorrectedStartDate = (FieldComponent) view.getComponentByReference(L_ORDER_CORRECTED_START_DATE);
        FieldComponent orderEffectiveStartDate = (FieldComponent) view.getComponentByReference(L_ORDER_EFFECTIVE_START_DATE);

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);

        Entity order = orderLookup.getEntity();

        orderPlannedStartDate.setFieldValue(DateUtils.toDateTimeString(order.getDateField(OrderFields.DATE_FROM)));
        orderCorrectedStartDate.setFieldValue(DateUtils.toDateTimeString(order.getDateField(OrderFields.CORRECTED_DATE_FROM)));
        orderEffectiveStartDate.setFieldValue(DateUtils.toDateTimeString(order.getDateField(OrderFields.EFFECTIVE_DATE_FROM)));

        orderPlannedStartDate.requestComponentUpdateState();
        orderCorrectedStartDate.requestComponentUpdateState();
        orderEffectiveStartDate.requestComponentUpdateState();
    }

    public void disableReasonOfCorrection(final ViewDefinitionState view) {
        FieldComponent progressType = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);

        if (isPlanned(progressType.getFieldValue())) {
            setResonOfCorrectionEnabled(view, false);
        } else {
            setResonOfCorrectionEnabled(view, true);
        }
    }

    private void setResonOfCorrectionEnabled(final ViewDefinitionState view, final boolean enabled) {
        AwesomeDynamicListComponent plannedProgressCorrectionTypes = (AwesomeDynamicListComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES);
        FieldComponent plannedProgressCorrectionComment = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT);

        plannedProgressCorrectionTypes.setEnabled(enabled);

        for (FormComponent plannedProgressCorrectionTypeForm : plannedProgressCorrectionTypes.getFormComponents()) {
            plannedProgressCorrectionTypeForm.setFormEnabled(enabled);
        }

        plannedProgressCorrectionComment.setEnabled(enabled);
    }

    public void fillProgressForDays(final ViewDefinitionState view) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        Entity productionPerShiftOperation = productionPerShiftOperationLookup.getEntity();

        if (productionPerShiftOperation == null) {
            progressForDaysADL.setFieldValue(null);
        } else {
            FieldComponent plannedProgressType = ((FieldComponent) view
                    .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE));

            List<Entity> progressForDays = productionPerShiftOperation
                    .getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS).find()
                    .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, !isPlanned(plannedProgressType.getFieldValue())))
                    .list().getEntities();

            progressForDaysADL.setFieldValue(progressForDays);
        }

        progressForDaysADL.requestComponentUpdateState();

        fillUnitFields(view);
        disableComponents(view);
    }

    public void refreshProgressForDaysADL(final ViewDefinitionState view) {
        fillUnitFields(view);

        if (!progressTypeWasChanged(view) || !technologyOperationComponentWasChanged(view)) {
            return;
        }

        fillProgressForDays(view);
    }

    @SuppressWarnings("unchecked")
    private boolean technologyOperationComponentWasChanged(final ViewDefinitionState view) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();

        if (progressForDays.isEmpty()) {
            return true;
        }

        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        Entity productionPerShiftOperation = productionPerShiftOperationLookup.getEntity();
        Entity progressForDay = progressForDays.get(0);

        if (progressForDay.getId() == null) {
            return false;
        }

        Entity technologyOperationComponentFromProgressForDays = progressForDay.getDataDefinition().get(progressForDay.getId())
                .getBelongsToField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT);

        if (!productionPerShiftOperation.getId().equals(technologyOperationComponentFromProgressForDays.getId())) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean progressTypeWasChanged(final ViewDefinitionState view) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();

        if (progressForDays.isEmpty()) {
            return true;
        }

        FieldComponent progressType = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);

        boolean corrected = progressForDays.get(0).getBooleanField(ProgressForDayFields.CORRECTED);

        if (isPlanned(progressType.getFieldValue()) && !corrected) {
            return true;
        }

        return false;
    }

    private boolean isPlanned(final Object progressType) {
        return PlannedProgressType.PLANNED.getStringValue().equals(progressType);
    }

    private void disableComponents(final ViewDefinitionState view) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);

        Entity order = orderLookup.getEntity();

        boolean isEnabled = (ppsHelper.shouldHasCorrections(view) || OrderState.PENDING.getStringValue().equals(
                order.getStringField(OrderFields.STATE)));

        for (FormComponent progressForDaysForm : progressForDaysADL.getFormComponents()) {
            FieldComponent dayField = (FieldComponent) progressForDaysForm.findFieldComponentByName(ProgressForDayFields.DAY);

            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDaysForm
                    .findFieldComponentByName(ProgressForDayFields.DAILY_PROGRESS);

            for (FormComponent dailyProgressForm : dailyProgressADL.getFormComponents()) {
                LookupComponent shiftLookup = (LookupComponent) dailyProgressForm
                        .findFieldComponentByName(DailyProgressFields.SHIFT);
                FieldComponent quantityField = (FieldComponent) dailyProgressForm
                        .findFieldComponentByName(DailyProgressFields.QUANTITY);
                FieldComponent unitField = (FieldComponent) dailyProgressForm.findFieldComponentByName(L_UNIT);

                shiftLookup.setEnabled(isEnabled);
                shiftLookup.requestComponentUpdateState();
                quantityField.setEnabled(isEnabled);
                quantityField.requestComponentUpdateState();
                unitField.setEnabled(isEnabled);
                unitField.requestComponentUpdateState();
            }

            dayField.setEnabled(isEnabled);
            dayField.requestComponentUpdateState();

            dailyProgressADL.setEnabled(isEnabled);
            dailyProgressADL.requestComponentUpdateState();
        }

        progressForDaysADL.setEnabled(isEnabled);
        progressForDaysADL.requestComponentUpdateState();
    }

    public void changeButtonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup progress = (RibbonGroup) window.getRibbon().getGroupByName(L_PROGRESS);

        RibbonActionItem clear = (RibbonActionItem) progress.getItemByName(L_CLEAR);
        RibbonActionItem copyFromPlanned = (RibbonActionItem) progress.getItemByName(L_COPY_FROM_PLANNED);

        FieldComponent plannedProgressType = (FieldComponent) view
                .getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE);

        if (isPlanned(plannedProgressType.getFieldValue())) {
            clear.setEnabled(false);
            copyFromPlanned.setEnabled(false);
        } else {
            clear.setEnabled(true);
            copyFromPlanned.setEnabled(true);
        }

        clear.requestUpdate(true);
        copyFromPlanned.requestUpdate(true);
    }

    public void checkIfWasItCorrected(final ViewDefinitionState view) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.TECHNOLOGY);
        Entity technology = technologyLookup.getEntity();

        List<Entity> technologyOperationComponentsWithCorrectedPlan = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                .add(SearchRestrictions.eq(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS, true)).list().getEntities();

        CheckBoxComponent wasItCorrected = (CheckBoxComponent) view.getComponentByReference(L_WAS_IT_CORRECTED);

        if (technologyOperationComponentsWithCorrectedPlan.isEmpty()) {
            wasItCorrected.setChecked(false);
        } else {
            wasItCorrected.setChecked(true);
        }

        wasItCorrected.requestComponentUpdateState();
    }

    public void checkShiftsIfWorks(final ViewDefinitionState view) {
        final FormComponent productionPerShiftForm = (FormComponent) view.getComponentByReference(L_FORM);
        final Entity productionPerShift = productionPerShiftForm.getEntity();

        final Entity order = productionPerShift.getBelongsToField(ProductionPerShiftFields.ORDER);

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        List<Entity> technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (technologyOperationComponents == null) {
            final Entity orderFromDb = order.getDataDefinition().get(order.getId());

            technology = orderFromDb.getBelongsToField(OrderFields.TECHNOLOGY);

            technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

            if (technologyOperationComponents == null) {
                return;
            }
        }

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            checkShiftsIfWorks(productionPerShiftForm, order, technologyOperationComponent);
        }
    }

    private void checkShiftsIfWorks(final FormComponent productionPerShiftForm, final Entity order,
            final Entity technologyOperationComponent) {
        final List<Entity> progressForDays = technologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        for (Entity progressForDay : progressForDays) {
            if ((progressForDay.getBooleanField(ProgressForDayFields.CORRECTED) && !technologyOperationComponent
                    .getBooleanField(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS))) {
                continue;
            }

            final List<Entity> dailyProgressList = progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS);

            for (Entity dailyProgress : dailyProgressList) {
                Entity shift = dailyProgress.getBelongsToField(DailyProgressFields.SHIFT);

                if (shift == null) {
                    continue;
                }

                boolean isFirstDailyProgress = dailyProgressList.get(0).equals(dailyProgress);

                if (!checkIfShiftWorks(progressForDays, progressForDay, shift, order, isFirstDailyProgress)) {
                    final String shiftName = shift.getStringField(ShiftFields.NAME);
                    final String workDate = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(ppsHelper.getDateAfterStartOrderForProgress(order, progressForDay));

                    productionPerShiftForm.addMessage("productionPerShift.progressForDay.shiftDoesNotWork", MessageType.INFO,
                            shiftName, workDate);
                }
            }
        }
    }

    private boolean checkIfShiftWorks(final List<Entity> progressForDays, final Entity progressForDay, final Entity shift,
            final Entity order, final boolean isFirstDailyProgress) {
        boolean works = false;

        if (progressForDay.equals(progressForDays.get(0)) && isFirstDailyProgress) {
            Entity shiftFromDay = shiftsService.getShiftFromDateWithTime(ppsHelper.getDateAfterStartOrderForProgress(order,
                    progressForDay));

            if (shiftFromDay == null) {
                works = false;
            } else if (shift.getId().equals(shiftFromDay.getId())) {
                works = true;
            }
        } else {
            works = shiftsService.checkIfShiftWorkAtDate(ppsHelper.getDateAfterStartOrderForProgress(order, progressForDay),
                    shift);
        }

        return works;
    }

    public void updateProgressForDaysDates(final ViewDefinitionState view) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS);

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionPerShiftFields.ORDER);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        for (FormComponent progressForDayForm : progressForDaysADL.getFormComponents()) {
            FieldComponent dayField = progressForDayForm.findFieldComponentByName(ProgressForDayFields.DAY);
            FieldComponent dateField = progressForDayForm.findFieldComponentByName(L_DATE);

            Entity progressForDay = progressForDayForm.getEntity();

            if (StringUtils.isEmpty(progressForDay.getStringField(ProgressForDayFields.DATE_OF_DAY))) {
                String day = (String) dayField.getFieldValue();

                if (!StringUtils.isEmpty(day)) {
                    Date date = ppsHelper.getDateAfterStartOrderForProgress(order, Integer.parseInt(day));

                    dateField.setFieldValue(DateUtils.toDateString(date));
                    dateField.requestComponentUpdateState();
                }
            }
        }

        progressForDaysADL.requestComponentUpdateState();
    }

}
