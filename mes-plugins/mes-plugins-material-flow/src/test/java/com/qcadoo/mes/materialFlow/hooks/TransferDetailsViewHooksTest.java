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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class TransferDetailsViewHooksTest {

    private TransferDetailsViewHooks transferDetailsViewHooks;

    private static final String L_FORM = "form";

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent transferForm;

    @Mock
    private FieldComponent typeField, timeField, locationFromField, locationToField, staffField;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition transferDD;

    @Mock
    private Entity transfer, transformations;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transferDetailsViewHooks = new TransferDetailsViewHooks();

        setField(transferDetailsViewHooks, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldReturnWhenCheckIfTransferHasTransformationsAndNumberIsNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(transferForm);

        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(typeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(transferForm.getEntityId()).willReturn(null);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField, never()).setEnabled(false);
        verify(timeField, never()).setEnabled(false);
        verify(locationFromField, never()).setEnabled(false);
        verify(locationToField, never()).setEnabled(false);
        verify(staffField, never()).setEnabled(false);
    }

    @Test
    public void shouldReturnWhenCheckIfTransferHasTransformationsAndTransferIsNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(transferForm);

        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(timeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(transferForm.getEntityId()).willReturn(1L);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER))
                .willReturn(transferDD);
        given(transferDD.get(1L)).willReturn(null);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField, never()).setEnabled(false);
        verify(timeField, never()).setEnabled(false);
        verify(locationFromField, never()).setEnabled(false);
        verify(locationToField, never()).setEnabled(false);
        verify(staffField, never()).setEnabled(false);
    }

    @Test
    public void shouldReturnWhenCheckIfTransferHasTransformationsAndTransformationsAreNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(transferForm);

        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(timeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(transferForm.getEntityId()).willReturn(1L);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER))
                .willReturn(transferDD);
        given(transferDD.get(1L)).willReturn(transfer);

        given(transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION)).willReturn(null);
        given(transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION)).willReturn(null);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField, never()).setEnabled(false);
        verify(timeField, never()).setEnabled(false);
        verify(locationFromField, never()).setEnabled(false);
        verify(locationToField, never()).setEnabled(false);
        verify(staffField, never()).setEnabled(false);
    }

    @Test
    public void shouldDisableFieldsWhenCheckIfTransferHasTransformationsAndTransformationsAreNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(transferForm);

        given(view.getComponentByReference(TYPE)).willReturn(typeField);
        given(view.getComponentByReference(TIME)).willReturn(timeField);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromField);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToField);
        given(view.getComponentByReference(STAFF)).willReturn(staffField);

        given(transferForm.getEntityId()).willReturn(1L);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER))
                .willReturn(transferDD);
        given(transferDD.get(1L)).willReturn(transfer);

        given(transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION)).willReturn(transformations);
        given(transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION)).willReturn(transformations);

        // when
        transferDetailsViewHooks.checkIfTransferHasTransformations(view);

        // then
        verify(typeField).setEnabled(false);
        verify(timeField).setEnabled(false);
        verify(locationFromField).setEnabled(false);
        verify(locationToField).setEnabled(false);
        verify(staffField).setEnabled(false);
    }

}
