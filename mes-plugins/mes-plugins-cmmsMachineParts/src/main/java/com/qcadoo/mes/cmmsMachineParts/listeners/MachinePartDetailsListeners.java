package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.hooks.MachinePartDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MachinePartDetailsListeners {

    @Autowired
    private MachinePartDetailsHooks machinePartDetailsHooks;

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    public void redirectToWarehouseStateList(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity product = form.getEntity();

        if (product.getId() == null) {
            return;
        }

        String productNumber = product.getStringField("number");

        if (productNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", applyInOperator(productNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "materialFlow.warehouseStock");

        String url = "../page/materialFlowResources/warehouseStocksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        StringBuilder builder = new StringBuilder();
        return builder.append("[").append(value).append("]").toString();
    }

    public void toggleSuppliersGrids(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        machinePartDetailsHooks.toggleSuppliersGrids(view);
    }
}
