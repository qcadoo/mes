package com.qcadoo.mes.basic.listeners;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CompanyDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    public void redirectToFilteredOrderProductionList(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = companyForm.getEntity();

        if (company.getId() == null) {
            return;
        }

        String companyName = company.getStringField("name");

        Map<String, String> filters = Maps.newHashMap();
        filters.put("companyName", companyName);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/ordersList.html";
        view.redirectTo(url, false, true, parameters);

    }
}
