package com.qcadoo.mes.orders.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderLogginsDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillReasonTypeDeviationsOfEffectiveStart(final ViewDefinitionState view) {
        fillReasonTypeDeviations(view, "reasonTypeDeviationsOfEffectiveStart", "commentReasonTypeDeviationsOfEffectiveStart",
                OrderStates.ACCEPTED.getStringValue(), OrderStates.IN_PROGRESS.getStringValue());
    }

    public void fillReasonTypeDeviationsOfEffectiveEnd(final ViewDefinitionState view) {
        fillReasonTypeDeviations(view, "reasonTypeDeviationsOfEffectiveEnd", "commentReasonTypeDeviationsOfEffectiveEnd",
                OrderStates.IN_PROGRESS.getStringValue(), OrderStates.COMPLETED.getStringValue());
    }

    private void fillReasonTypeDeviations(final ViewDefinitionState view, final String typeReasonReference,
            final String commentReference, final String previousState, final String currentState) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        FieldComponent typeReasonField = (FieldComponent) view.getComponentByReference(typeReasonReference);
        FieldComponent comment = (FieldComponent) view.getComponentByReference(commentReference);
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());
        Entity logging = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.eq("previousState", previousState))
                .add(SearchRestrictions.eq("currentState", currentState)).uniqueResult();
        typeReasonField.setFieldValue(logging.getField("reasonType"));
        comment.setFieldValue(logging.getField("comment"));
        typeReasonField.requestComponentUpdateState();
        comment.requestComponentUpdateState();
    }
}
