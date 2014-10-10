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
package com.qcadoo.mes.materialRequirements.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirements.constants.OrderFieldsMR;
import com.qcadoo.mes.materialRequirements.constants.ParameterFieldsMR;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderDetailsHooksMR {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public void setInputProductsRequiredForTypeFromParameters(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (orderForm.getEntityId() != null) {
            return;
        }

        FieldComponent inputProductsRequiredForTypeField = (FieldComponent) view
                .getComponentByReference(OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE);

        String inputProductsRequiredForType = (String) inputProductsRequiredForTypeField.getFieldValue();

        if (StringUtils.isEmpty(inputProductsRequiredForType)) {
            inputProductsRequiredForTypeField.setFieldValue(getInputProductsRequiredForType());
        }

        inputProductsRequiredForTypeField.requestComponentUpdateState();
    }

    private String getInputProductsRequiredForType() {
        return parameterService.getParameter().getStringField(ParameterFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE);
    }

}
