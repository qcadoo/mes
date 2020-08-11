package com.qcadoo.mes.basicProductionCounting.hooks;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class OperationalTaskHooksBPC {

    public void disabledButtonForAppropriateState(final ViewDefinitionState view) {
        setButtonEnabledForOrder(view, BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_GROUP,
                BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_NAME, Optional.empty());
    }

    public void setButtonEnabledForOrder(final ViewDefinitionState view, final String ribbonGroupName, final String ribbonItemName, final Optional<String> message) {
        RibbonActionItem ribbonItem = getRibbonItem(view, ribbonGroupName, ribbonItemName);
        boolean enabled = canEnabled(view);
        if (ribbonItem == null) {
            return;
        }
        ribbonItem.setEnabled(enabled);
        if (!enabled && message.isPresent()) {
            ribbonItem.setMessage(message.get());
        }
        ribbonItem.requestUpdate(true);
    }

    private boolean canEnabled(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity operationalTask = form.getEntity();
        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
        if (order == null) {
            return false;
        }
        OrderState orderState = OrderState.of(order);
        return orderState != OrderState.DECLINED && orderState != OrderState.PENDING;
    }

    private RibbonActionItem getRibbonItem(final ViewDefinitionState view, final String ribbonGroupName,
            final String ribbonItemName) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(ribbonGroupName);
        if (ribbonGroup == null) {
            return null;
        }
        return ribbonGroup.getItemByName(ribbonItemName);
    }

}
