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
package com.qcadoo.mes.productionPerShift.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderDetailsListenersPPS {

    @Autowired
    private PPSHelper ppsHelper;

    public void redirectToProductionPerShift(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId == null) {
            return;
        }

        Long ppsId = ppsHelper.getPpsIdForOrder(orderId);

        if (ppsId == null) {
            ppsId = ppsHelper.createPpsForOrderAndReturnId(orderId);
            Preconditions.checkNotNull(ppsId);
        }

        redirect(view, ppsId);
    }

    private void redirect(final ViewDefinitionState viewState, final Long ppsId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", ppsId);

        String url = "../page/productionPerShift/productionPerShiftDetails.html";
        viewState.redirectTo(url, false, true, parameters);
    }

}
