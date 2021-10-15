/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderDetailsHooksPFTD {

    @Autowired
    ParameterService parameterService;

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public void onBeforeRender(final ViewDefinitionState view) {

        orderDetailsRibbonHelper.setButtonEnabled(view, "materialFlow", "warehouseIssues", OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY::test);

        orderDetailsRibbonHelper.setButtonEnabled(view, "materialFlow", "componentAvailability", OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY::test);

    }

    public void onBeforeRenderAdditionalForm(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent ignoreMissingProductsField = (FieldComponent) view
                .getComponentByReference(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS);

        if (form.getEntityId() == null) {
            ignoreMissingProductsField.setFieldValue(parameterService.getParameter().getBooleanField(
                    ParameterFieldsPFTD.IGNORE_MISSING_COMPONENTS));
            ignoreMissingProductsField.requestComponentUpdateState();
        }

    }
}
