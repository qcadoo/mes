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
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import static com.qcadoo.mes.techSubcontracting.constants.TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.COMMENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class OperationalTaskDetailsListenersOTFOOverrideUtilTest {

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private OperationalTaskDetailsListenersOTFOOverrideUtil operationalTaskDetailsListenersOTFOOverrideUtil;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private LookupComponent orderLookup, technologyOperationComponentLookup, productionLineLookup;

    @Mock
    private FieldComponent nameField, descriptionField;

    @Mock
    private Entity order, technologyOperationComponent, productionLine, operation;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationalTaskDetailsListenersOTFOOverrideUtil = new OperationalTaskDetailsListenersOTFOOverrideUtil();

        given(view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER)).willReturn(orderLookup);
        given(view.getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT)).willReturn(technologyOperationComponentLookup);
        given(view.getComponentByReference(OperationalTaskFields.PRODUCTION_LINE)).willReturn(productionLineLookup);
        given(view.getComponentByReference(OperationalTaskFields.NAME)).willReturn(nameField);
        given(view.getComponentByReference(OperationalTaskFields.DESCRIPTION)).willReturn(descriptionField);
    }

    @Test
    public void shouldClearOperationFieldWhenOrderIsChanged() throws Exception {
        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(view);

        // then
        Mockito.verify(technologyOperationComponentLookup).setFieldValue(null);
    }

    @Test
    public void shouldClearFieldWhenOrderIsNull() throws Exception {
        // given
        given(orderLookup.getEntity()).willReturn(null);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(view);

        // then
        Mockito.verify(technologyOperationComponentLookup).setFieldValue(null);
    }

    @Test
    public void shouldReturnWhenTechInstOperCompIsNull() throws Exception {
        // given
        Long productionLineId = 1L;

        given(orderLookup.getEntity()).willReturn(order);
        given(technologyOperationComponentLookup.getEntity()).willReturn(null);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);
        given(productionLine.getId()).willReturn(productionLineId);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(view);

        // then
        Mockito.verify(technologyOperationComponentLookup).setFieldValue(null);
    }

    @Test
    public void shouldSetNullInProductionLineLookupWhenOperationIsNotSubcontrAndOrderProductionLineIsNull() throws Exception {
        // given
        String name = "name";

        given(orderLookup.getEntity()).willReturn(order);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(null);
        given(technologyOperationComponentLookup.getEntity()).willReturn(technologyOperationComponent);
        given(technologyOperationComponent.getBooleanField(IS_SUBCONTRACTING)).willReturn(true);
        given(technologyOperationComponent.getBelongsToField(OPERATION)).willReturn(operation);
        given(operation.getStringField(OperationFields.NAME)).willReturn(name);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(view);

        // then
        Mockito.verify(technologyOperationComponentLookup).setFieldValue(null);
        Mockito.verify(productionLineLookup).setFieldValue(null);
    }

    @Test
    public void shouldSetProductionLineInLookupWhenOperationIsNotSubcontr() throws Exception {
        // given
        Long productionLineId = 1L;

        given(orderLookup.getEntity()).willReturn(order);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);
        given(technologyOperationComponentLookup.getEntity()).willReturn(technologyOperationComponent);
        given(technologyOperationComponent.getBooleanField(IS_SUBCONTRACTING)).willReturn(false);
        given(productionLine.getId()).willReturn(productionLineId);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(view);

        // then
        Mockito.verify(technologyOperationComponentLookup).setFieldValue(null);
        Mockito.verify(productionLineLookup).setFieldValue(productionLineId);
    }

    @Test
    public void shouldSetOperationCommentAndName() throws Exception {
        // given
        String comment = "Comment";
        String name = "name";

        given(technologyOperationComponentLookup.getEntity()).willReturn(technologyOperationComponent);
        given(technologyOperationComponent.getBooleanField(IS_SUBCONTRACTING)).willReturn(true);
        given(orderLookup.getEntity()).willReturn(order);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(null);
        given(technologyOperationComponent.getStringField(COMMENT)).willReturn(comment);
        given(technologyOperationComponent.getBelongsToField(OPERATION)).willReturn(operation);
        given(operation.getStringField(OperationFields.NAME)).willReturn(name);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.setOperationalNameAndDescriptionForSubcontractedOperation(view);

        // then
        Mockito.verify(descriptionField).setFieldValue(comment);
        Mockito.verify(nameField).setFieldValue(name);
    }

    @Test
    public void shouldSetNullToFieldWhenTechInstOpCompIsNull() throws Exception {
        // given
        given(technologyOperationComponentLookup.getEntity()).willReturn(null);
        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.setOperationalNameAndDescriptionForSubcontractedOperation(view);

        // then
        Mockito.verify(descriptionField).setFieldValue(null);
        Mockito.verify(nameField).setFieldValue(null);
    }

}
