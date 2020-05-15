package com.qcadoo.mes.stoppage.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AllStoppagesListeners {

    

    private static final String L_ORDER = "order";

    public void addNew(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        Map<String, Object> parameters = Maps.newHashMap();
        view.redirectTo("../page/stoppage/allStoppagesForm.html", false, true, parameters);
    }

    public void addNewFromProductionTracking(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productionTracking = form.getEntity();
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.productionTracking", productionTracking.getId());
        parameters.put("form.order", productionTracking.getBelongsToField(L_ORDER).getId());
        view.openModal("../page/stoppage/allStoppagesForm.html", parameters);
    }
}
