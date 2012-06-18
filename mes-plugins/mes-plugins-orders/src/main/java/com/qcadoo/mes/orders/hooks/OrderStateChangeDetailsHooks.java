package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.OrderStateChangeDescriber;
import com.qcadoo.mes.orders.states.constants.OrderState;
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
