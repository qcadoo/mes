package com.qcadoo.mes.productFlowThruDivision.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionTrackingDetailsHooksPFTD {

    private static final String L_MATERIAL_FLOW = "materialFlow";

    private static final String L_COMPONENT_AVAILABILITY = "componentAvailability";

    public void onBeforeRender(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup materialFlowRibbonGroup = (RibbonGroup) window.getRibbon().getGroupByName(L_MATERIAL_FLOW);
        RibbonActionItem componentAvailabilityRibbonActionItem = (RibbonActionItem) materialFlowRibbonGroup
                .getItemByName(L_COMPONENT_AVAILABILITY);

        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productionTracking = productionTrackingForm.getEntity();

        boolean isDraft = ProductionTrackingStateStringValues.DRAFT
                .equals(productionTracking.getStringField(ProductionTrackingFields.STATE));

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        if (order == null) {
            return;
        }

        boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        componentAvailabilityRibbonActionItem.setEnabled(isDraft && (registerQuantityInProduct || registerQuantityOutProduct));
        componentAvailabilityRibbonActionItem.requestUpdate(true);
    }

}
