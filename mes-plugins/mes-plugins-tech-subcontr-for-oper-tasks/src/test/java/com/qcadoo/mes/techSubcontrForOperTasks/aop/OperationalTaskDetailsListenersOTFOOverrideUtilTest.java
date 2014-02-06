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

import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyInstanceOperCompFieldsTS;
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
    private Entity order, technologyOperationComponent;

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
    public void shouldntOperationalTaskNameDescriptionAndProductionLineForSubcontractedWhenIsSubcontracting() throws Exception {
        // given
        given(orderLookup.getEntity()).willReturn(order);
        given(technologyOperationComponentLookup.getEntity()).willReturn(technologyOperationComponent);
        given(technologyOperationComponent.getBooleanField(TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING))
                .willReturn(true);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.setOperationalTaskNameDescriptionAndProductionLineForSubcontracted(view);

        // then
        Mockito.verify(nameField).setFieldValue(null);
        Mockito.verify(descriptionField).setFieldValue(null);
        Mockito.verify(productionLineLookup).setFieldValue(null);
    }

    @Test
    public void shouldntOperationalTaskNameDescriptionAndProductionLineForSubcontractedWhenTechnologyOperationComponentIsNull()
            throws Exception {
        // given
        given(orderLookup.getEntity()).willReturn(order);
        given(technologyOperationComponentLookup.getEntity()).willReturn(null);
        given(technologyOperationComponent.getBooleanField(TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING)).willReturn(
                false);

        // when
        operationalTaskDetailsListenersOTFOOverrideUtil.setOperationalTaskNameDescriptionAndProductionLineForSubcontracted(view);

        // then
        Mockito.verify(descriptionField).setFieldValue(null);
        Mockito.verify(nameField).setFieldValue(null);
        Mockito.verify(productionLineLookup).setFieldValue(null);
    }

}
