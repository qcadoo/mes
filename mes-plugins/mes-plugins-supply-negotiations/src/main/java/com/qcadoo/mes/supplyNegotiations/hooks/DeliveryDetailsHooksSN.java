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
package com.qcadoo.mes.supplyNegotiations.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class DeliveryDetailsHooksSN {



    private static final String L_OFFER = "offer";

    private static final String L_FILL_PRICES = "fillPrices";

    public void disabledButtonWhenEmptyOrders(final ViewDefinitionState view) {
        FieldComponent deliveryState = (FieldComponent) view.getComponentByReference(DeliveryFields.STATE);
        RibbonActionItem fillPricesButton = getFillPricesButton(view);

        if (DeliveryState.DRAFT.getStringValue().equals(deliveryState.getFieldValue())
                || DeliveryState.DURING_CORRECTION.getStringValue().equals(deliveryState.getFieldValue())) {

            GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

            boolean isEnabled = !orderedProductsGrid.getEntities().isEmpty();

            fillPricesButton.setEnabled(isEnabled);
        } else {
            fillPricesButton.setEnabled(false);
        }

        fillPricesButton.requestUpdate(true);
    }

    private RibbonActionItem getFillPricesButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup ribbonGroup = window.getRibbon().getGroupByName(L_OFFER);

        return ribbonGroup.getItemByName(L_FILL_PRICES);
    }

    public void addTooltip(final ViewDefinitionState view) {
        RibbonActionItem fillPricesButton = getFillPricesButton(view);

        fillPricesButton.setMessage("deliveries.deliveryDetails.window.ribbon.offer.fillPrices.description");
        fillPricesButton.requestUpdate(true);
    }

}
