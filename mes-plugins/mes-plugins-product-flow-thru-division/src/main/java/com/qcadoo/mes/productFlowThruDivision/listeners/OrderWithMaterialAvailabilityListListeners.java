package com.qcadoo.mes.productFlowThruDivision.listeners;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.MaterialAvailabilityFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderWithMaterialAvailabilityListListeners {



    public void showReplacementsAvailability(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity record = grid.getSelectedEntities().get(0);

        Long productId = record.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getId();

        JSONObject json = new JSONObject();

        try {
            json.put("product.id", productId);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "/page/productFlowThruDivision/materialReplacementsAvailabilityList.html?context=" + json.toString();
        view.redirectTo(url, false, true);
    }

    public void showAvailability(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity record = grid.getSelectedEntities().get(0);

        Long productId = record.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getId();

        JSONObject json = new JSONObject();

        try {
            json.put("product.id", productId);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "/page/productFlowThruDivision/materialAvailabilityList.html?context=" + json.toString();
        view.redirectTo(url, false, true);
    }

}
