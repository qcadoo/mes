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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class MasterOrderProductDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    public void fillUnitField(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference(MasterOrderProductFields.PRODUCT);
        Entity product = productField.getEntity();
        String unit = null;

        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }
        for (String reference : Arrays.asList("cumulatedOrderQuantityUnit", "masterOrderQuantityUnit",
                "producedOrderQuantityUnit", "leftToReleaseUnit")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(unit);
            field.requestComponentUpdateState();
        }
    }

    public void fillDefaultTechnology(final ViewDefinitionState view) {
        masterOrderDetailsHooks.fillDefaultTechnology(view);
    }

    public void showErrorWhenCumulatedQuantity(final ViewDefinitionState view) {
        FormComponent masterOrderProductForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrderProduct = masterOrderProductForm.getEntity();

        if ((masterOrderProduct == null) || !masterOrderProduct.isValid()) {
            return;
        }

        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);

        if (!masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE).equals(
                MasterOrderType.MANY_PRODUCTS.getStringValue())) {
            return;
        }

        BigDecimal cumulatedQuantity = masterOrderProduct.getDecimalField(MasterOrderProductFields.CUMULATED_ORDER_QUANTITY);
        BigDecimal masterOrderQuantity = masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY);

        if ((cumulatedQuantity != null) && (masterOrderQuantity != null)
                && (cumulatedQuantity.compareTo(masterOrderQuantity) == -1)) {
            masterOrderProductForm.addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity",
                    MessageType.INFO, false);
        }
    }

}
