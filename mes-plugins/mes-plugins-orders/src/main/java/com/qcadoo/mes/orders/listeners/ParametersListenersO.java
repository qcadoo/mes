/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.orders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParametersListenersO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void redirectToOrdersParameters(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Long parameterId = (Long) componentState.getFieldValue();

        if (parameterId != null) {
            String url = "../page/orders/ordersParameters.html?context={\"form.id\":\"" + parameterId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void redirectToDeviationsDictionary(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        Long dictionaryId = getDictionaryId("reasonTypeOfChaningOrderState");

        if (dictionaryId != null) {
            String url = "../page/qcadooDictionaries/dictionaryDetails.html?context={\"form.id\":\"" + dictionaryId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }

    }

    private Long getDictionaryId(final String name) {
        Entity dictionary = dataDefinitionService.get("qcadoomodel", "dictionary").find()
                .add(SearchRestrictions.eq("name", name)).setMaxResults(1).uniqueResult();
        if (dictionary == null) {
            return null;
        } else {
            return dictionary.getId();
        }
    }
}
