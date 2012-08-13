/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.materialFlowMultitransfers.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCTS;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlowMultitransfers.constants.ProductQuantityFields.PRODUCT;
import static com.qcadoo.mes.materialFlowMultitransfers.constants.ProductQuantityFields.QUANTITY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowMultitransfers.constants.MaterialFlowMultitransfersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

public class MultitransferListenersTest {

    private static final String L_FORM = "form";

    private MultitransferListeners multitransferListeners;

    @Mock
    private MaterialFlowService materialFlowService;

    // @Mock
    // private MaterialFlowResourceService materialFlowResourceService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private TimeConverterService timeConverterService;

    @Mock
    private DataDefinition staffDD, locationDD;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState state;

    @Mock
    private AwesomeDynamicListComponent adlc;

    @Mock
    private FormComponent formComponent;

    @Mock
    private FieldComponent unit;

    @Mock
    private Entity productQuantity, product, staff, locationFrom, locationTo;

    @Mock
    private FieldComponent timeComp, locationFromComponent, locationToComponent, typeComponent, staffComponent;

    @Mock
    private FormComponent form;

    @Mock
    private Date time;

    @Mock
    private Entity template;

    private static final String L_UNIT = "szt";

    private static final String L_TYPE = "type";

    @Mock
    private DataDefinition productQuantityDD;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        multitransferListeners = new MultitransferListeners();

        ReflectionTestUtils.setField(multitransferListeners, "materialFlowService", materialFlowService);
        // ReflectionTestUtils.setField(multitransferListeners, "materialFlowResourceService", materialFlowResourceService);
        ReflectionTestUtils.setField(multitransferListeners, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(multitransferListeners, "timeConverterService", timeConverterService);

        given(view.getComponentByReference(L_FORM)).willReturn(form);

        given(view.getComponentByReference(PRODUCTS)).willReturn(adlc);
        given(view.getComponentByReference(TYPE)).willReturn(typeComponent);
        given(view.getComponentByReference(TIME)).willReturn(timeComp);
        given(view.getComponentByReference(LOCATION_FROM)).willReturn(locationFromComponent);
        given(view.getComponentByReference(LOCATION_TO)).willReturn(locationToComponent);
        given(view.getComponentByReference(STAFF)).willReturn(staffComponent);

        List<FormComponent> formComponents = Arrays.asList(formComponent);
        given(adlc.getFormComponents()).willReturn(formComponents);

        given(formComponent.getEntity()).willReturn(productQuantity);
        given(formComponent.findFieldComponentByName(UNIT)).willReturn(unit);

        given(productQuantity.getBelongsToField(PRODUCT)).willReturn(product);
        given(productQuantity.getDecimalField(QUANTITY)).willReturn(BigDecimal.TEN);
        given(product.getStringField(UNIT)).willReturn(L_UNIT);

        given(typeComponent.getFieldValue()).willReturn(L_TYPE);
        given(staffComponent.getFieldValue()).willReturn(1L);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF)).willReturn(staffDD);
        given(staffDD.get(1L)).willReturn(staff);

        Long dateFieldValue = 123L;

        given(timeComp.getFieldValue()).willReturn(dateFieldValue);
        given(timeConverterService.getDateFromField(dateFieldValue)).willReturn(time);

        given(locationFromComponent.getFieldValue()).willReturn(0L);
        given(locationToComponent.getFieldValue()).willReturn(1L);

        given(dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION))
                .willReturn(locationDD);
        given(locationDD.get(0L)).willReturn(locationFrom);
        given(locationDD.get(1L)).willReturn(locationTo);

        // given(materialFlowResourceService.areResourcesSufficient(locationFrom, product, BigDecimal.TEN)).willReturn(true);
    }

    @Test
    public void shouldFillUnitsInADL() {
        // given

        // when
        multitransferListeners.fillUnitsInADL(view, state, null);

        // then
        verify(unit).setFieldValue(L_UNIT);
        verify(formComponent).setEntity(productQuantity);
    }

    // TODO lupo fix problem with test
    @Ignore
    @Test
    public void shouldCreateMultitransfer() {
        // given

        // when
        multitransferListeners.createMultitransfer(view, state, null);

        // then
        // verify(materialFlowTransferService).createTransfer(type, locationFrom, locationTo, product, BigDecimal.TEN, staff,
        // time);
        verify(form).addMessage("materialFlow.multitransfer.generate.success", MessageType.SUCCESS);
    }

    // TODO lupo fix problem with test
    @Ignore
    @Test
    public void shouldDownloadProductsFromTemplates() {
        // given
        List<Entity> templates = Arrays.asList(template);

        given(template.getBelongsToField(PRODUCT)).willReturn(product);
        given(
                dataDefinitionService.get(MaterialFlowMultitransfersConstants.PLUGIN_IDENTIFIER,
                        MaterialFlowMultitransfersConstants.MODEL_PRODUCT_QUANTITY)).willReturn(productQuantityDD);
        // given(materialFlowTransferService.getTransferTemplates(locationFrom, locationTo)).willReturn(templates);
        given(productQuantityDD.create()).willReturn(productQuantity);

        // when
        multitransferListeners.getFromTemplates(view, state, null);

        // then
        verify(productQuantity).setField(PRODUCT, product);
        verify(adlc).setFieldValue(Arrays.asList(productQuantity));
        verify(form).addMessage("materialFlow.multitransfer.template.success", MessageType.SUCCESS);
    }
}
