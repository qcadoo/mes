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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
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
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductionPerShiftDetailsHooksTest {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_PROGRESS = "progress";

    private static final String L_COPY_FROM_PLANNED = "copyFromPlanned";

    private static final String L_CLEAR = "clear";

    private static final String L_PRODUCTION_PER_SHIFT_OPERATION = "productionPerShiftOperation";

    private static final String L_PRODUCES = "produces";

    private static final String L_SET_ROOT = "setRoot";

    private static final String L_UNIT = "unit";

    private static final String L_ORDER_CORRECTED_START_DATE = "orderCorrectedStartDate";

    private static final String L_ORDER_PLANNED_START_DATE = "orderPlannedStartDate";

    private static final String L_ORDER_EFFECTIVE_START_DATE = "orderEffectiveStartDate";

    private ProductionPerShiftDetailsHooks productionPerShiftDetailsHooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private TechnologyService technologyService;

    @Mock
    private ShiftsService shiftsService;

    @Mock
    private PPSHelper ppsHelper;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form, form1;

    @Mock
    private FieldComponent operationField, plannedProgressTypeField, plannedDateField, corretedDateField, effectiveDateField,
            plannedProgressCorrectionCommentField, unitField, setRootField;

    @Mock
    private LookupComponent lookupComponent, producesLookup;

    @Mock
    private DataDefinition orderDD, technologyDD, technologyOperationComponentDD;

    @Mock
    private Entity order, technology, technologyOperationComponent, operationProductOutComponent, product, shift;

    @Mock
    private EntityTreeNode root;

    @Mock
    private EntityTree technologyOperationComponents;

    @Mock
    private EntityList progressForDays;

    @Mock
    private SearchResult searchResult;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private List<FormComponent> forms, forms1;

    @Mock
    private Iterator<FormComponent> iteratorForm, iteratorForm1;

    @Mock
    private AwesomeDynamicListComponent adl, dailyProgress, plannedProgressCorrectionTypes;

    private Long orderId;

    @Before
    public void init() {
        productionPerShiftDetailsHooks = new ProductionPerShiftDetailsHooks();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(productionPerShiftDetailsHooks, "shiftsService", shiftsService);
        ReflectionTestUtils.setField(productionPerShiftDetailsHooks, "technologyService", technologyService);
        ReflectionTestUtils.setField(productionPerShiftDetailsHooks, "ppsHelper", ppsHelper);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)).willReturn(technologyOperationComponentDD);
        given(view.getComponentByReference(ProductionPerShiftFields.ORDER)).willReturn(lookupComponent);
        given(lookupComponent.getEntity()).willReturn(order);
        given(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY))
                .willReturn(technologyDD);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
    }

    @Test
    public void shouldFillProducesFieldAfterSelection() {
        // given
        String prodName = "asdf";
        String unit = "PLN";

        given(view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).willReturn(lookupComponent);
        given(lookupComponent.getEntity()).willReturn(technologyOperationComponent);
        given(view.getComponentByReference(L_PRODUCES)).willReturn(producesLookup);
        given(producesLookup.getEntity()).willReturn(null);

        given(technologyService.getMainOutputProductComponent(technologyOperationComponent)).willReturn(
                operationProductOutComponent);
        given(operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT)).willReturn(product);
        given(product.getStringField(ProductFields.NAME)).willReturn(prodName);
        given(view.getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(adl);
        given(adl.getFormComponents()).willReturn(forms);
        given(forms.iterator()).willReturn(iteratorForm);
        given(iteratorForm.next()).willReturn(form);
        given(iteratorForm.hasNext()).willReturn(true, false);

        given(form.findFieldComponentByName(ProgressForDayFields.DAILY_PROGRESS)).willReturn(dailyProgress);
        given(dailyProgress.getFormComponents()).willReturn(forms1);
        given(forms1.iterator()).willReturn(iteratorForm1);
        given(iteratorForm1.next()).willReturn(form1);
        given(iteratorForm1.hasNext()).willReturn(true, false);

        given(product.getStringField(ProductFields.UNIT)).willReturn(unit);
        given(form1.findFieldComponentByName(L_UNIT)).willReturn(unitField);

        // when
        productionPerShiftDetailsHooks.fillProducedField(view);

        // then
        Mockito.verify(unitField).setFieldValue(unit);
    }

    @Ignore
    @Test
    public void shouldAddRootForOperation() throws Exception {
        // given
        Long entityId = 1L;
        Long operationId = 2L;
        Long rootId = 2L;

        given(view.getComponentByReference(L_SET_ROOT)).willReturn(setRootField);
        given(setRootField.getFieldValue()).willReturn("");
        given(view.getComponentByReference(L_FORM)).willReturn(form);
        given(form.getEntityId()).willReturn(entityId);

        given(order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS)).willReturn(
                technologyOperationComponents);
        given(technologyOperationComponents.isEmpty()).willReturn(false);
        given(technologyOperationComponents.getRoot()).willReturn(root);
        given(view.getComponentByReference(L_PRODUCTION_PER_SHIFT_OPERATION)).willReturn(operationField);
        given(operationField.getFieldValue()).willReturn(null);
        given(view.getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(adl);
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(PlannedProgressType.PLANNED.getStringValue());
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderState.PENDING.getStringValue());
        given(operationField.getFieldValue()).willReturn(null);
        given(root.getId()).willReturn(rootId);

        // when
        productionPerShiftDetailsHooks.addRootForOperation(view);

        // then
        Assert.assertEquals(rootId, operationId);
    }

    @Test
    public void shouldDisabledPlannedProgressTypeForPendingOrder() throws Exception {
        // given
        String empty = "";
        given(orderDD.get(orderId)).willReturn(order);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderState.PENDING.getStringValue());
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(empty);

        // when
        productionPerShiftDetailsHooks.disablePlannedProgressTypeForPendingOrder(view);

        // then
        verify(plannedProgressTypeField).setFieldValue(PlannedProgressType.PLANNED.getStringValue());
        verify(plannedProgressTypeField).setEnabled(false);
    }

    @Test
    public void shouldEnabledPlannedProgressTypeForInProgressOrder() throws Exception {
        // given
        String empty = "";
        given(view.getComponentByReference(ProductionPerShiftFields.ORDER)).willReturn(lookupComponent);
        given(lookupComponent.getEntity()).willReturn(order);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderState.IN_PROGRESS.getStringValue());
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(empty);

        // when
        productionPerShiftDetailsHooks.disablePlannedProgressTypeForPendingOrder(view);

        // then
        verify(plannedProgressTypeField).setEnabled(true);
    }

    @Test
    public void shouldSetOrderStartDatesWhenPlannedDateExists() throws Exception {
        // given
        given(orderDD.get(orderId)).willReturn(order);
        given(view.getComponentByReference(L_ORDER_PLANNED_START_DATE)).willReturn(plannedDateField);
        given(view.getComponentByReference(L_ORDER_CORRECTED_START_DATE)).willReturn(corretedDateField);
        given(view.getComponentByReference(L_ORDER_EFFECTIVE_START_DATE)).willReturn(effectiveDateField);

        Date planned = new Date();

        given(order.getField(OrderFields.DATE_FROM)).willReturn(planned);
        given(order.getField(OrderFields.CORRECTED_DATE_FROM)).willReturn(null);
        given(order.getField(OrderFields.EFFECTIVE_DATE_FROM)).willReturn(null);

        // when
        productionPerShiftDetailsHooks.setOrderStartDate(view);

        // then
        verify(plannedDateField).setFieldValue(Mockito.any(Date.class));
    }

    @Test
    public void shouldSetOrderStartDatesWhenPlannedAndCorrectedDateExists() throws Exception {
        // given
        given(orderDD.get(orderId)).willReturn(order);
        given(view.getComponentByReference(L_ORDER_PLANNED_START_DATE)).willReturn(plannedDateField);
        given(view.getComponentByReference(L_ORDER_CORRECTED_START_DATE)).willReturn(corretedDateField);
        given(view.getComponentByReference(L_ORDER_EFFECTIVE_START_DATE)).willReturn(effectiveDateField);

        Date planned = new Date();
        Date corrected = new Date();
        Date effective = new Date();

        given(order.getField(OrderFields.DATE_FROM)).willReturn(planned);
        given(order.getField(OrderFields.CORRECTED_DATE_FROM)).willReturn(corrected);
        given(order.getField(OrderFields.EFFECTIVE_DATE_FROM)).willReturn(effective);

        // when
        productionPerShiftDetailsHooks.setOrderStartDate(view);

        // then
        verify(plannedDateField).setFieldValue(Mockito.any(Date.class));
        verify(corretedDateField).setFieldValue(Mockito.any(Date.class));
        verify(effectiveDateField).setFieldValue(Mockito.any(Date.class));
    }

    @Test
    public void shouldDisableReasonOfCorrectionsFieldForPlannedProgressType() throws Exception {
        // given
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES)).willReturn(
                plannedProgressCorrectionTypes);
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT)).willReturn(
                plannedProgressCorrectionCommentField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(PlannedProgressType.PLANNED.getStringValue());

        // when
        productionPerShiftDetailsHooks.disableReasonOfCorrection(view);

        // then
        verify(plannedProgressCorrectionTypes).setEnabled(false);
        verify(plannedProgressCorrectionCommentField).setEnabled(false);
    }

    @Test
    public void shouldEnableReasonOfCorrectionsFieldForCorrectedProgressType() throws Exception {
        // given
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_TYPES)).willReturn(
                plannedProgressCorrectionTypes);
        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_CORRECTION_COMMENT)).willReturn(
                plannedProgressCorrectionCommentField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(PlannedProgressType.CORRECTED.getStringValue());

        // when
        productionPerShiftDetailsHooks.disableReasonOfCorrection(view);

        // then
        verify(plannedProgressCorrectionTypes).setEnabled(true);
        verify(plannedProgressCorrectionCommentField).setEnabled(true);
    }

    @Test
    @Ignore
    public void shouldFillProgressForDaysSelectedOperation() throws Exception {
        // given
        given(view.getComponentByReference(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(adl);

        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(
                progressForDays);
        given(plannedProgressTypeField.getFieldValue()).willReturn(PlannedProgressType.CORRECTED.getStringValue());

        SearchCriterion searchCriterion = SearchRestrictions.eq(ProgressForDayFields.CORRECTED, true);

        given(progressForDays.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(searchCriterion)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(progressForDays);

        // when
        productionPerShiftDetailsHooks.fillProgressForDays(view);

        // then
        Mockito.verify(adl).setFieldValue(progressForDays);
    }

    @Test
    public void shouldDisabledButtonWhenProgressTypeIsPlanne() throws Exception {
        // given
        WindowComponentState window = mock(WindowComponentState.class);
        Ribbon ribbon = mock(Ribbon.class);
        RibbonGroup progressSelectedOperation = mock(RibbonGroup.class);
        RibbonActionItem clear = mock(RibbonActionItem.class);
        RibbonActionItem copyFromPlanned = mock(RibbonActionItem.class);

        given(view.getComponentByReference(L_WINDOW)).willReturn(window);
        given(window.getRibbon()).willReturn(ribbon);
        given(ribbon.getGroupByName(L_PROGRESS)).willReturn(progressSelectedOperation);
        given(progressSelectedOperation.getItemByName(L_CLEAR)).willReturn(clear);
        given(progressSelectedOperation.getItemByName(L_COPY_FROM_PLANNED)).willReturn(copyFromPlanned);

        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(PlannedProgressType.PLANNED.getStringValue());

        // when
        productionPerShiftDetailsHooks.changeButtonState(view);

        // then
        Mockito.verify(clear).setEnabled(false);
        Mockito.verify(copyFromPlanned).setEnabled(false);
    }

    @Test
    public void shouldEnabledButtonWhenProgressTypeIsCorrected() throws Exception {
        // given
        WindowComponentState windowComponent = mock(WindowComponentState.class);
        Ribbon ribbon = mock(Ribbon.class);
        RibbonGroup progressSelectedOperation = mock(RibbonGroup.class);
        RibbonActionItem clear = mock(RibbonActionItem.class);
        RibbonActionItem copyFromPlanned = mock(RibbonActionItem.class);

        given(view.getComponentByReference(L_WINDOW)).willReturn(windowComponent);
        given(windowComponent.getRibbon()).willReturn(ribbon);
        given(ribbon.getGroupByName(L_PROGRESS)).willReturn(progressSelectedOperation);
        given(progressSelectedOperation.getItemByName(L_CLEAR)).willReturn(clear);
        given(progressSelectedOperation.getItemByName(L_COPY_FROM_PLANNED)).willReturn(copyFromPlanned);

        given(view.getComponentByReference(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE)).willReturn(plannedProgressTypeField);
        given(plannedProgressTypeField.getFieldValue()).willReturn(PlannedProgressType.CORRECTED.getStringValue());

        // when
        productionPerShiftDetailsHooks.changeButtonState(view);

        // then
        Mockito.verify(clear).setEnabled(true);
        Mockito.verify(copyFromPlanned).setEnabled(true);
    }

    @Test
    public void returnFalseWhenFirstProgressDoesnotWorkAtDateTime() throws Exception {
        // given
        Entity productionPerShift = mock(Entity.class);
        Entity progressForDay = mock(Entity.class);
        Integer day = Integer.valueOf(1);
        EntityList progressForDays = mockEntityList(Lists.newArrayList(progressForDay));
        Entity dailyProgress = mock(Entity.class);
        EntityList dailyProgressList = mockEntityList(Lists.newArrayList(dailyProgress));
        Date correctedDate = new Date();

        given(view.getComponentByReference(L_FORM)).willReturn(form);
        given(form.getEntity()).willReturn(productionPerShift);

        given(order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS)).willReturn(
                technologyOperationComponents);

        given(technologyOperationComponents.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return Lists.newArrayList(technologyOperationComponent).iterator();
            }
        });

        given(technologyOperationComponent.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)).willReturn(
                progressForDays);

        given(progressForDays.get(0)).willReturn(progressForDay);
        given(progressForDay.getField(ProgressForDayFields.DAY)).willReturn(day);
        given(progressForDay.getBooleanField(ProgressForDayFields.CORRECTED)).willReturn(false);

        given(progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS)).willReturn(dailyProgressList);
        given(dailyProgressList.get(0)).willReturn(dailyProgress);
        given(dailyProgress.getBelongsToField(DailyProgressFields.SHIFT)).willReturn(shift);

        given(productionPerShift.getBelongsToField(ProductionPerShiftFields.ORDER)).willReturn(order);
        given(order.getDateField(OrderFields.START_DATE)).willReturn(correctedDate);

        given(ppsHelper.getDateAfterStartOrderForProgress(order, progressForDay)).willReturn(correctedDate);

        // when
        productionPerShiftDetailsHooks.checkShiftsIfWorks(view);

        // then
        // Assert.assertFalse(result);
    }

    public EntityList mockEntityList(final List<Entity> entities) {
        final EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });
        given(entityList.isEmpty()).willReturn(entities.isEmpty());
        return entityList;
    }

}
