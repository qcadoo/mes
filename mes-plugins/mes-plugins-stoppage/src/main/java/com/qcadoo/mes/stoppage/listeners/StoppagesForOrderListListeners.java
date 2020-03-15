package com.qcadoo.mes.stoppage.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class StoppagesForOrderListListeners {

    public void addNew(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        JSONObject context = view.getJsonContext();
        Long orderId = context.getLong("window.mainTab.stoppage.forOrder");
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.order", orderId);
        view.openModal("../page/stoppage/stoppageForOrderForm.html", parameters);

    }
}
