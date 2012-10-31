/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DeliveryDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateDeliveryNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERY, L_FORM, NUMBER);
    }

    public void setBufferForSupplier(final ViewDefinitionState view) {
        LookupComponent supplierLookup = (LookupComponent) view.getComponentByReference(DeliveryFields.SUPPLIER);
        FieldComponent deliveryDateBuffer = (FieldComponent) view.getComponentByReference("deliveryDateBuffer");
        Entity supplier = supplierLookup.getEntity();
        if (supplier == null) {
            deliveryDateBuffer.setFieldValue(null);
        } else {
            deliveryDateBuffer.setFieldValue(supplier.getField("buffer"));
        }
        deliveryDateBuffer.requestComponentUpdateState();
    }

    public void changedEnabledFieldForSpecificDeliveryState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        final Entity delivery = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERY).get(form.getEntityId());
        if (delivery.getStringField(STATE).equals(DeliveryState.PREPARED.getStringValue())
                || delivery.getStringField(STATE).equals(DeliveryState.APPROVED.getStringValue())) {
            changedEnabledFields(view, false, true);
        } else if (delivery.getStringField(STATE).equals(DeliveryState.DECLINED.getStringValue())
                || delivery.getStringField(STATE).equals(DeliveryState.RECEIVED.getStringValue())) {
            changedEnabledFields(view, false, false);
        } else {
            changedEnabledFields(view, true, true);
        }
    }

    private void changedEnabledFields(final ViewDefinitionState view, final boolean enabledFormAndOrderedProduct,
            final boolean enabledDeliveredGrid) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        GridComponent deliveredProducts = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);
        GridComponent orderedProducts = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        form.setFormEnabled(enabledFormAndOrderedProduct);
        deliveredProducts.setEnabled(enabledDeliveredGrid);
        deliveredProducts.setEditable(enabledDeliveredGrid);
        orderedProducts.setEnabled(enabledFormAndOrderedProduct);
        orderedProducts.setEditable(enabledFormAndOrderedProduct);
    }

}
