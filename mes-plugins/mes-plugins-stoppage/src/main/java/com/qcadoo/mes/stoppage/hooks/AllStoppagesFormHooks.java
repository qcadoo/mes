package com.qcadoo.mes.stoppage.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class AllStoppagesFormHooks {

    public final void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        JSONObject context = view.getJsonContext();
        context.toString();
        if(view.isViewAfterRedirect() && context.has("window.mainTab.form.order")) {
            Long orderId = context.getLong("window.mainTab.form.order");
            LookupComponent orderLookupComponent = (LookupComponent) view.getComponentByReference("order");
            orderLookupComponent.setFieldValue(orderId);
            orderLookupComponent.requestComponentUpdateState();
        }
    }
}
