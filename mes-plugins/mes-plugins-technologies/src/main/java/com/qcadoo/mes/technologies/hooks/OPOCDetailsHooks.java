/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.technologies.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OPOCDetailsHooks {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setOPOCDefaultQuantityFromParameter(view);
    }

    private void setOPOCDefaultQuantityFromParameter(final ViewDefinitionState view) {
        FormComponent operationProductOutComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent quantityField = (FieldComponent) view
                .getComponentByReference(OperationProductOutComponentFields.QUANTITY);
        if (operationProductOutComponentForm.getEntityId() == null && quantityField.getFieldValue() == null) {
            BigDecimal operationProductOutDefaultQuantity = parameterService.getParameter().getDecimalField(ParameterFieldsT.OPERATION_PRODUCT_OUT_DEFAULT_QUANTITY);
            if (operationProductOutDefaultQuantity != null) {
                quantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(operationProductOutDefaultQuantity, 0));
            }
        }
    }

}
