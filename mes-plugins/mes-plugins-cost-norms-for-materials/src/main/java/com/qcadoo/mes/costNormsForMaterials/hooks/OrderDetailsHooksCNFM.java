package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksCNFM {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateViewCostsButtonState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup materials = (RibbonGroup) window.getRibbon().getGroupByName("materials");
        RibbonActionItem viewCosts = (RibbonActionItem) materials.getItemByName("viewCosts");

        if (orderForm.getEntityId() != null) {
            Long orderId = orderForm.getEntityId();

            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (order != null) {
                Entity technology = order.getBelongsToField(TECHNOLOGY);

                if ((technology != null) && (TechnologyState.ACCEPTED.getStringValue().equals(technology.getStringField(STATE)))) {
                    viewCosts.setEnabled(true);
                    viewCosts.requestUpdate(true);

                    return;
                }
            }
        }
        viewCosts.setEnabled(false);
        viewCosts.requestUpdate(true);
    }
}
