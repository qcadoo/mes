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
package com.qcadoo.mes.supplyNegotiations.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;

@Service
public class NegotiationDetailsListeners {



    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_NEGOTIATION_NUMBER = "negotiationNumber";

    public final void showRequestForQuotationsForGivenNegotiation(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity negotiation = negotiationForm.getEntity();

        if (negotiation.getId() == null) {
            return;
        }

        String negotiationNumber = negotiation.getStringField(NUMBER);

        if (negotiationNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put(L_NEGOTIATION_NUMBER, negotiationNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.requestsForQuotation");

        String url = "../page/supplyNegotiations/requestForQuotationsList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOffersForGivenNegotiation(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity negotiation = negotiationForm.getEntity();

        if (negotiation.getId() == null) {
            return;
        }

        String negotiationNumber = negotiation.getStringField(NUMBER);

        if (negotiationNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put(L_NEGOTIATION_NUMBER, negotiationNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.offer");

        String url = "../page/supplyNegotiations/offersList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOffersItemsForGivenNegotiation(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity negotiation = negotiationForm.getEntity();

        if (negotiation.getId() == null) {
            return;
        }

        String negotiationNumber = negotiation.getStringField(NUMBER);

        if (negotiationNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put(L_NEGOTIATION_NUMBER, negotiationNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.offersItems");

        String url = "../page/supplyNegotiations/offersItems.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showSupplyItemsForGivenNegotiation(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity negotiation = negotiationForm.getEntity();

        if (negotiation.getId() == null) {
            return;
        }

        String negotiationNumber = negotiation.getStringField(NUMBER);

        if (negotiationNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put(L_NEGOTIATION_NUMBER, negotiationNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.supplyItems");

        String url = "../page/deliveries/supplyItems.html";
        view.redirectTo(url, false, true, parameters);
    }
}
