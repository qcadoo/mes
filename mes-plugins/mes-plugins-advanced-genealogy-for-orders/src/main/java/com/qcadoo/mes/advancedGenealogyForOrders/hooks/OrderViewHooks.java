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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderViewHooks {

    private static final String L_STATE = "state";

    private static final String L_TRACKING_RECORD_TREATMENT = "trackingRecordTreatment";

    private static final String L_TRACKING_RECORD_FOR_ORDER_TREATMENT = "trackingRecordForOrderTreatment";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = getForm(view);
        setOrderDefaultValue(view, form);
        changeOrderFieldState(view);
        toggleCostCalculationButtonEnabled(view, form);
    }

    void setOrderDefaultValue(final ViewDefinitionState view, final FormComponent form) {
        FieldComponent trackingRecordTreatment = getFieldComponent(view, L_TRACKING_RECORD_TREATMENT);

        if ((form.getEntityId() == null) && (trackingRecordTreatment.getFieldValue() == null)) {
            trackingRecordTreatment.setFieldValue(getTrackingRecordForOrderTreatment());
            trackingRecordTreatment.requestComponentUpdateState();
        }
    }

    private void changeOrderFieldState(final ViewDefinitionState view) {
        FieldComponent trackingRecordTreatment = getFieldComponent(view, L_TRACKING_RECORD_TREATMENT);

        FieldComponent state = getFieldComponent(view, L_STATE);

        if (PENDING.getStringValue().equals(state.getFieldValue())) {
            trackingRecordTreatment.setEnabled(true);
        } else {
            trackingRecordTreatment.setEnabled(false);
        }
    }

    private void toggleCostCalculationButtonEnabled(final ViewDefinitionState view, final FormComponent form) {
        orderDetailsRibbonHelper.setButtonEnabled(view, "trackingRecordsDetails", "trackingRecordsDetails",
                OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY,
                Optional.of("orders.ribbon.message.mustChangeTechnologyState"));
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private FieldComponent getFieldComponent(final ViewDefinitionState view, final String name) {
        return (FieldComponent) view.getComponentByReference(name);
    }

    public String getTrackingRecordForOrderTreatment() {
        return parameterService.getParameter().getStringField(L_TRACKING_RECORD_FOR_ORDER_TREATMENT);
    }

    public void checkSelectedTrackingRecordForOrderTreatmentInOrder(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        // FIXME dev_team: this listener should be defined & injected from srcAdvGenealogyForOrders.
        if (pluginManager.isPluginEnabled("srcAdvGenealogyForOrders")) {
            FieldComponent trackingRecordTreatment = (FieldComponent) view.getComponentByReference("trackingRecordTreatment");
            FieldComponent externalNumber = (FieldComponent) view.getComponentByReference("externalNumber");
            if (externalNumber != null
                    && !externalNumber.getFieldValue().equals("")
                    && trackingRecordTreatment.getFieldValue().equals(
                            TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT.getStringValue())) {

                FormComponent form = (FormComponent) view.getComponentByReference("form");
                Entity order = form.getEntity();
                String orderTrackingRecordTreatment = order.getStringField("trackingRecordTreatment");
                state.addMessage("srcAdvGenealogy.trackingRecordForOrderTreatment.inOrder", MessageType.FAILURE,
                        orderTrackingRecordTreatment);
            }
        }
    }
}
