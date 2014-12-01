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

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubBooleanField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.PpsDetailsViewAwareTest;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.mes.technologies.tree.MainTocOutputProductProvider;
import com.qcadoo.mes.technologies.tree.dataProvider.TechnologyOperationDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductionPerShiftDetailsHooksTest extends PpsDetailsViewAwareTest {

    private static final String COPY_FROM_PLANNED_BUTTON_NAME = "copyFromPlanned";

    private static final String CLEAR_BUTTON_NAME = "clear";

    private ProductionPerShiftDetailsHooks productionPerShiftDetailsHooks;

    @Mock
    private MainTocOutputProductProvider mainTocOutputProductProvider;

    @Mock
    private TechnologyOperationDataProvider technologyOperationDataProvider;

    @Mock
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Before
    public void init() {
        super.init();
        productionPerShiftDetailsHooks = new ProductionPerShiftDetailsHooks();
        setField(productionPerShiftDetailsHooks, "mainTocOutputProductProvider", mainTocOutputProductProvider);
        setField(productionPerShiftDetailsHooks, "technologyOperationDataProvider", technologyOperationDataProvider);
        setField(productionPerShiftDetailsHooks, "progressForDayDataProvider", progressForDayDataProvider);

        stubBelongsToField(order, OrderFields.TECHNOLOGY, technology);
    }

    private void stubMainTocProduct(final Entity product) {
        given(mainTocOutputProductProvider.find(anyLong())).willReturn(Optional.fromNullable(product));
        given(mainTocOutputProductProvider.findAsFunction()).willReturn(new Function<Long, Optional<Entity>>() {

            @Override
            public Optional<Entity> apply(final Long input) {
                return Optional.fromNullable(product);
            }
        });
    }

    private void stubProgressForDayDataProviderFindForOp(final Entity technologyOperation, final boolean hasCorrections,
            final Collection<Entity> results) {
        given(progressForDayDataProvider.findForOperation(technologyOperation, hasCorrections)).willAnswer(
                new Answer<List<Entity>>() {

                    @Override
                    public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                        return Lists.newLinkedList(results);
                    }
                });
    }

    private void stubOrderState(final OrderState orderState) {
        stubStringField(order, OrderFields.STATE, Optional.fromNullable(orderState).transform(new Function<OrderState, String>() {

            @Override
            public String apply(final OrderState input) {
                return input.getStringValue();
            }
        }).orNull());
    }

    @Test
    public void shouldAddRootForOperation() throws Exception {
        // given
        final Long rootOperationId = 2L;
        LookupComponent technologyOperationLookup = mockLookup(mockEntity());
        stubViewComponent(OPERATION_LOOKUP_REF, technologyOperationLookup);
        Entity rootOperation = mockEntity(rootOperationId);
        given(technologyOperationDataProvider.findRoot(anyLong())).willReturn(Optional.of(rootOperation));

        // when
        productionPerShiftDetailsHooks.fillTechnologyOperationLookup(view, technology);

        // then
        verify(technologyOperationLookup).setFieldValue(rootOperationId);
    }

    @Test
    public void shouldEnableProgressType() throws Exception {
        ProgressType progressType = ProgressType.PLANNED;
        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.PENDING);
        Set<OrderState> allStates = ImmutableSet.copyOf(OrderState.values());
        for (OrderState orderState : Sets.difference(allStates, unsupportedStates)) {
            performPlannedProgressDisablingTest(progressType, orderState, true);
        }
    }

    @Test
    public void shouldDisableProgressType() throws Exception {
        ProgressType progressType = ProgressType.PLANNED;
        performPlannedProgressDisablingTest(progressType, OrderState.PENDING, false);
    }

    private void performPlannedProgressDisablingTest(final ProgressType progressType, final OrderState orderState,
            final boolean expectEnabled) {
        // given
        FieldComponent progressTypeComboBox = mock(FieldComponent.class);
        stubViewComponent(PROGRESS_TYPE_COMBO_REF, progressTypeComboBox);

        // when
        productionPerShiftDetailsHooks.setupProgressTypeComboBox(view, orderState, progressType);

        // then
        verify(progressTypeComboBox).setFieldValue(progressType.getStringValue());
        verify(progressTypeComboBox).setEnabled(expectEnabled);
    }

    @Test
    public void shouldEnabledPlannedProgressTypeForInProgressOrder() throws Exception {
        // given
        ProgressType progressType = ProgressType.PLANNED;

        // when
        productionPerShiftDetailsHooks.setupProgressTypeComboBox(view, OrderState.IN_PROGRESS, progressType);

        // then
        verify(progressTypeComboBox).setFieldValue(progressType.getStringValue());
        verify(progressTypeComboBox).setEnabled(true);
    }

    @Test
    public void shouldSetOrderStartDatesWhenPlannedDateExists() throws Exception {
        // given
        ComponentState plannedDateField = mockFieldComponent(null);
        ComponentState correctedDateField = mockFieldComponent(null);
        ComponentState effectiveDateField = mockFieldComponent(null);

        stubViewComponent(PLANNED_START_DATE_TIME_REF, plannedDateField);
        stubViewComponent(CORRECTED_START_DATE_TIME_REF, correctedDateField);
        stubViewComponent(EFFECTIVE_START_DATE_TIME_REF, effectiveDateField);

        Date planned = new Date();

        stubDateField(order, OrderFields.DATE_FROM, planned);
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, null);
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, null);

        // when
        productionPerShiftDetailsHooks.fillOrderDateComponents(view, order);

        // then
        verify(plannedDateField).setFieldValue(DateUtils.toDateTimeString(planned));
        verify(correctedDateField).setFieldValue("");
        verify(effectiveDateField).setFieldValue("");
    }

    @Test
    public void shouldSetOrderStartDatesWhenPlannedAndCorrectedDateExists() throws Exception {
        // given
        ComponentState plannedDateField = mockFieldComponent(null);
        ComponentState correctedDateField = mockFieldComponent(null);
        ComponentState effectiveDateField = mockFieldComponent(null);

        stubViewComponent(PLANNED_START_DATE_TIME_REF, plannedDateField);
        stubViewComponent(CORRECTED_START_DATE_TIME_REF, correctedDateField);
        stubViewComponent(EFFECTIVE_START_DATE_TIME_REF, effectiveDateField);

        Date planned = new Date();
        Date corrected = new Date();
        Date effective = new Date();

        stubDateField(order, OrderFields.DATE_FROM, planned);
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, corrected);
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effective);

        // when
        productionPerShiftDetailsHooks.fillOrderDateComponents(view, order);

        // then
        verify(plannedDateField).setFieldValue(DateUtils.toDateTimeString(planned));
        verify(correctedDateField).setFieldValue(DateUtils.toDateTimeString(corrected));
        verify(effectiveDateField).setFieldValue(DateUtils.toDateTimeString(effective));
    }

    @Test
    public void shouldDisableReasonOfCorrections() throws Exception {
        stubProgressType(ProgressType.PLANNED);

        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.ABANDONED, OrderState.DECLINED, OrderState.COMPLETED);
        for (OrderState orderState : unsupportedStates) {
            stubOrderState(orderState);
            performReasonOfCorrectionDisablingTest(false);
        }
    }

    @Test
    public void shouldEnableReasonOfCorrections() throws Exception {
        stubProgressType(ProgressType.CORRECTED);

        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.ABANDONED, OrderState.DECLINED, OrderState.COMPLETED);
        Set<OrderState> allStates = ImmutableSet.copyOf(OrderState.values());
        for (OrderState orderState : Sets.difference(allStates, unsupportedStates)) {
            stubOrderState(orderState);
            performReasonOfCorrectionDisablingTest(true);
        }
    }

    private void performReasonOfCorrectionDisablingTest(final boolean expectFieldsToBeEnabled) {
        AwesomeDynamicListComponent correctionReasonTypes = mock(AwesomeDynamicListComponent.class);
        stubViewComponent(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES, correctionReasonTypes);

        FieldComponent correctionCommentComponent = mockFieldComponent(null);
        stubViewComponent(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT, correctionCommentComponent);

        // when
        productionPerShiftDetailsHooks.disableReasonOfCorrection(view);

        // then
        verify(correctionReasonTypes).setEnabled(expectFieldsToBeEnabled);
        verify(correctionCommentComponent).setEnabled(expectFieldsToBeEnabled);
    }

    @Test
    public void shouldFillProgressesADL() throws Exception {
        // given
        Entity technologyOperation = mockEntity(3L);
        stubViewComponent(OPERATION_LOOKUP_REF, mockLookup(technologyOperation));

        LookupComponent producedProductLookup = mockLookup(null);
        stubViewComponent(PRODUCED_PRODUCT_LOOKUP_REF, producedProductLookup);

        long productId = 100L;
        Entity product = mockEntity(productId);
        stubMainTocProduct(product);
        stubProgressType(ProgressType.CORRECTED);
        stubOrderState(OrderState.PENDING);

        Entity firstPfd = mockEntity();
        Entity secondPfd = mockEntity();
        stubProgressForDayDataProviderFindForOp(technologyOperation, true, Lists.newArrayList(firstPfd, secondPfd));

        String productUnit = "someArbitraryUnit";
        stubStringField(product, ProductFields.UNIT, productUnit);

        FieldComponent firstDayField = mockFieldComponent(null);
        LookupComponent firstShiftLookup = mockLookup(mockEntity());
        FieldComponent firstQuantityFieldComponent = mockFieldComponent(null);
        FieldComponent firstUnitFieldComponent = mockFieldComponent(null);
        FormComponent firstPfdForm = mockProgressForDayRowForm(firstDayField, firstShiftLookup, firstQuantityFieldComponent,
                firstUnitFieldComponent);

        FieldComponent secondDayField = mockFieldComponent(null);
        LookupComponent secondShiftLookup = mockLookup(mockEntity());
        FieldComponent secondQuantityFieldComponent = mockFieldComponent(null);
        FieldComponent secondUnitFieldComponent = mockFieldComponent(null);
        FormComponent secondPfdForm = mockProgressForDayRowForm(secondDayField, secondShiftLookup, secondQuantityFieldComponent,
                secondUnitFieldComponent);

        AwesomeDynamicListComponent progressesAdl = mock(AwesomeDynamicListComponent.class);
        stubViewComponent("progressForDays", progressesAdl);
        given(progressesAdl.getFormComponents()).willReturn(Lists.newArrayList(firstPfdForm, secondPfdForm));

        // when
        productionPerShiftDetailsHooks.setProductAndFillProgressForDays(view);

        // then
        verify(progressesAdl).setFieldValue(Lists.newArrayList(firstPfd, secondPfd));
        verify(producedProductLookup).setFieldValue(productId);
        // verify(firstShiftLookup).setEnabled(true);
        // verify(firstQuantityFieldComponent).setEnabled(true);
        verify(firstUnitFieldComponent).setFieldValue(productUnit);
        // verify(secondShiftLookup).setEnabled(true);
        // verify(secondQuantityFieldComponent).setEnabled(true);
        verify(secondUnitFieldComponent).setFieldValue(productUnit);
    }

    private FormComponent mockProgressForDayRowForm(final FieldComponent dayField, final LookupComponent shiftLookup,
            final FieldComponent quantityField, final FieldComponent unitField) {
        FormComponent progressForDayForm = mockForm(mockEntity());

        AwesomeDynamicListComponent dailyProgressesAdl = mock(AwesomeDynamicListComponent.class);
        FormComponent dailyProgressForm = mockForm(mockEntity());
        stubFormComponent(dailyProgressForm, "unit", unitField);
        stubFormComponent(dailyProgressForm, "quantity", quantityField);
        stubFormComponent(dailyProgressForm, "shift", shiftLookup);
        given(dailyProgressesAdl.getFormComponents()).willReturn(Lists.newArrayList(dailyProgressForm));

        stubFormComponent(progressForDayForm, "dailyProgress", dailyProgressesAdl);
        stubFormComponent(progressForDayForm, "day", dayField);
        return progressForDayForm;
    }

    @Test
    public final void shouldEnableComponents() {
        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.ABANDONED, OrderState.DECLINED, OrderState.COMPLETED);
        Set<OrderState> allStates = ImmutableSet.copyOf(OrderState.values());
        for (OrderState orderState : Sets.difference(allStates, unsupportedStates)) {
            performComponentDisablingTest(ProgressType.CORRECTED, orderState, true);
        }
        performComponentDisablingTest(ProgressType.PLANNED, OrderState.PENDING, true);
    }

    @Test
    public final void shouldDisableComponents() {
        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.ABANDONED, OrderState.DECLINED, OrderState.COMPLETED);
        for (OrderState orderState : unsupportedStates) {
            performComponentDisablingTest(ProgressType.CORRECTED, orderState, false);
            if (orderState != OrderState.PENDING) {
                performComponentDisablingTest(ProgressType.PLANNED, orderState, false);
            }
        }
    }

    private void performComponentDisablingTest(final ProgressType progressType, final OrderState orderState,
            final boolean expectToBeEnabled) {
        // given
        FormComponent firstPfdFirstDailyForm = mockForm(mockEntity());
        FormComponent firstPfdSecondDailyForm = mockForm(mockEntity());
        AwesomeDynamicListComponent firstPfdDailyAdl = mock(AwesomeDynamicListComponent.class);
        given(firstPfdDailyAdl.getFormComponents()).willReturn(
                Lists.newArrayList(firstPfdFirstDailyForm, firstPfdSecondDailyForm));

        FormComponent firstPfdForm = mockForm(mockEntity());
        stubFormComponent(firstPfdForm, DAILY_PROGRESS_ADL_REF, firstPfdDailyAdl);

        FormComponent secondPfdFirstDailyForm = mockForm(mockEntity());
        AwesomeDynamicListComponent secondPfdDailyAdl = mock(AwesomeDynamicListComponent.class);
        given(secondPfdDailyAdl.getFormComponents()).willReturn(Lists.newArrayList(secondPfdFirstDailyForm));

        FormComponent secondPfdForm = mockForm(mockEntity());
        stubFormComponent(secondPfdForm, DAILY_PROGRESS_ADL_REF, secondPfdDailyAdl);

        AwesomeDynamicListComponent progressForDaysAdl = mock(AwesomeDynamicListComponent.class);
        stubViewComponent("progressForDays", progressForDaysAdl);
        given(progressForDaysAdl.getFormComponents()).willReturn(Lists.newArrayList(firstPfdForm, secondPfdForm));

        // when
        productionPerShiftDetailsHooks.disableComponents(progressForDaysAdl, progressType, orderState);

        // then
        verify(progressForDaysAdl).setEnabled(expectToBeEnabled);

        verify(firstPfdFirstDailyForm).setFormEnabled(expectToBeEnabled);
        verify(firstPfdSecondDailyForm).setFormEnabled(expectToBeEnabled);
        verify(firstPfdDailyAdl).setEnabled(expectToBeEnabled);
        verify(firstPfdForm).setFormEnabled(expectToBeEnabled);

        verify(secondPfdFirstDailyForm).setFormEnabled(expectToBeEnabled);
        verify(secondPfdDailyAdl).setEnabled(expectToBeEnabled);
        verify(secondPfdForm).setFormEnabled(expectToBeEnabled);
    }

    @Test
    public void shouldDisableDailyProgressRowsForLockedEntities() {
        // given
        FormComponent firstPfdFirstDailyForm = mockForm(mockEntity());

        FormComponent firstPfdSecondDailyForm = mockForm(mockLockedEntity());

        AwesomeDynamicListComponent firstPfdDailyAdl = mock(AwesomeDynamicListComponent.class);
        given(firstPfdDailyAdl.getFormComponents()).willReturn(
                Lists.newArrayList(firstPfdFirstDailyForm, firstPfdSecondDailyForm));

        FormComponent firstPfdForm = mockForm(mockEntity());
        stubFormComponent(firstPfdForm, DAILY_PROGRESS_ADL_REF, firstPfdDailyAdl);

        FormComponent secondPfdFirstDailyForm = mockForm(mockEntity());
        AwesomeDynamicListComponent secondPfdDailyAdl = mock(AwesomeDynamicListComponent.class);
        given(secondPfdDailyAdl.getFormComponents()).willReturn(Lists.newArrayList(secondPfdFirstDailyForm));

        FormComponent secondPfdForm = mockForm(mockEntity());
        stubFormComponent(secondPfdForm, DAILY_PROGRESS_ADL_REF, secondPfdDailyAdl);

        AwesomeDynamicListComponent progressForDaysAdl = mock(AwesomeDynamicListComponent.class);
        stubViewComponent("progressForDays", progressForDaysAdl);
        given(progressForDaysAdl.getFormComponents()).willReturn(Lists.newArrayList(firstPfdForm, secondPfdForm));

        // when
        productionPerShiftDetailsHooks.disableComponents(progressForDaysAdl, ProgressType.PLANNED, OrderState.PENDING);

        // then
        verify(progressForDaysAdl).setEnabled(true);

        verify(firstPfdFirstDailyForm).setFormEnabled(true);
        verify(firstPfdSecondDailyForm).setFormEnabled(false);
        verify(firstPfdDailyAdl).setEnabled(true);
        verify(firstPfdForm).setFormEnabled(true);

        verify(secondPfdFirstDailyForm).setFormEnabled(true);
        verify(secondPfdDailyAdl).setEnabled(true);
        verify(secondPfdForm).setFormEnabled(true);
    }

    private Entity mockLockedEntity() {
        Entity entity = mockEntity();
        stubBooleanField(entity, DailyProgressFields.LOCKED, true);
        return entity;
    }

    @Test
    public void shouldDisabledRibbonButtons() throws Exception {
        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.ABANDONED, OrderState.DECLINED, OrderState.COMPLETED);
        for (OrderState orderState : unsupportedStates) {
            performRibbonDisablingTest(ProgressType.PLANNED, orderState, false);
        }
    }

    @Test
    public void shouldEnabledRibbonButtons() throws Exception {
        Set<OrderState> unsupportedStates = ImmutableSet.of(OrderState.ABANDONED, OrderState.DECLINED, OrderState.COMPLETED);
        Set<OrderState> allStates = ImmutableSet.copyOf(OrderState.values());
        for (OrderState orderState : Sets.difference(allStates, unsupportedStates)) {
            performRibbonDisablingTest(ProgressType.CORRECTED, orderState, true);
        }
    }

    private void performRibbonDisablingTest(final ProgressType progressType, final OrderState orderState,
            final boolean expectEnabledButtons) {
        // given
        WindowComponentState window = mock(WindowComponentState.class);
        stubViewComponent(WINDOW_REF, window);
        Ribbon ribbon = mock(Ribbon.class);
        given(window.getRibbon()).willReturn(ribbon);
        RibbonGroup progressRibbonGroup = mock(RibbonGroup.class);
        given(ribbon.getGroupByName(PROGRESS_RIBBON_GROUP_NAME)).willReturn(progressRibbonGroup);
        RibbonActionItem clear = mock(RibbonActionItem.class);
        RibbonActionItem copyFromPlanned = mock(RibbonActionItem.class);
        given(progressRibbonGroup.getItems()).willReturn(Lists.newArrayList(clear, copyFromPlanned));
        given(progressRibbonGroup.getItemByName(CLEAR_BUTTON_NAME)).willReturn(clear);
        given(progressRibbonGroup.getItemByName(COPY_FROM_PLANNED_BUTTON_NAME)).willReturn(copyFromPlanned);

        // when
        productionPerShiftDetailsHooks.changeButtonState(view, progressType, orderState);

        // then
        verify(clear).setEnabled(expectEnabledButtons);
        verify(copyFromPlanned).setEnabled(expectEnabledButtons);
    }

}
