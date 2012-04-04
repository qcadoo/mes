package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductDetailsViewHooks {

    public final void addTechnologyGroup(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Long productId = (Long) componentState.getFieldValue();

        if (productId == null) {
            return;
        }

    }

    public final void showTechnologiesWithTechnologyGroup(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) componentState;
        Entity product = form.getEntity();

        if (product == null) {
            return;
        }

        Entity technologyGroup = product.getBelongsToField("technologyGroup");

        if (technologyGroup == null) {
            return;
        }

        String technologyGroupNumber = technologyGroup.getStringField(NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("technologyGroupNumber", technologyGroupNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put("filters", filters);

        Map<String, Object> componentsOptions = Maps.newHashMap();
        componentsOptions.put("grid.options", gridOptions);

        componentsOptions.put("window.activeMenu", "technology.technologies");

        String url = "../page/technologies/technologiesList.html";
        viewDefinitionState.redirectTo(url, false, true, componentsOptions);
    }

    public final void showTechnologiesWithProduct(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) componentState;
        Entity product = form.getEntity();

        if (product == null) {
            return;
        }

        String productName = product.getStringField(NAME);

        if (productName == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productName", productName);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put("filters", filters);

        Map<String, Object> componentsOptions = Maps.newHashMap();
        componentsOptions.put("grid.options", gridOptions);

        componentsOptions.put("window.activeMenu", "technology.technologies");

        String url = "../page/technologies/technologiesList.html";
        viewDefinitionState.redirectTo(url, false, true, componentsOptions);
    }

    public final void showOrdersWithProductMain(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) componentState;
        Entity product = form.getEntity();

        if (product == null) {
            return;
        }

        String productNumber = product.getStringField(NUMBER);

        if (productNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", productNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put("filters", filters);

        Map<String, Object> componentsOptions = Maps.newHashMap();
        componentsOptions.put("grid.options", gridOptions);

        componentsOptions.put("window.activeMenu", "orders.productionOrders");

        String url = "../page/orders/ordersList.html";
        viewDefinitionState.redirectTo(url, false, true, componentsOptions);
    }

    public final void showOrdersWithProductPlanned(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) componentState;
        Entity product = form.getEntity();

        if (product == null) {
            return;
        }

        String productNumber = product.getStringField(NUMBER);

        if (productNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", productNumber);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put("filters", filters);

        Map<String, Object> componentsOptions = Maps.newHashMap();
        componentsOptions.put("grid.options", gridOptions);

        componentsOptions.put("window.activeMenu", "orders.productionOrders");

        String url = "../page/orders/ordersList.html";
        viewDefinitionState.redirectTo(url, false, true, componentsOptions);
    }

}
