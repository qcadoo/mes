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
package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParametersListenersO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderService orderService;

    public void redirectToOrdersParameters(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Long parameterId = (Long) componentState.getFieldValue();

        if (parameterId != null) {
            String url = "../page/orders/ordersParameters.html?context={\"form.id\":\"" + parameterId + "\"}";
            view.redirectTo(url, false, true);
        }
    }

    public void redirectToDeviationsDictionary(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        Long dictionaryId = getDictionaryId("reasonTypeOfChangingOrderState");

        if (dictionaryId != null) {
            String url = "../page/qcadooDictionaries/dictionaryDetails.html?context={\"form.id\":\"" + dictionaryId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }

    }

    private Long getDictionaryId(final String name) {
        Entity dictionary = dataDefinitionService.get("qcadooModel", "dictionary").find()
                .add(SearchRestrictions.eq("name", name)).setMaxResults(1).uniqueResult();
        if (dictionary == null) {
            return null;
        } else {
            return dictionary.getId();
        }
    }

    public void showTimeField(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        String componentStateName = componentState.getName();
        if (REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM.equals(componentStateName)) {
            orderService.changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM, DELAYED_EFFECTIVE_DATE_FROM_TIME);
        } else if (REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM.equals(componentStateName)) {
            orderService.changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM, EARLIER_EFFECTIVE_DATE_FROM_TIME);
        } else if (REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO.equals(componentStateName)) {
            orderService.changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO, DELAYED_EFFECTIVE_DATE_TO_TIME);
        } else if (REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO.equals(componentStateName)) {
            orderService.changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO, EARLIER_EFFECTIVE_DATE_TO_TIME);
        }
    }

}
