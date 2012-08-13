/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeDescriber;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderStateChangeDetailsHooks {

    @Autowired
    private OrderStateChangeDescriber describer;

    public void fillReasonTypeDeviationsOfEffectiveStart(final ViewDefinitionState view) {
        fillReasonTypeDeviations(view, "reasonTypeDeviationsOfEffectiveStart", "commentReasonTypeDeviationsOfEffectiveStart",
                OrderState.ACCEPTED.getStringValue(), OrderState.IN_PROGRESS.getStringValue());
    }

    public void fillReasonTypeDeviationsOfEffectiveEnd(final ViewDefinitionState view) {
        fillReasonTypeDeviations(view, "reasonTypeDeviationsOfEffectiveEnd", "commentReasonTypeDeviationsOfEffectiveEnd",
                OrderState.IN_PROGRESS.getStringValue(), OrderState.COMPLETED.getStringValue());
    }

    private void fillReasonTypeDeviations(final ViewDefinitionState view, final String typeReasonReference,
            final String commentReference, final String previousState, final String currentState) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        final FieldComponent typeReasonField = (FieldComponent) view.getComponentByReference(typeReasonReference);
        final FieldComponent comment = (FieldComponent) view.getComponentByReference(commentReference);
        final Entity stateChange = getLastMatchingStateChange(form.getEntityId(), previousState, currentState);
        if (stateChange == null) {
            return;
        }
        typeReasonField.setFieldValue(stateChange.getField("reasonType"));
        comment.setFieldValue(stateChange.getField("comment"));
        typeReasonField.requestComponentUpdateState();
        comment.requestComponentUpdateState();
    }

    private Entity getLastMatchingStateChange(final Long orderId, final String previousState, final String currentState) {
        final SearchCriteriaBuilder criteriaBuilder = describer.getDataDefinition().find();
        criteriaBuilder.createAlias(describer.getOwnerFieldName(), describer.getOwnerFieldName());
        criteriaBuilder.add(SearchRestrictions.eq(describer.getOwnerFieldName() + ".id", orderId));
        criteriaBuilder.add(SearchRestrictions.eq(describer.getSourceStateFieldName(), previousState));
        criteriaBuilder.add(SearchRestrictions.eq(describer.getTargetStateFieldName(), currentState));
        criteriaBuilder.add(SearchRestrictions.eq(describer.getStatusFieldName(), SUCCESSFUL.getStringValue()));
        criteriaBuilder.addOrder(SearchOrders.desc(describer.getDateTimeFieldName()));
        return criteriaBuilder.setMaxResults(1).uniqueResult();
    }
}
