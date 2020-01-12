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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogyForOrders.constants.ParameterFieldsAGFO;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ParametersViewHooks {

    @Autowired
    private PluginManager pluginManager;

    public void checkSelectedTrackingRecordForOrderTreatmentInParameters(final ViewDefinitionState view,
            final ComponentState state, final String[] args) {
        if (pluginManager.isPluginEnabled("srcAdvGenealogyForOrders")) {
            FieldComponent trackingRecordForOrderTreatment = (FieldComponent) view
                    .getComponentByReference("trackingRecordForOrderTreatment");
            if (trackingRecordForOrderTreatment.getFieldValue()
                    .equals(TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT.getStringValue())) {
                FormComponent form = (FormComponent) view.getComponentByReference("form");
                form.addMessage("srcAdvGenealogy.trackingRecordForOrderTreatment.inParameters", MessageType.INFO, false);
            }
        }
    }

    public void generateBatchForOrderedProductChange(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
    }

    public final void setNumberPatternEnabled(final ViewDefinitionState view) {
        CheckBoxComponent generateBatchForOrderedProduct = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsAGFO.GENERATE_BATCH_FOR_ORDERED_PRODUCT);
        FieldComponent numberPattern = (FieldComponent) view.getComponentByReference(ParameterFieldsAGFO.NUMBER_PATTERN);

        if (generateBatchForOrderedProduct.isChecked()) {
            numberPattern.setEnabled(true);
        } else {
            numberPattern.setEnabled(false);
            numberPattern.setFieldValue(null);
        }
    }
}
