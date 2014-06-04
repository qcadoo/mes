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
package com.qcadoo.mes.orders;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;

public class OrderServiceImplTest {

    private OrderService orderService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private TranslationService translationService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private TechnologyServiceO technologyServiceO;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private CheckBoxComponent booleanCheckBoxComponent;

    @Mock
    private FieldComponent fieldComponent;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Entity order, technology, genealogy, operationComponent, operationProductInComponent;

    @Mock
    private Iterator<Entity> iterator, iterator2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderService = new OrderServiceImpl();

        setField(orderService, "dataDefinitionService", dataDefinitionService);
        setField(orderService, "translationService", translationService);
        setField(orderService, "parameterService", parameterService);
        setField(orderService, "technologyServiceO", technologyServiceO);
    }

    @Test
    public final void shouldRecognizeIfOrderWasStarted() {
        mustNotBeStarted(null);
        mustNotBeStarted(OrderState.PENDING.getStringValue());
        mustNotBeStarted(OrderState.ACCEPTED.getStringValue());
        mustNotBeStarted(OrderState.DECLINED.getStringValue());
        mustNotBeStarted(OrderState.ABANDONED.getStringValue());

        mustBeStarted(OrderState.IN_PROGRESS.getStringValue());
        mustBeStarted(OrderState.COMPLETED.getStringValue());
        mustBeStarted(OrderState.INTERRUPTED.getStringValue());
    }

    private void mustBeStarted(final String state) {
        assertTrue(orderService.isOrderStarted(state));
    }

    private void mustNotBeStarted(final String state) {
        assertFalse(orderService.isOrderStarted(state));
    }

    @Test
    public void shouldChangeFieldStateIfCheckboxIsSelected() {
        // given
        String booleanFieldComponentName = "booleanFieldComponentName";
        String fieldComponentName = "fieldComponentName";

        given(view.getComponentByReference(booleanFieldComponentName)).willReturn(booleanCheckBoxComponent);
        given(view.getComponentByReference(fieldComponentName)).willReturn(fieldComponent);

        given(booleanCheckBoxComponent.isChecked()).willReturn(true);

        // when
        orderService.changeFieldState(view, booleanFieldComponentName, fieldComponentName);

        // then
        verify(fieldComponent).setEnabled(true);
    }

    @Test
    public void shouldntChangeFieldStateIfCheckboxIsntSelected() {
        // given
        String booleanFieldComponentName = "booleanFieldComponentName";
        String fieldComponentName = "fieldComponentName";

        given(view.getComponentByReference(booleanFieldComponentName)).willReturn(booleanCheckBoxComponent);
        given(view.getComponentByReference(fieldComponentName)).willReturn(fieldComponent);

        given(booleanCheckBoxComponent.isChecked()).willReturn(false);

        // when
        orderService.changeFieldState(view, booleanFieldComponentName, fieldComponentName);

        // then
        verify(fieldComponent).setEnabled(false);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(true);
        given(technology.getBooleanField("batchRequired")).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForPostBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(true);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForOtherBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(true);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("otherFeatureRequired")).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForOperationComponentBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(true);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("otherFeatureRequired")).willReturn(false);
        given(technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(operationComponent);
        given(operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS).iterator())
                .willReturn(iterator2);
        given(iterator2.hasNext()).willReturn(true, false);
        given(iterator2.next()).willReturn(operationProductInComponent);
        given(operationProductInComponent.getBooleanField("batchRequired")).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(false);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(true);
        given(genealogy.getField("batch")).willReturn(null);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyShiftBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(false);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(true);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("shiftFeatures").isEmpty()).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyPostBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(false);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("shiftFeatures").isEmpty()).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("postFeatures").isEmpty()).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyOtherBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").size()).willReturn(1);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("postFeatures").isEmpty()).willReturn(false);
        given(technology.getBooleanField("otherFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("otherFeatures").isEmpty()).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyComponentsBatchRequired() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").isEmpty()).willReturn(false);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("otherFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("otherFeatures").isEmpty()).willReturn(false);
        given(genealogy.getHasManyField("productInComponents").iterator()).willReturn(iterator2);
        given(iterator2.hasNext()).willReturn(true, false);
        given(iterator2.next()).willReturn(operationProductInComponent);
        given(operationProductInComponent.getBelongsToField("productInComponent").getBooleanField("batchRequired")).willReturn(
                true);
        given(operationProductInComponent.getHasManyField("batch").isEmpty()).willReturn(true);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyComponentsBatchRequired2() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").size()).willReturn(1);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("otherFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("otherFeatures").size()).willReturn(1);
        given(genealogy.getHasManyField("productInComponents").iterator()).willReturn(iterator2);
        given(iterator2.hasNext()).willReturn(true, false);
        given(iterator2.next()).willReturn(operationProductInComponent);
        given(operationProductInComponent.getBelongsToField("productInComponent").getBooleanField("batchRequired")).willReturn(
                true);
        given(operationProductInComponent.getHasManyField("batch").size()).willReturn(1);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldFailCheckingRequiredBatchForGenealogyComponentsBatchRequired3() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").size()).willReturn(1);
        given(order.getHasManyField("genealogies").iterator()).willReturn(iterator);
        given(iterator.hasNext()).willReturn(true, false);
        given(iterator.next()).willReturn(genealogy);
        given(technology.getBooleanField("batchRequired")).willReturn(false);
        given(technology.getBooleanField("shiftFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("postFeatureRequired")).willReturn(false);
        given(technology.getBooleanField("otherFeatureRequired")).willReturn(true);
        given(genealogy.getHasManyField("otherFeatures").size()).willReturn(1);
        given(genealogy.getHasManyField("productInComponents").iterator()).willReturn(iterator2);
        given(iterator2.hasNext()).willReturn(true, false);
        given(iterator2.next()).willReturn(operationProductInComponent);
        given(operationProductInComponent.getBelongsToField("productInComponent").getBooleanField("batchRequired")).willReturn(
                false);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldSuccessCheckingRequiredBatch() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getHasManyField("genealogies").size()).willReturn(1);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldSuccessCheckingRequiredBatchIfThereIsNoTechnology() throws Exception {
        // given
        given(order.getField(OrderFields.TECHNOLOGY)).willReturn(null);

        // when
        boolean result = orderService.checkRequiredBatch(order);

        // then
        assertTrue(result);
    }

}
