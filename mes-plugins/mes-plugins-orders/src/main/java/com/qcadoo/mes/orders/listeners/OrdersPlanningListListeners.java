package com.qcadoo.mes.orders.listeners;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class OrdersPlanningListListeners {

    private static final String L_GRID = "grid";

    public void changeDates(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(L_GRID);

        Set<Long> selectedEntitiesIds = ordersGrid.getSelectedEntitiesIds();

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.orderIds", selectedEntitiesIds.stream().map(Object::toString).collect(Collectors.joining(",")));

        String url = "../page/orders/changeDatesDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void setCategory(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(L_GRID);

        Set<Long> selectedEntitiesIds = ordersGrid.getSelectedEntitiesIds();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.selectedEntities",
                selectedEntitiesIds.stream().map(Object::toString).collect(Collectors.joining(",")));

        String url = "../page/orders/setCategory.html";
        view.openModal(url, parameters);
    }

    public void openOrdersImportPage(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        StringBuilder url = new StringBuilder("../page/orders/ordersImport.html");

        view.openModal(url.toString());
    }

}
