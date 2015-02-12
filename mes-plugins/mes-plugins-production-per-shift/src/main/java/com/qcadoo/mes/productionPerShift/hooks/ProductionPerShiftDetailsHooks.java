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
package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qcadoo.commons.functional.FluentOptional;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.dates.OrderDates;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.constants.TechnologyOperationComponentFieldsPPS;
import com.qcadoo.mes.productionPerShift.dataProvider.ProductionPerShiftDataProvider;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.tree.MainTocOutputProductProvider;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyOperationDataProvider;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.EntityUtils;
import com.qcadoo.view.api.ComponentState;
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

    private static final String WINDOW_REF = "window";

    private static final String PROGRESS_RIBBON_GROUP_NAME = "progress";

    private static final String PROGRESS_TYPE_COMBO_REF = "plannedProgressType";

    private static final String PROGRESS_ADL_REF = "progressForDays";

    private static final String ORDER_LOOKUP_REF = "order";

    private static final String TECHNOLOGY_LOOKUP_REF = "technology";

    private static final String OPERATION_LOOKUP_REF = "productionPerShiftOperation";

    private static final String PRODUCED_PRODUCT_LOOKUP_REF = "produces";

    private static final String VIEW_IS_INITIALIZED_CHECKBOX_REF = "viewIsInitialized";

    private static final String UNIT_COMPONENT_NAME = "unit";

    private static final String WAS_CORRECTED_CHECKBOX_REF = "wasItCorrected";

    private static final String PLANNED_START_DATE_TIME_REF = "orderPlannedStartDate";

    private static final String CORRECTED_START_DATE_TIME_REF = "orderCorrectedStartDate";

    private static final String EFFECTIVE_START_DATE_TIME_REF = "orderEffectiveStartDate";

    private static final String CORRECTION_CAUSE_TYPES_ADL_REF = "plannedProgressCorrectionTypes";

    private static final String CORRECTION_COMMENT_TEXT_AREA_REF = "plannedProgressCorrectionComment";

    private static final String DAILY_PROGRESS_ADL_REF = "dailyProgress";

    private static final String SHIFT_LOOKUP_REF = "shift";

    private static final String QUANTITY_FIELD_REF = "quantity";

    @Autowired
    private ProductionPerShiftDataProvider productionPerShiftDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final Function<LookupComponent, Optional<Entity>> GET_LOOKUP_ENTITY = new Function<LookupComponent, Optional<Entity>>() {

        @Override
        public Optional<Entity> apply(final LookupComponent lookup) {
            return Optional.fromNullable(lookup.getEntity());
        }
    };

    private static final ImmutableMap<String, String> ORDER_DATE_FIELDS_TO_VIEW_COMPONENTS = ImmutableMap
            .<String, String> builder().put(OrderFields.DATE_FROM, PLANNED_START_DATE_TIME_REF)
            .put(OrderFields.CORRECTED_DATE_FROM, CORRECTED_START_DATE_TIME_REF)
            .put(OrderFields.EFFECTIVE_DATE_FROM, EFFECTIVE_START_DATE_TIME_REF).build();

    private static final ImmutableSet<OrderState> UNSUPPORTED_ORDER_STATES = ImmutableSet.of(OrderState.ABANDONED,
            OrderState.DECLINED, OrderState.COMPLETED);

    @Autowired
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Autowired
    private TechnologyOperationDataProvider technologyOperationDataProvider;

    @Autowired
    private MainTocOutputProductProvider mainTocOutputProductProvider;

    public void onBeforeRender(final ViewDefinitionState view) {
        Entity order = getEntityFromLookup(view, ORDER_LOOKUP_REF).get();
        OrderState orderState = OrderState.of(order);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        ProgressType progressType = resolveProgressType(view);
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(PROGRESS_ADL_REF);
        if (!isViewAlreadyInitialized(view)) {
            fillTechnologyLookup(view, technology);
            fillTechnologyOperationLookup(view, technology);
            fillOrderDateComponents(view, order);
            setupProgressTypeComboBox(view, orderState, progressType);
            setProductAndFillProgressForDays(view, progressForDaysADL, orderState, progressType);
        }

        disableReasonOfCorrection(view, progressType, orderState);
        disableComponents(progressForDaysADL, progressType, orderState);

        changeButtonState(view, progressType, orderState);
        setupHasBeenCorrectedCheckbox(view, technology);
        checkOrderDates(view, order);
        markViewAsInitialized(view);

    }

    private void checkOrderDates(final ViewDefinitionState view, final Entity order) {
        Long technologyId = order.getBelongsToField(OrderFields.TECHNOLOGY).getId();
        Set<Long> progressForDayIds = productionPerShiftDataProvider.findIdsOfEffectiveProgressForDay(technologyId);
        DataDefinition progressForDayDD = dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
        Optional<OrderDates> maybeOrderDates = OrderDates.of(order);
        boolean areDatesCorrect = true;
        if (maybeOrderDates.isPresent()) {
            OrderDates orderDates = maybeOrderDates.get();
            Date orderStart = removeTime(orderDates.getStart().effectiveWithFallback().toDate());
            Date orderEnd = removeTime(orderDates.getEnd().effectiveWithFallback().toDate());
            for (Long id : progressForDayIds) {
                Entity progressForDay = progressForDayDD.get(id);
                Date progressDate = progressForDay.getDateField(ProgressForDayFields.ACTUAL_DATE_OF_DAY);
                if (progressDate == null) {
                    progressDate = progressForDay.getDateField(ProgressForDayFields.DATE_OF_DAY);
                }
                if (progressDate.before(orderStart) || progressDate.after(orderEnd)) {
                    areDatesCorrect = false;
                }
            }
        }
        if (!areDatesCorrect) {
            view.addMessage("productionPerShift.info.invalidDates", MessageType.INFO, false);
        }
    }

    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Optional<Entity> getMainOutProductFor(final Optional<Entity> maybeTechnologyOperation) {
        return FluentOptional.wrap(maybeTechnologyOperation).flatMap(EntityUtils.getSafeIdExtractor())
                .flatMap(mainTocOutputProductProvider.findAsFunction()).toOpt();
    }

    public ProgressType resolveProgressType(final ViewDefinitionState view) {
        return FluentOptional.wrap(view.tryFindComponentByReference(PROGRESS_TYPE_COMBO_REF))
                .flatMap(new Function<ComponentState, Optional<ProgressType>>() {

                    @Override
                    public Optional<ProgressType> apply(final ComponentState input) {
                        String stringValue = ObjectUtils.toString(input.getFieldValue());
                        if (stringValue.isEmpty()) {
                            return Optional.absent();
                        }
                        return Optional.of(ProgressType.parseString(stringValue));
                    }
                }).or(ProgressType.PLANNED);
    }

    private boolean isViewAlreadyInitialized(final ViewDefinitionState view) {
        return view.<CheckBoxComponent> tryFindComponentByReference(VIEW_IS_INITIALIZED_CHECKBOX_REF)
                .transform(new Function<CheckBoxComponent, Boolean>() {

                    @Override
                    public Boolean apply(final CheckBoxComponent input) {
                        return input.isChecked();
                    }
                }).or(false);
    }

    private void markViewAsInitialized(final ViewDefinitionState view) {
        Optional<CheckBoxComponent> maybeCheckbox = view.tryFindComponentByReference(VIEW_IS_INITIALIZED_CHECKBOX_REF);
        for (CheckBoxComponent checkbox : maybeCheckbox.asSet()) {
            checkbox.setChecked(true);
            checkbox.requestComponentUpdateState();
        }
    }

    private Optional<Entity> getEntityFromLookup(final ViewDefinitionState view, final String referenceName) {
        Optional<LookupComponent> maybeLookupComponent = view.tryFindComponentByReference(referenceName);
        return FluentOptional.wrap(maybeLookupComponent).flatMap(GET_LOOKUP_ENTITY).toOpt();
    }

    void fillTechnologyLookup(final ViewDefinitionState view, final Entity technology) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(TECHNOLOGY_LOOKUP_REF);
        technologyLookup.setFieldValue(technology.getId());
    }

    void fillTechnologyOperationLookup(final ViewDefinitionState view, final Entity technology) {
        LookupComponent technologyOperationLookup = (LookupComponent) view.getComponentByReference(OPERATION_LOOKUP_REF);
        for (Entity rootOperation : technologyOperationDataProvider.findRoot(technology.getId()).asSet()) {
            technologyOperationLookup.setFieldValue(rootOperation.getId());
            technologyOperationLookup.requestComponentUpdateState();
        }
        technologyOperationLookup.setEnabled(true);
    }

    void setupProgressTypeComboBox(final ViewDefinitionState view, final OrderState orderState, final ProgressType progressType) {
        FieldComponent plannedProgressType = (FieldComponent) view.getComponentByReference(PROGRESS_TYPE_COMBO_REF);
        plannedProgressType.setFieldValue(progressType.getStringValue());
        plannedProgressType.requestComponentUpdateState();
        plannedProgressType.setEnabled(orderState != OrderState.PENDING);
    }

    void fillOrderDateComponents(final ViewDefinitionState view, final Entity order) {
        for (ImmutableMap.Entry<String, String> modelFieldToViewReference : ORDER_DATE_FIELDS_TO_VIEW_COMPONENTS.entrySet()) {
            FieldComponent dateComponent = (FieldComponent) view.getComponentByReference(modelFieldToViewReference.getValue());
            Date date = order.getDateField(modelFieldToViewReference.getKey());
            dateComponent.setFieldValue(DateUtils.toDateTimeString(date));
            dateComponent.requestComponentUpdateState();
        }
    }

    public void disableReasonOfCorrection(final ViewDefinitionState view) {
        Entity order = getEntityFromLookup(view, ORDER_LOOKUP_REF).get();
        OrderState orderState = OrderState.of(order);
        disableReasonOfCorrection(view, resolveProgressType(view), orderState);
    }

    void disableReasonOfCorrection(final ViewDefinitionState view, final ProgressType progressType, final OrderState orderState) {
        boolean enabled = progressType != ProgressType.PLANNED && !UNSUPPORTED_ORDER_STATES.contains(orderState);
        AwesomeDynamicListComponent plannedProgressCorrectionTypes = (AwesomeDynamicListComponent) view
                .getComponentByReference(CORRECTION_CAUSE_TYPES_ADL_REF);
        plannedProgressCorrectionTypes.setEnabled(enabled);

        for (FormComponent plannedProgressCorrectionTypeForm : plannedProgressCorrectionTypes.getFormComponents()) {
            plannedProgressCorrectionTypeForm.setFormEnabled(enabled);
        }

        view.getComponentByReference(CORRECTION_COMMENT_TEXT_AREA_REF).setEnabled(enabled);
    }

    public void setProductAndFillProgressForDays(final ViewDefinitionState view) {
        Entity order = getEntityFromLookup(view, "order").get();
        OrderState orderState = OrderState.of(order);
        ProgressType progressType = resolveProgressType(view);
        AwesomeDynamicListComponent progressForDaysADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(PROGRESS_ADL_REF);
        setProductAndFillProgressForDays(view, progressForDaysADL, orderState, progressType);
    }

    public void setProductAndFillProgressForDays(final ViewDefinitionState view,
            final AwesomeDynamicListComponent progressForDaysADL, final OrderState orderState, final ProgressType progressType) {
        Optional<Entity> maybeTechnologyOperation = getEntityFromLookup(view, OPERATION_LOOKUP_REF);
        Optional<Entity> maybeMainOperationProduct = getMainOutProductFor(maybeTechnologyOperation);
        fillOperationProductLookup(view, maybeMainOperationProduct);
        fillProgressForDays(progressForDaysADL, maybeTechnologyOperation, maybeMainOperationProduct, progressType, orderState);
        fillProgressesUnitFields(progressForDaysADL, maybeMainOperationProduct);
        disableComponents(progressForDaysADL, progressType, orderState);
    }

    private void fillOperationProductLookup(final ViewDefinitionState view, final Optional<Entity> maybeMainOperationProduct) {
        LookupComponent producesField = (LookupComponent) view.getComponentByReference(PRODUCED_PRODUCT_LOOKUP_REF);
        producesField.setFieldValue(maybeMainOperationProduct.transform(EntityUtils.getIdExtractor()).orNull());
        producesField.requestComponentUpdateState();
    }

    private void fillProgressForDays(final AwesomeDynamicListComponent progressForDaysADL,
            final Optional<Entity> maybeTechnologyOperation, final Optional<Entity> maybeMainOperationProduct,
            final ProgressType progressType, final OrderState orderState) {
        List<Entity> progresses = maybeTechnologyOperation.transform(new Function<Entity, List<Entity>>() {

            @Override
            public List<Entity> apply(final Entity technologyOperation) {
                return progressForDayDataProvider.findForOperation(technologyOperation, progressType == ProgressType.CORRECTED);
            }
        }).or(Collections.<Entity> emptyList());
        progressForDaysADL.setFieldValue(progresses);
        progressForDaysADL.requestComponentUpdateState();
    }

    private void fillProgressesUnitFields(final AwesomeDynamicListComponent progressForDaysADL,
            final Optional<Entity> maybeMainOperationProduct) {
        String unit = FluentOptional.wrap(maybeMainOperationProduct)
                .flatMap(EntityUtils.<String> getSafeFieldExtractor(ProductFields.UNIT)).toOpt().orNull();
        for (FormComponent progressForDayForm : progressForDaysADL.getFormComponents()) {
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDayForm
                    .findFieldComponentByName(DAILY_PROGRESS_ADL_REF);
            for (FormComponent dailyProgressForm : dailyProgressADL.getFormComponents()) {
                FieldComponent unitField = dailyProgressForm.findFieldComponentByName(UNIT_COMPONENT_NAME);
                unitField.setFieldValue(unit);
                unitField.requestComponentUpdateState();
            }
            dailyProgressADL.requestComponentUpdateState();
        }
        progressForDaysADL.requestComponentUpdateState();
    }

    void disableComponents(final AwesomeDynamicListComponent progressForDaysADL, final ProgressType progressType,
            final OrderState orderState) {
        boolean isEnabled = (progressType == ProgressType.CORRECTED || orderState == OrderState.PENDING)
                && !UNSUPPORTED_ORDER_STATES.contains(orderState);
        for (FormComponent progressForDaysForm : progressForDaysADL.getFormComponents()) {
            progressForDaysForm.setFormEnabled(isEnabled);
            AwesomeDynamicListComponent dailyProgressADL = (AwesomeDynamicListComponent) progressForDaysForm
                    .findFieldComponentByName(DAILY_PROGRESS_ADL_REF);
            for (FormComponent dailyProgressForm : dailyProgressADL.getFormComponents()) {
                Entity dpEntity = dailyProgressForm.getPersistedEntityWithIncludedFormValues();
                boolean isLocked = dpEntity.getBooleanField(DailyProgressFields.LOCKED);
                dailyProgressForm.setFormEnabled(isEnabled && !isLocked);
            }
            dailyProgressADL.setEnabled(isEnabled);
        }
        progressForDaysADL.setEnabled(isEnabled);
    }

    void changeButtonState(final ViewDefinitionState view, final ProgressType progressType, final OrderState orderState) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(WINDOW_REF);
        RibbonGroup progressRibbonGroup = window.getRibbon().getGroupByName(PROGRESS_RIBBON_GROUP_NAME);
        boolean isInCorrectionMode = progressType == ProgressType.CORRECTED && !UNSUPPORTED_ORDER_STATES.contains(orderState);
        ;
        for (RibbonActionItem ribbonActionItem : progressRibbonGroup.getItems()) {
            ribbonActionItem.setEnabled(isInCorrectionMode);
            ribbonActionItem.requestUpdate(true);
        }
    }

    void setupHasBeenCorrectedCheckbox(final ViewDefinitionState view, final Entity technology) {
        long numOfCorrectedOperations = technologyOperationDataProvider.count(and(
                belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology),
                eq(TechnologyOperationComponentFieldsPPS.HAS_CORRECTIONS, true)));

        CheckBoxComponent hasBeenCorrectedCheckbox = (CheckBoxComponent) view.getComponentByReference(WAS_CORRECTED_CHECKBOX_REF);
        hasBeenCorrectedCheckbox.setChecked(numOfCorrectedOperations > 0);
        hasBeenCorrectedCheckbox.requestComponentUpdateState();
    }

}
