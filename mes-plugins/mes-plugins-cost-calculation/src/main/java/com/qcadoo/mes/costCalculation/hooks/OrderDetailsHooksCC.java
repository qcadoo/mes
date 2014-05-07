package com.qcadoo.mes.costCalculation.hooks;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksCC {

    private static final Set<TechnologyState> SUPPORTED_TECHNOLOGY_STATES = ImmutableSet.of(TechnologyState.ACCEPTED,
            TechnologyState.CHECKED);

    public void onBeforeRender(final ViewDefinitionState view) {
        toggleCostCalculationButtonEnabled(view);
    }

    private void toggleCostCalculationButtonEnabled(final ViewDefinitionState view) {
        RibbonActionItem performCostCalculationButton = getPerformCostCalculationButton(view);
        Entity order = getOrderEntity(view);
        if (performCostCalculationButton == null || order == null || OrderState.of(order) != OrderState.PENDING) {
            return;
        }
        Entity orderTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (orderTechnology == null) {
            return;
        }

        performCostCalculationButton.setEnabled(SUPPORTED_TECHNOLOGY_STATES.contains(TechnologyState.of(orderTechnology)));
        performCostCalculationButton.requestUpdate(true);
    }

    private Entity getOrderEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form == null) {
            return null;
        }
        return form.getPersistedEntityWithIncludedFormValues();
    }

    private RibbonActionItem getPerformCostCalculationButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup costCalculationGroup = ribbon.getGroupByName("costCalculate");
        if (costCalculationGroup == null) {
            return null;
        }
        return costCalculationGroup.getItemByName("costCalculate");
    }

}
