/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.basicProductionCounting.listeners;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class BasicProductionCountingListListeners {

    public void showDetailedProductionCountingAndProgress(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId == null) {
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("order.id", orderId);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "../page/basicProductionCounting/detailedProductionCountingAndProgressList.html?context=" + json.toString();
        view.redirectTo(url, false, true);
    }

    public void showDetailedProductionCounting(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId == null) {
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("order.id", orderId);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "../page/basicProductionCounting/detailedProductionCountingList.html?context=" + json.toString();
        view.redirectTo(url, false, true);
    }

}
