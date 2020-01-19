package com.qcadoo.mes.stoppage.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

import java.util.Map;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class AllStoppagesListeners {

    public void addNew(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        grid.performEvent(view, "performNew");
        JSONObject context = view.getJsonContext();
        Map<String, Object> parameters = Maps.newHashMap();
        if (Objects.nonNull(context) && context.has("window.mainTab.stoppage.forOrder")) {
            Long orderId = context.getLong("window.mainTab.stoppage.forOrder");
            parameters.put("form.order", orderId);
            context.remove("window.mainTab.stoppage.forOrder");
            view.setJsonContext(context);
            view.openModal("../page/stoppage/allStoppagesForm.html", parameters);
        } else {
            view.redirectTo("../page/stoppage/allStoppagesForm.html", false, true, parameters);
        }
    }
}
