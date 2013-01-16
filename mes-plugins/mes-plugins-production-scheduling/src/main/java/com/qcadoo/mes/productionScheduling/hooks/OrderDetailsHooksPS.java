package com.qcadoo.mes.productionScheduling.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksPS {

    private static final String FORM = "form";

    private static final String OPERATION_DURATION = "operationDuration";

    private static final String L_WINDOW = "window";

    public void disabledButtonOperationDuration(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(FORM);
        if (form.getEntityId() == null) {
            disabledButton(view, false);
            return;
        }
        Entity order = form.getEntity().getDataDefinition().get(form.getEntityId());
        if (order.getBelongsToField(OrderFields.TECHNOLOGY) == null) {
            disabledButton(view, false);
            return;
        }
        disabledButton(view, true);
    }

    private void disabledButton(final ViewDefinitionState view, final boolean enable) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup group = (RibbonGroup) window.getRibbon().getGroupByName(OPERATION_DURATION);

        RibbonActionItem operationDuration = (RibbonActionItem) group.getItemByName(OPERATION_DURATION);
        operationDuration.setEnabled(enable);
        operationDuration.requestUpdate(true);
    }
}
