package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksPPS {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ORDER_PROGRESS_PLANS = "orderProgressPlans";

    private static final String L_PRODUCTION_PER_SHIFT = "productionPerShift";

    @Autowired
    private OrderService orderService;

    public void updateViewPPSButtonState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup orderProgressPlans = (RibbonGroup) window.getRibbon().getGroupByName(L_ORDER_PROGRESS_PLANS);
        RibbonActionItem productionPerShift = (RibbonActionItem) orderProgressPlans.getItemByName(L_PRODUCTION_PER_SHIFT);

        Long orderId = orderForm.getEntityId();

        Entity order = orderForm.getPersistedEntityWithIncludedFormValues();

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        String orderType = order.getStringField(OrderFields.ORDER_TYPE);

        boolean isEnabled = false;

        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            Entity technologyPrototype = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

            isEnabled = checkIfOrderIsSavedAndTechnologyPrototypeStateIsNotDraftOrNotAccepted(orderId, technology,
                    technologyPrototype);
        } else if (OrderType.WITH_OWN_TECHNOLOGY.getStringValue().equals(orderType)) {
            isEnabled = checkIfOrderIsSavedAndTechnologyStateIsNotDraft(orderId, technology);
        } else {
            if (orderId != null) {
                order = orderService.getOrder(orderId);

                if (order != null) {
                    technology = order.getBelongsToField(TECHNOLOGY);

                    isEnabled = (technology != null);
                }
            }
        }

        updatePPSButtonState(productionPerShift, isEnabled);
    }

    private boolean checkIfOrderIsSavedAndTechnologyPrototypeStateIsNotDraftOrNotAccepted(final Long orderId,
            final Entity technology, final Entity technologyPrototype) {
        return ((orderId != null) && (technology != null) && (technologyPrototype != null) && checkIfTechnologyPrototypeStateIsNotDraftOrNotAccepted(technologyPrototype));
    }

    private boolean checkIfTechnologyPrototypeStateIsNotDraftOrNotAccepted(final Entity technologyPrototype) {
        return (!checkIfTechnologyStateIsDraft(technologyPrototype) || !checkIfTechnologyStateIsAccepted(technologyPrototype));
    }

    private boolean checkIfTechnologyStateIsDraft(final Entity technology) {
        return (TechnologyState.DRAFT.getStringValue().equals(technology.getStringField(TechnologyFields.STATE)));
    }

    private boolean checkIfTechnologyStateIsAccepted(final Entity technology) {
        return (TechnologyState.ACCEPTED.getStringValue().equals(technology.getStringField(TechnologyFields.STATE)));
    }

    private boolean checkIfOrderIsSavedAndTechnologyStateIsNotDraft(final Long orderId, final Entity technology) {
        return ((orderId != null) && !checkIfTechnologyStateIsDraft(technology));
    }

    private void updatePPSButtonState(final RibbonActionItem productionPerShift, final boolean isEnabled) {
        productionPerShift.setEnabled(isEnabled);
        productionPerShift.requestUpdate(true);
    }

}
