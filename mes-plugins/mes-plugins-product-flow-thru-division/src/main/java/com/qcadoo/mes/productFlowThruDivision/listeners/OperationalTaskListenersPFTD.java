package com.qcadoo.mes.productFlowThruDivision.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.OperationalTaskMaterialAvailability;
import com.qcadoo.mes.productFlowThruDivision.OrderMaterialAvailability;
import com.qcadoo.mes.productFlowThruDivision.constants.MaterialAvailabilityFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class OperationalTaskListenersPFTD {


    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationalTaskMaterialAvailability operationalTaskMaterialAvailability;


    public void showMaterialAvailabilityForOperationalTask(final ViewDefinitionState view, final ComponentState state,
                                                 final String[] args) {
        Long operationalTaskId = (Long) state.getFieldValue();
        showMaterialAvailability(view, operationalTaskId);
    }

    private void showMaterialAvailability(ViewDefinitionState view, Long operationalTaskId) {
        operationalTaskMaterialAvailability.generateAndSaveMaterialAvailability(getOperationalTaskDD().get(operationalTaskId));

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("operationalTask.id", operationalTaskId);

        parameters.put("window.showBack", true);

        String url = "/page/productFlowThruDivision/operTaskWithMaterialAvailabilityList.html";
        view.redirectTo(url, false, true, parameters);
    }


    public void showReplacementsAvailability(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity record = grid.getSelectedEntities().get(0);
        Entity oma = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_OPER_TASK_MATERIAL_AVAILABILITY).get(record.getId());
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
        Entity oma = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_OPER_TASK_MATERIAL_AVAILABILITY).get(record.getId());
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
        Entity oma = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_OPER_TASK_MATERIAL_AVAILABILITY).get(entity.getId());

        String productNumber = oma.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getStringField(ProductFields.NUMBER);
        Entity location = oma.getBelongsToField(MaterialAvailabilityFields.LOCATION);

        if(Objects.isNull(location)) {
            view.addMessage("productFlowThruDivision.operTaskWithMaterialAvailabilityList.showWarehouseResources.locationEmpty", ComponentState.MessageType.INFO, false);
            return;
        }

        String locationNumber = location.getStringField(LocationFields.NUMBER);

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

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }
}
