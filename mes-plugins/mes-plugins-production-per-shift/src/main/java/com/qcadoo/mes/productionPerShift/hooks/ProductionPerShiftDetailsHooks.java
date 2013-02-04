/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;
import static com.qcadoo.mes.productionPerShift.constants.PlannedProgressType.PLANNED;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.PLANNED_PROGRESS_TYPE;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAILY_PROGRESS;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.HAS_CORRECTIONS;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.PROGRESS_FOR_DAYS;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductionPerShiftDetailsHooks {

    private static final String ORDER_CORRECTED_START_DATE = "orderCorrectedStartDate";

    private static final String ORDER_PLANNED_START_DATE = "orderPlannedStartDate";

    private static final String L_ORDER = "order";

    private static final String L_PROGRESS_FOR_DAYS_ADL = "progressForDaysADL";

    private static final String L_PRODUCTION_PER_SHIFT_OPERATION = "productionPerShiftOperation";

    private static final Long MILLISECONDS_OF_ONE_DAY = 86400000L;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private PPSHelper helper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    public void fillSetRoot(final ViewDefinitionState view) {
        FieldComponent setRootField = (FieldComponent) view.getComponentByReference("setRoot");
        setRootField.setFieldValue(false);
        setRootField.requestComponentUpdateState();
    }

    public void addRootForOperation(final ViewDefinitionState view) {
        FieldComponent setRootField = (FieldComponent) view.getComponentByReference("setRoot");
        if (setRootField.getFieldValue() != null && setRootField.getFieldValue().equals("1")) {
            return;
        }
        Entity order = ((LookupComponent) view.getComponentByReference(L_ORDER)).getEntity();
        EntityTree techInstOperComps = order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        if (techInstOperComps.isEmpty()) {
            return;
        }
        Entity root = techInstOperComps.getRoot();
        FieldComponent operation = (FieldComponent) view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);

        if (operation.getFieldValue() == null) {
            operation.setFieldValue(root.getId());
            setRootField.setFieldValue(true);
        }
        operation.requestComponentUpdateState();
        setRootField.requestComponentUpdateState();
    }

    public void disablePlannedProgressTypeForPendingOrder(final ViewDefinitionState view) {
        Entity order = ((LookupComponent) view.getComponentByReference(L_ORDER)).getEntity();
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        if (plannedProgressType.getFieldValue().equals("") || isPlanned(plannedProgressType.getFieldValue())) {
            plannedProgressType.setFieldValue(PlannedProgressType.PLANNED.getStringValue());
        } else {
            plannedProgressType.setFieldValue(PlannedProgressType.CORRECTED.getStringValue());
        }
        if (order.getStringField(OrderFields.STATE).equals(PENDING.getStringValue())) {
            plannedProgressType.setEnabled(false);
        } else {
            plannedProgressType.setEnabled(true);
        }
        plannedProgressType.requestComponentUpdateState();
    }

    public void fillProducedField(final ViewDefinitionState viewState) {
        ComponentState producesInput = viewState.getComponentByReference("produces");
        if (!producesInput.getFieldValue().equals("")) {
            return;
        }
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);
        Entity tioc = ((LookupComponent) viewState.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        if (tioc == null) {
            progressForDaysADL.setFieldValue(null);
            return;
        } else {
            String producedProduct = null;
            Entity toc = tioc.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);
            Entity prodComp = technologyService.getMainOutputProductComponent(toc);
            Entity prod = prodComp.getBelongsToField("product");
            producedProduct = prod.getStringField("name");
            producesInput.setFieldValue(producedProduct);
            fillUnitFields(viewState);
        }
    }

    private void fillUnitFields(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);
        Entity tioc = ((LookupComponent) viewState.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        Entity toc = tioc.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);
        Entity prodComp = technologyService.getMainOutputProductComponent(toc);
        Entity prod = prodComp.getBelongsToField("product");
        for (FormComponent form : progressForDaysADL.getFormComponents()) {
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) form
                    .findFieldComponentByName("dailyProgressADL");
            for (FormComponent formComponent : dailyProgressADL.getFormComponents()) {
                FieldComponent unit = formComponent.findFieldComponentByName(UNIT);
                unit.setFieldValue(prod.getStringField(UNIT));
                unit.requestComponentUpdateState();
            }
            dailyProgressADL.requestComponentUpdateState();
        }
        progressForDaysADL.requestComponentUpdateState();
    }

    public void setOrderStartDate(final ViewDefinitionState view) {
        Entity order = ((LookupComponent) view.getComponentByReference(L_ORDER)).getEntity();
        FieldComponent orderPlannedStartDate = (FieldComponent) view.getComponentByReference(ORDER_PLANNED_START_DATE);
        FieldComponent orderCorrectedStartDate = (FieldComponent) view.getComponentByReference(ORDER_CORRECTED_START_DATE);
        orderPlannedStartDate.setFieldValue(DateUtils.toDateTimeString((Date) order.getField(OrderFields.DATE_FROM)));
        orderCorrectedStartDate.setFieldValue(DateUtils.toDateTimeString((Date) order.getField(OrderFields.CORRECTED_DATE_FROM)));
        orderPlannedStartDate.requestComponentUpdateState();
        orderCorrectedStartDate.requestComponentUpdateState();
    }

    public void disableReasonOfCorrection(final ViewDefinitionState view) {
        FieldComponent progressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        FieldComponent plannedProgressCorrectionType = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_TYPE);
        FieldComponent plannedProgressCorrectionComment = (FieldComponent) view
                .getComponentByReference(PLANNED_PROGRESS_CORRECTION_COMMENT);
        if (isPlanned(progressType.getFieldValue())) {
            plannedProgressCorrectionType.setEnabled(false);
            plannedProgressCorrectionComment.setEnabled(false);
        } else {
            plannedProgressCorrectionType.setEnabled(true);
            plannedProgressCorrectionComment.setEnabled(true);
        }
        plannedProgressCorrectionType.requestComponentUpdateState();
        plannedProgressCorrectionComment.requestComponentUpdateState();
    }

    public void fillProgressForDays(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);
        Entity tioc = ((LookupComponent) viewState.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        if (tioc == null) {
            progressForDaysADL.setFieldValue(null);
        } else {
            FieldComponent plannedProgressType = ((FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE));
            List<Entity> progressForDays = tioc.getHasManyField(PROGRESS_FOR_DAYS).find()
                    .add(SearchRestrictions.eq(CORRECTED, !isPlanned(plannedProgressType.getFieldValue()))).list().getEntities();
            progressForDaysADL.setFieldValue(progressForDays);
        }
        progressForDaysADL.requestComponentUpdateState();
        disabledComponents(viewState);
    }

    public void refreshProgressForDaysADL(final ViewDefinitionState viewState) {
        fillUnitFields(viewState);
        disabledComponents(viewState);
        if (!progressTypeWasChange(viewState) || !tiocWasChanged(viewState)) {
            return;
        }
        fillProgressForDays(viewState);
    }

    private boolean tiocWasChanged(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);
        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();
        if (progressForDays.isEmpty()) {
            return true;
        }
        Entity tioc = ((LookupComponent) viewState.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        Entity tiocFromPfdays = progressForDays.get(0).getDataDefinition().get(progressForDays.get(0).getId())
                .getBelongsToField(ProgressForDayFields.TECH_INST_OPER_COMP);
        if (!tioc.getId().equals(tiocFromPfdays.getId())) {
            return true;
        }
        return false;
    }

    private boolean progressTypeWasChange(final ViewDefinitionState viewState) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);
        List<Entity> progressForDays = (List<Entity>) progressForDaysADL.getFieldValue();
        if (progressForDays.isEmpty()) {
            return true;
        }
        FieldComponent progressType = (FieldComponent) viewState.getComponentByReference(PLANNED_PROGRESS_TYPE);
        boolean corrected = progressForDays.get(0).getBooleanField(CORRECTED);
        if ((isPlanned(progressType.getFieldValue()) && !corrected)) {
            return true;
        }
        return false;
    }

    private boolean isPlanned(final Object progressType) {
        return PLANNED.getStringValue().equals(progressType);
    }

    private void disabledComponents(final ViewDefinitionState viewState) {
        Entity order = ((LookupComponent) viewState.getComponentByReference(L_ORDER)).getEntity();
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) viewState
                .getComponentByReference(L_PROGRESS_FOR_DAYS_ADL);
        boolean shouldDisabled = helper.shouldHasCorrections(viewState)
                || order.getStringField(STATE).equals(OrderState.PENDING.getStringValue());
        progressForDaysADL.setEnabled(shouldDisabled);
        progressForDaysADL.requestComponentUpdateState();
        for (FormComponent form : progressForDaysADL.getFormComponents()) {
            ((FieldComponent) form.findFieldComponentByName("day")).setEnabled(shouldDisabled);
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) form
                    .findFieldComponentByName("dailyProgressADL");
            dailyProgressADL.setEnabled(shouldDisabled);
            dailyProgressADL.requestComponentUpdateState();
            for (FormComponent dailyProgressForm : dailyProgressADL.getFormComponents()) {
                FieldComponent shift = (FieldComponent) dailyProgressForm.findFieldComponentByName("shift");
                FieldComponent quantity = (FieldComponent) dailyProgressForm.findFieldComponentByName("quantity");
                FieldComponent unit = (FieldComponent) dailyProgressForm.findFieldComponentByName(UNIT);
                shift.setEnabled(shouldDisabled);
                shift.requestComponentUpdateState();
                quantity.setEnabled(shouldDisabled);
                quantity.requestComponentUpdateState();
                unit.setEnabled(shouldDisabled);
                unit.requestComponentUpdateState();
            }
        }
    }

    public void changedButtonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup progressSelectedOperation = (RibbonGroup) window.getRibbon().getGroupByName("progress");
        RibbonActionItem clearButton = (RibbonActionItem) progressSelectedOperation.getItemByName("clear");
        RibbonActionItem copyButton = (RibbonActionItem) progressSelectedOperation.getItemByName("copyFromPlanned");
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PLANNED_PROGRESS_TYPE);
        if (isPlanned(plannedProgressType.getFieldValue())) {
            clearButton.setEnabled(false);
            copyButton.setEnabled(false);
        } else {
            clearButton.setEnabled(true);
            copyButton.setEnabled(true);
        }
        clearButton.requestUpdate(true);
        copyButton.requestUpdate(true);
    }

    public void checkIfWasItCorrected(final ViewDefinitionState view) {
        Entity order = ((LookupComponent) view.getComponentByReference(L_ORDER)).getEntity();
        List<Entity> tiocWithCorrectedPlan = dataDefinitionService
                .get(PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(L_ORDER, order))
                .add(SearchRestrictions.eq(TechInstOperCompFieldsPPS.HAS_CORRECTIONS, true)).list().getEntities();
        FieldComponent wasItCorrected = (FieldComponent) view.getComponentByReference("wasItCorrected");
        if (tiocWithCorrectedPlan.isEmpty()) {
            wasItCorrected.setFieldValue(false);
        } else {
            wasItCorrected.setFieldValue(true);
        }
        wasItCorrected.requestComponentUpdateState();
    }

    public void checkShiftsIfWorks(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        final Entity productionPerShift = form.getEntity();
        final Entity order = productionPerShift.getBelongsToField(ProductionPerShiftFields.ORDER);
        if (order == null) {
            return;
        }
        List<Entity> tiocs = order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        if (tiocs == null) {
            final Entity orderFromDb = order.getDataDefinition().get(order.getId());
            tiocs = orderFromDb.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
            if (tiocs == null) {
                return;
            }
        }
        for (Entity tioc : tiocs) {
            checkShiftsIfWorks(form, tioc);
        }
    }

    private void checkShiftsIfWorks(final FormComponent formComponent, final Entity tioc) {
        final List<Entity> progressForDays = tioc.getHasManyField("progressForDays");
        for (Entity progressForDay : progressForDays) {
            if ((progressForDay.getBooleanField(CORRECTED) && !tioc.getBooleanField(HAS_CORRECTIONS))) {
                continue;
            }
            final List<Entity> dailyProgressList = progressForDay.getHasManyField(DAILY_PROGRESS);
            for (Entity dailyProgress : dailyProgressList) {
                Entity shift = dailyProgress.getBelongsToField("shift");
                if (shift == null) {
                    continue;
                }
                boolean isFirstDailyProgress = dailyProgressList.get(0).equals(dailyProgress);
                if (!checkIfShiftWorks(progressForDays, progressForDay, tioc, shift, isFirstDailyProgress)) {
                    final String shiftName = shift.getStringField(ShiftFields.NAME);
                    final String workDate = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault())
                            .format(getDateAfterStartOrderForProgress(tioc.getBelongsToField(L_ORDER), progressForDay));
                    formComponent.addMessage("productionPerShift.progressForDay.shiftDoesNotWork", MessageType.INFO, shiftName,
                            workDate);
                }
            }
        }
    }

    private boolean checkIfShiftWorks(final List<Entity> progressForDays, final Entity progressForDay, final Entity tioc,
            final Entity shift, final boolean isFirstDailyProgress) {
        boolean works = false;
        if (progressForDay.equals(progressForDays.get(0)) && isFirstDailyProgress) {
            Entity shiftFromDay = shiftsService.getShiftFromDateWithTime(getDateAfterStartOrderForProgress(
                    tioc.getBelongsToField(L_ORDER), progressForDay));
            if (shiftFromDay == null) {
                works = false;
            } else if (shift.getId().equals(shiftFromDay.getId())) {
                works = true;
            }
        } else {
            works = shiftsService.checkIfShiftWorkAtDate(
                    getDateAfterStartOrderForProgress(tioc.getBelongsToField(L_ORDER), progressForDay), shift);
        }
        return works;
    }

    private Date getDateAfterStartOrderForProgress(final Entity order, final Entity progressForDay) {
        final Integer day = Integer.valueOf(progressForDay.getField(DAY).toString());
        final Date startOrder = getPlannedOrCorrectedDate(order);
        return new Date(startOrder.getTime() + (day - 1) * MILLISECONDS_OF_ONE_DAY);
    }

    private Date getPlannedOrCorrectedDate(final Entity order) {
        if (order.getField(CORRECTED_DATE_FROM) == null) {
            return (Date) order.getField(DATE_FROM);
        } else {
            return (Date) order.getField(CORRECTED_DATE_FROM);
        }
    }

}
