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
package com.qcadoo.mes.negotForOrderSupplies.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.negotForOrderSupplies.NegotForOrderSuppliesService;
import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class GenerateMaterialRequirementCoverageListenersNFOS {

    private static final String L_FORM = "form";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private NegotForOrderSuppliesService negotForOrderSuppliesService;

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    public final void createNegotiation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent materialRequirementCoverageForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long materialRequirementCoverageId = materialRequirementCoverageForm.getEntityId();

        if (materialRequirementCoverageId == null) {
            return;
        }

        Entity negotiation = negotForOrderSuppliesService.createNegotiation(orderSuppliesService
                .getMaterialRequirementCoverage(materialRequirementCoverageId));

        if (negotiation == null) {
            materialRequirementCoverageForm.addMessage(
                    "orderSupplies.materialRequirementCoverage.negotiation.thereAreNoLacksToCover", MessageType.INFO);

            return;
        }

        Long negotiationId = negotiation.getId();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", negotiationId);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.negotiation");

        String url = "../page/supplyNegotiations/negotiationDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

}
