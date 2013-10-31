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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
<<<<<<< HEAD
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.orders.constants.OrderFields;
=======
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
>>>>>>> dev
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.mes.productionPerShift.util.OrderRealizationDaysResolver;
import com.qcadoo.mes.productionPerShift.util.OrderRealizationDaysResolver.OrderRealizationDayWithShifts;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
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

    private static final String L_PROGRESS_FOR_DAYS = "progressForDays";

    private static final String L_DAILY_PROGRESS = "dailyProgress";

    private static final String L_DAY = "day";

    private static final String L_DATE = "date";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PPSHelper ppsHelper;

    @Autowired
    private ProductionPerShiftDetailsHooks detailsHooks;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderRealizationDaysResolver orderRealizationDaysResolver;

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

                Entity order = ((LookupComponent) view.getComponentByReference(L_ORDER)).getEntity();
                EntityTree techInstOperComps = order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
                if (techInstOperComps.isEmpty()) {
                    return;
                }
                Entity root = techInstOperComps.getRoot();
                if (!root.getId().equals(tioc.getId())) {
                    return;
                }
                BigDecimal productionQuantity = new BigDecimal(0);
                List<Entity> plannedPrograssForDay = root.getHasManyField(PROGRESS_FOR_DAYS);
                for (Entity progressForDay : plannedPrograssForDay) {
                    List<Entity> dailyProgreses = progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS);
                    for (Entity dailyProgress : dailyProgreses) {
                        productionQuantity = productionQuantity.add(dailyProgress.getDecimalField(DailyProgressFields.QUANTITY),
                                numberService.getMathContext());
                    }
                }
                BigDecimal planedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                BigDecimal difference = planedQuantity.subtract(productionQuantity, numberService.getMathContext());
                ComponentState form = (ComponentState) view.getComponentByReference("form");
                if (difference.compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    form.addMessage("productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSSmaller", MessageType.INFO,
                            false,
                            numberService.formatWithMinimumFractionDigits(difference.abs(numberService.getMathContext()), 0));
                } else {
                    form.addMessage("productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSGreater", MessageType.INFO,
                            false,
                            numberService.formatWithMinimumFractionDigits(difference.abs(numberService.getMathContext()), 0));

                }

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

<<<<<<< HEAD
=======
    private List<Entity> addCorrectedToPlannedProgressForDay(final Entity toc, final List<Entity> progressForDays) {
        Entity techOperComp = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(toc.getId());
        List<Entity> plannedPrograssForDay = techOperComp.getHasManyField(PROGRESS_FOR_DAYS).find().list().getEntities();
        plannedPrograssForDay.addAll(progressForDays);
        return plannedPrograssForDay;
    }

>>>>>>> dev
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
            deleteCorrectedProgressForDays(view, tioc);
            for (Entity progressForDay : progressForDays) {
                Entity copyProgressForDay = progressForDayDD.copy(progressForDay.getId()).get(0);
                copyProgressForDay = progressForDayDD.get(copyProgressForDay.getId());
                copyProgressForDay.setField(CORRECTED, true);
                progressForDayDD.save(copyProgressForDay);
            }
<<<<<<< HEAD
            tioc.setField(HAS_CORRECTIONS, true);
            tioc.getDataDefinition().save(tioc);
=======
            toc.setField(HAS_CORRECTIONS, true);
            deleteProgressForDays(view, toc);
            toc.setField(PROGRESS_FOR_DAYS, addCorrectedToPlannedProgressForDay(toc, copiedProgressForDays));
            toc.getDataDefinition().save(toc);
>>>>>>> dev
        }
        detailsHooks.fillProgressForDays(view);
    }

    public void deleteProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity toc = ((LookupComponent) view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).getEntity();
        if (toc == null) {
            return;
        } else {
<<<<<<< HEAD
            deleteCorrectedProgressForDays(view, tioc);
=======
            deleteProgressForDays(view, toc);
>>>>>>> dev
        }
        detailsHooks.fillProgressForDays(view);
    }

<<<<<<< HEAD
    private void deleteCorrectedProgressForDays(final ViewDefinitionState view, final Entity tioc) {
=======
    private void deleteProgressForDays(final ViewDefinitionState view, final Entity toc) {
>>>>>>> dev
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

    private Entity getOrderFromOperationLookup(final ViewDefinitionState view) {
        LookupComponent productionPerShiftOperationLookup = (LookupComponent) view
                .getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION);
        return productionPerShiftOperationLookup.getEntity().getBelongsToField(L_ORDER);
    }

<<<<<<< HEAD
    public void updateProgressForDays(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(L_PROGRESS_FOR_DAYS);
        Entity order = getOrderFromOperationLookup(view);
=======
        Entity toc = productionPerShiftOperationLookup.getEntity();
        Entity technology = toc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).uniqueResult();
>>>>>>> dev

        int lastDay = 0;
        boolean isFirstRow = true;
        DateTime orderStartDate = new DateTime(order.getDateField(OrderFields.START_DATE));

        for (FormComponent progressForDay : progressForDaysADL.getFormComponents()) {
            FieldComponent dayField = progressForDay.findFieldComponentByName(L_DAY);
            FieldComponent dateField = progressForDay.findFieldComponentByName(L_DATE);
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDay
                    .findFieldComponentByName(L_DAILY_PROGRESS);

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
            Entity dailyProgressWithShift = ppsHelper.getDailyProgressDataDef().create();
            dailyProgressWithShift.setField(SHIFT, shift.getEntity());
            dailyProgress.add(dailyProgressWithShift);
        }
        return dailyProgress;
    }

}
