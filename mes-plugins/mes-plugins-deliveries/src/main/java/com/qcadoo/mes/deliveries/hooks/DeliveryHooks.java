package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DRAFT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class DeliveryHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private DeliveryStateChangeDescriber describer;

    private static final String L_FORM = "form";

    public void setInitialState(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, DRAFT);
    }

    public void clearStateFieldOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT);
    }

    public void updateRibbonState(final ViewDefinitionState view) {

        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = companyForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup suppliers = (RibbonGroup) window.getRibbon().getGroupByName("suppliers");

        RibbonActionItem redirectToFilteredDeliveriesList = (RibbonActionItem) suppliers
                .getItemByName("redirectToFilteredDeliveriesList");

        if (company.getId() == null) {
            updateButtonState(redirectToFilteredDeliveriesList, false);
        } else {
            updateButtonState(redirectToFilteredDeliveriesList, true);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }
}
