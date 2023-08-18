/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.criteriaModifiers.StaffCriteriaModifierPFTD;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderDetailsHooksPFTD {

    @Autowired
    ParameterService parameterService;

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    public void onBeforeRender(final ViewDefinitionState view) {
        orderDetailsRibbonHelper.setButtonEnabled(view, "materialFlow", "warehouseIssues", OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY::test);

        orderDetailsRibbonHelper.setButtonEnabled(view, "materialFlow", "componentAvailability", OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY::test);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        ComponentState staffTab = view.getComponentByReference("staffTab");
        if (form.getEntityId() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(form.getEntityId());
            staffTab.setVisible(TypeOfProductionRecording.CUMULATED.getStringValue()
                    .equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)));
            LookupComponent technologyLookup = (LookupComponent) view
                    .getComponentByReference(OrderFields.TECHNOLOGY);
            LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);
            Entity technology = technologyLookup.getEntity();
            Entity productionLine = productionLineLookup.getEntity();
            FieldComponent plannedStaffField = (FieldComponent) view.getComponentByReference("plannedStaff");
            if (technology != null && productionLine != null) {
                Optional<Integer> plannedStaff = technologyService.getPlannedStaff(technology, productionLine);
                if (plannedStaff.isPresent()) {
                    plannedStaffField.setFieldValue(plannedStaff.get());
                } else {
                    plannedStaffField.setFieldValue(null);
                }
            } else {
                plannedStaffField.setFieldValue(null);
            }
            FieldComponent actualStaff = (FieldComponent) view.getComponentByReference("actualStaff");
            actualStaff.setFieldValue(order.getManyToManyField(OrderFields.STAFF).size());

            LookupComponent staffLookup = (LookupComponent) view.getComponentByReference("staffLookup");

            FilterValueHolder valueHolder = staffLookup.getFilterValue();
            valueHolder.put(StaffCriteriaModifierPFTD.L_ORDER_ID, form.getEntityId());
            if (productionLine != null) {
                valueHolder.put(StaffCriteriaModifierPFTD.L_PRODUCTION_LINE_ID, productionLine.getId());
            } else if (valueHolder.has(StaffCriteriaModifierPFTD.L_PRODUCTION_LINE_ID)) {
                valueHolder.remove(StaffCriteriaModifierPFTD.L_PRODUCTION_LINE_ID);
            }
            staffLookup.setFilterValue(valueHolder);
            String state = order.getStringField(OrderFields.STATE);
            GridComponent staffGrid = (GridComponent) view.getComponentByReference(OrderFields.STAFF);
            staffGrid.setEnabled(!OrderState.DECLINED.getStringValue().equals(state)
                    && !OrderState.ABANDONED.getStringValue().equals(state)
                    && !OrderState.COMPLETED.getStringValue().equals(state));
        } else {
            staffTab.setVisible(false);
        }
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
