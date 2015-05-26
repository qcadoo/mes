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
package com.qcadoo.mes.productionCounting.listeners;

import java.util.List;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.TechnologyFieldsPC;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderDetailsListenersPC {
    private static final String L_FORM = "form";

    private static final List<String> L_ORDER_FIELD_NAMES = Lists.newArrayList(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
            OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, OrderFieldsPC.REGISTER_PRODUCTION_TIME,
            OrderFieldsPC.REGISTER_PIECEWORK, OrderFieldsPC.JUST_ONE, OrderFieldsPC.ALLOW_TO_CLOSE,
            OrderFieldsPC.AUTO_CLOSE_ORDER);

    private static final List<String> L_TECHNOLOGY_FIELD_NAMES = Lists.newArrayList(TechnologyFieldsPC.REGISTER_QUANTITY_IN_PRODUCT,
            TechnologyFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT, TechnologyFieldsPC.REGISTER_PRODUCTION_TIME,
            TechnologyFieldsPC.REGISTER_PIECEWORK, TechnologyFieldsPC.JUST_ONE, TechnologyFieldsPC.ALLOW_TO_CLOSE,
            TechnologyFieldsPC.AUTO_CLOSE_ORDER);

    @Autowired
    private ProductionCountingService productionCountingService;

    public void disableFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent typeOfProductionRecordingField = (FieldComponent) view
                .getComponentByReference(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        String typeOfProductionRecording = (String) typeOfProductionRecordingField.getFieldValue();

        if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)
                || productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
            productionCountingService.setComponentsState(view, L_ORDER_FIELD_NAMES, true, true);
        }

        productionCountingService.changeDoneQuantityAndAmountOfProducedQuantityFieldState(view);
    }

    public void fillPCParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent order = (FormComponent) view.getComponentByReference(L_FORM);

        Entity orderEntity = order.getPersistedEntityWithIncludedFormValues();
        Entity technology = orderEntity.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        for (String fieldComponentName : L_TECHNOLOGY_FIELD_NAMES) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldComponentName);

                fieldComponent.setFieldValue(getDefaultValueForProductionCounting(technology, fieldComponentName));
                fieldComponent.requestComponentUpdateState();


            fieldComponent.setEnabled(false);
        }
        FieldComponent typeOfProductionRecordingField = (FieldComponent) view
                .getComponentByReference(TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
            typeOfProductionRecordingField
                    .setFieldValue(getDefaultValueForTypeOfProductionRecording(technology,
                            TechnologyFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

    }

    private boolean getDefaultValueForProductionCounting(final Entity technology, final String fieldName) {
        return technology.getBooleanField(fieldName);
    }

    private String getDefaultValueForTypeOfProductionRecording(final Entity technology, final String fieldName) {
        return technology.getStringField(fieldName);
    }
}
