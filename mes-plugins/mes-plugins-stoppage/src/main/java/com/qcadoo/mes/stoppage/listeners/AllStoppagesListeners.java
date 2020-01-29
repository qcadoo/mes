package com.qcadoo.mes.stoppage.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

import java.util.Map;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class AllStoppagesListeners {

    private static final String L_FORM = "form";

    private static final String L_ORDER = "order";

    public void addNew(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        JSONObject context = view.getJsonContext();
        Map<String, Object> parameters = Maps.newHashMap();
        if (Objects.nonNull(context) && context.has("window.mainTab.stoppage.forOrder")) {
            Long orderId = context.getLong("window.mainTab.stoppage.forOrder");
            parameters.put("form.order", orderId);
            view.openModal("../page/stoppage/allStoppagesForm.html", parameters);
        } else {
            view.redirectTo("../page/stoppage/allStoppagesForm.html", false, true, parameters);
        }
    }

    public void addNewFromProductionTracking(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity productionTracking = form.getEntity();
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.productionTracking", productionTracking.getId());
        parameters.put("form.order", productionTracking.getBelongsToField(L_ORDER).getId());
        view.openModal("../page/stoppage/allStoppagesForm.html", parameters);
    }
}
