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
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductDetailsViewHooks {

    public final void addTechnologyGroup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference("form");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
            return;
        }

        Long productId = product.getId();

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("product.id", productId);

        parameters.put("window.activeMenu", "technology.technologyGroups");

        String url = "../page/technologies/technologyGroupDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showTechnologiesWithTechnologyGroup(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference("form");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
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

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("grid.options", gridOptions);

        parameters.put("window.activeMenu", "technology.technologies");

        String url = "../page/technologies/technologiesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showTechnologiesWithProduct(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference("form");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
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
        view.redirectTo(url, false, true, componentsOptions);
    }

    public final void showOrdersWithProductMain(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference("form");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
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

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("grid.options", gridOptions);

        parameters.put("window.activeMenu", "orders.productionOrders");

        String url = "../page/orders/ordersList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showOrdersWithProductPlanned(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference("form");
        Entity product = productForm.getEntity();

        if (product.getId() == null) {
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

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("grid.options", gridOptions);

        parameters.put("window.activeMenu", "orders.productionOrders");

        String url = "../page/orders/ordersList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference("form");
        Entity product = productForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup technologies = (RibbonGroup) window.getRibbon().getGroupByName("technologies");
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName("orders");

        RibbonActionItem addTechnologyGroup = (RibbonActionItem) technologies.getItemByName("addTechnologyGroup");
        RibbonActionItem showTechnologiesWithTechnologyGroup = (RibbonActionItem) technologies
                .getItemByName("showTechnologiesWithTechnologyGroup");
        RibbonActionItem showTechnologiesWithProduct = (RibbonActionItem) technologies
                .getItemByName("showTechnologiesWithProduct");
        RibbonActionItem showOrdersWithProductMain = (RibbonActionItem) orders.getItemByName("showOrdersWithProductMain");
        RibbonActionItem showOrdersWithProductPlanned = (RibbonActionItem) orders.getItemByName("showOrdersWithProductPlanned");

        if (product.getId() != null) {
            updateButtonState(addTechnologyGroup, true);

            Entity technologyGroup = product.getBelongsToField("technologyGroup");

            if (technologyGroup == null) {
                updateButtonState(showTechnologiesWithTechnologyGroup, false);
            } else {
                updateButtonState(showTechnologiesWithTechnologyGroup, true);
            }

            updateButtonState(showTechnologiesWithProduct, true);
            updateButtonState(showOrdersWithProductMain, true);
            updateButtonState(showOrdersWithProductPlanned, true);

            return;
        }

        updateButtonState(addTechnologyGroup, false);
        updateButtonState(showTechnologiesWithTechnologyGroup, false);
        updateButtonState(showTechnologiesWithProduct, false);
        updateButtonState(showOrdersWithProductMain, false);
        updateButtonState(showOrdersWithProductPlanned, false);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
