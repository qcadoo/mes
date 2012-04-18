package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.plugin.api.PluginUtils.isEnabled;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyInstOperCompDetailsHooks {

    public void disabledFormWhenOrderStateIsAccepted(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity techInstOperComp = form.getEntity().getDataDefinition().get(form.getEntityId());
        Entity order = techInstOperComp.getBelongsToField("order");
        WindowComponent windowComponent = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonGroup standardFormTemplate = windowComponent.getRibbon().getGroupByName("actions");
        if (!order.getStringField(OrderFields.STATE).equals(OrderStates.PENDING.getStringValue())) {
            form.setFormEnabled(false);
            for (RibbonActionItem item : standardFormTemplate.getItems()) {
                if (!item.getName().equals("refresh")) {
                    item.setEnabled(false);
                    item.requestUpdate(true);
                }
            }
            if (isEnabled("costNormsForOperation")) {
                disabledButton(windowComponent, "costs", "copyCostsFromTechnology");
            }
            if (isEnabled("timeNormsForOperations")) {
                disabledButton(windowComponent, "norm", "copyTimeNormsFromTechnology");
            }
        }
    }

    private void disabledButton(final WindowComponent windowComponent, final String ribbonGroup, final String ribbonActionItem) {
        RibbonGroup group = windowComponent.getRibbon().getGroupByName(ribbonGroup);
        RibbonActionItem actionItem = group.getItemByName(ribbonActionItem);
        actionItem.setEnabled(false);
        actionItem.requestUpdate(true);
    }
}
