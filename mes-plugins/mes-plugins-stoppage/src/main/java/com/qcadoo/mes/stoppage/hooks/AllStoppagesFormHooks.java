package com.qcadoo.mes.stoppage.hooks;

import com.qcadoo.mes.stoppage.constants.StoppageFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AllStoppagesFormHooks {

    private static final String L_ORDER = "order";

    private static final String L_PRODUCTION_TRACKING = "productionTracking";

    private static final String L_CONTEXT_KEY_PRODUCTION_TRACKING = "window.mainTab.form.productionTracking";

    private static final String L_CONTEXT_KEY_ORDER = "window.mainTab.form.order";

    public final void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        if (Objects.isNull(((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntityId())) {
            JSONObject context = view.getJsonContext();

            if (view.isViewAfterRedirect() && context.has(L_CONTEXT_KEY_PRODUCTION_TRACKING)) {
                Long productionTrackingId = context.getLong(L_CONTEXT_KEY_PRODUCTION_TRACKING);
                Long orderId = context.getLong(L_CONTEXT_KEY_ORDER);

                LookupComponent orderLookupComponent = (LookupComponent) view.getComponentByReference(L_ORDER);
                orderLookupComponent.setFieldValue(orderId);
                orderLookupComponent.setEnabled(false);
                orderLookupComponent.requestComponentUpdateState();

                LookupComponent productionTrackingComponent = (LookupComponent) view
                        .getComponentByReference(L_PRODUCTION_TRACKING);
                productionTrackingComponent.setFieldValue(productionTrackingId);
                productionTrackingComponent.setEnabled(false);
                productionTrackingComponent.requestComponentUpdateState();
            } else if (view.isViewAfterRedirect() && context.has(L_CONTEXT_KEY_ORDER)) {
                Long orderId = context.getLong(L_CONTEXT_KEY_ORDER);
                LookupComponent orderLookupComponent = (LookupComponent) view.getComponentByReference(L_ORDER);
                orderLookupComponent.setFieldValue(orderId);
                orderLookupComponent.setEnabled(false);
                orderLookupComponent.requestComponentUpdateState();
            }
        } else {
            LookupComponent orderLookupComponent = (LookupComponent) view.getComponentByReference(L_ORDER);
            LookupComponent productionTrackingComponent = (LookupComponent) view.getComponentByReference(L_PRODUCTION_TRACKING);
            JSONObject context = view.getJsonContext();
            if (Objects.nonNull(context) && context.has(L_CONTEXT_KEY_PRODUCTION_TRACKING)) {
                orderLookupComponent.setEnabled(false);
                productionTrackingComponent.setEnabled(false);
            }
            Entity order = orderLookupComponent.getEntity();

            if (order != null) {
                FilterValueHolder holder = productionTrackingComponent.getFilterValue();

                holder.put(StoppageFields.ORDER, order.getId());

                productionTrackingComponent.setFilterValue(holder);
            }
        }
    }
}
