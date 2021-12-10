package com.qcadoo.mes.productFlowThruDivision.listeners;

import java.util.Map;

import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.model.api.DataDefinitionService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.MaterialAvailabilityFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderWithMaterialAvailabilityListListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showReplacementsAvailability(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity record = grid.getSelectedEntities().get(0);
        Entity oma = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY).get(record.getId());
        Long productId = oma.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getId();

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
        Entity oma = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY).get(record.getId());
        Long productId = oma.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getId();

        JSONObject json = new JSONObject();

        try {
            json.put("product.id", productId);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "/page/productFlowThruDivision/materialAvailabilityList.html?context=" + json;
        view.redirectTo(url, false, true);
    }

    public final void showWarehouseResources(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Entity entity = grid.getSelectedEntities().get(0);
        Entity oma = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY).get(entity.getId());

        String productNumber = oma.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getStringField(ProductFields.NUMBER);
        String locationNumber = oma.getBelongsToField(MaterialAvailabilityFields.LOCATION)
                .getStringField(LocationFields.NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", applyInOperator(productNumber));
        filters.put("locationNumber", applyInOperator(locationNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "materialFlow.resources");

        String url = "../page/materialFlowResources/resourcesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        return "[" + value + "]";
    }

}
