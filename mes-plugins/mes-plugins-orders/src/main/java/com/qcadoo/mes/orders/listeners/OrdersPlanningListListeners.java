package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class OrdersPlanningListListeners {

    public void changeDates(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        Set<Long> selectedEntitiesIds = grid.getSelectedEntitiesIds();
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.orderIds", selectedEntitiesIds.stream().map(Object::toString).collect(Collectors.joining(",")));

        String url = "../page/orders/changeDatesDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void setCategory(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        Set<Long> selectedEntitiesIds = grid.getSelectedEntitiesIds();
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.selectedEntities", selectedEntitiesIds.stream().map(Object::toString).collect(Collectors.joining(",")));

        String url = "../page/orders/setCategory.html";
        view.openModal(url, parameters);
    }

}
