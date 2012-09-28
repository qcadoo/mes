package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DeliveryDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateAssignmentToShiftReportNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERY, L_FORM, NUMBER);
    }

    public void changedEnabledFieldForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        final Entity order = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY)
                .get(form.getEntityId());
        if (order.getStringField(STATE).equals(DeliveryState.PREPARED.getStringValue())
                || order.getStringField(STATE).equals(DeliveryState.APPROVED.getStringValue())) {
            changedEnabledFields(view, false, true);
        } else if (order.getStringField(STATE).equals(DeliveryState.DECLINED.getStringValue())
                || order.getStringField(STATE).equals(DeliveryState.RECEIVED.getStringValue())) {
            changedEnabledFields(view, false, false);
        } else {
            changedEnabledFields(view, true, true);
        }
    }

    private void changedEnabledFields(final ViewDefinitionState view, final boolean enabledFormAndOrderedProduct,
            final boolean enabledDeliveredGrid) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        GridComponent deliveredProducts = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);
        GridComponent orderedProducts = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        form.setFormEnabled(enabledFormAndOrderedProduct);
        deliveredProducts.setEnabled(enabledDeliveredGrid);
        deliveredProducts.setEditable(enabledDeliveredGrid);
        orderedProducts.setEnabled(enabledFormAndOrderedProduct);
        orderedProducts.setEditable(enabledFormAndOrderedProduct);
    }

}
