package com.qcadoo.mes.technologies.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OperationGroupDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    public void redirectToFilteredOperationGroupsList(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        FormComponent operationGroupForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity operationGroup = operationGroupForm.getEntity();

        if (operationGroup.getId() == null) {
            return;
        }

        String operationGroupNumber = operationGroup.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("operationGroupNumber", operationGroupNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.operation");

        String url = "../page/technologies/operationsList.html";
        view.redirectTo(url, false, true, parameters);

    }
}
